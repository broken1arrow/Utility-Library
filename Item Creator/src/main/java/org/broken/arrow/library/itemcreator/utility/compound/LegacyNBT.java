package org.broken.arrow.library.itemcreator.utility.compound;

import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.logging.Validate;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.logging.Level;

/**
 * Provides reflective access to NMS ItemStacks and NBTTagCompounds in legacy
 * Minecraft versions see {@link NbtData} for more info.
 *
 * <p>This class is intended as an internal utility for low-level item modifications.
 * It should generally be accessed through {@link NbtData} or {@link CompoundTag} rather than
 * directly by plugin developers.</p>
 */
public class LegacyNBT {
    private static final Logging logger = new Logging(LegacyNBT.class);

    private LegacyNBT() {
    }

    /**
     * Creates a new {@link NmsItemSession} for the given Bukkit {@link ItemStack}.
     *
     * <p>This method converts the provided {@link ItemStack} into its internal
     * NMS representation and prepares all required reflective access for
     * reading and writing its underlying {@code NBTTagCompound}.</p>
     *
     * <p>If the required NMS classes or methods could not be resolved during
     * startup, this method will throw an {@link IllegalStateException}. Always
     * verify availability first via {@link NmsItemSession#isReady()}, it will have
     * checks so nothing breaks if you miss it.</p>
     *
     * <p>This method is intended to be used internally by {@link NbtData} and
     * other bridge classes. End-user plugin code should prefer {@link NbtData}.</p>
     *
     * @param stack the Bukkit ItemStack to wrap
     * @return a new {@link NmsItemSession} for the given ItemStack
     * @throws IllegalStateException if the NMS bridge is not available
     */
    public static NmsItemSession session(@Nonnull final ItemStack stack) {
        if (!NmsItemSession.REFLECTION_READY) {
            logger.log(Level.WARNING, () -> "NMS bridge not loaded");
            return null;
        }
        return new NmsItemSession(stack);
    }

    /**
     * Creates a new {@link CompoundSession} for the given NBTTagCompound handle.
     *
     * <p>This method is responsible for binding reflective access to the
     * underlying {@code NBTTagCompound} instance. It enables operations such
     * as {@code hasKey}, {@code setBoolean}, and {@code getBoolean}</p>
     *
     * <p>This is a low-level internal factory and is used by {@link CompoundTag}.
     * End-user code should never call this method directly.</p>
     *
     * @param handle the raw NBTTagCompound instance from NMS
     * @return a new {@link CompoundSession} bound to the provided handle
     * @throws IllegalStateException if the CompoundSession layer is not available
     */
    public static CompoundSession compoundSession(@Nonnull final Object handle) {
        if (!CompoundSession.isReady()) {
            logger.log(Level.WARNING, () -> "CompoundSession not loaded");
            return null;
        }
        return new CompoundSession(handle);
    }

    /**
     * A per-item reflective session that provides access to the underlying NMS ItemStack
     * and its NBTTagCompound.
     *
     * <p>All operations assume that reflection has been successfully initialized. If not,
     * methods will fail silently or throw exceptions as appropriate.</p>
     */
    public static class NmsItemSession {
        private static final MethodHandle NMS_ITEM_COPY;
        private static final MethodHandle AS_BUKKIT_ITEM_COPY;
        private static final MethodHandle HAS_TAG;
        private static final MethodHandle GET_TAG;
        private static final MethodHandle SET_TAG;
        private static final Constructor<?> nbtTagConstructor;
        private static final MethodHandle GET_COMPOUND;
        private static final MethodHandle GET_NESTED_COMPOUND;
        private static final MethodHandle SET_NESTED_COMPOUND;

        private static final boolean REFLECTION_READY;
        private final Object nmsItemCopy;
        private final ItemStack bukkitItem;
        private boolean finalize = false;
        private CompoundState compoundState = CompoundState.NOT_CREATED;

        static {
            Constructor<?> tagConstructor = null;
            MethodHandle setNBTTag = null;
            MethodHandle getNBTTag = null;
            MethodHandle hasNBTTag = null;
            MethodHandle bukkitCopy = null;
            MethodHandle nMSCopyItem = null;
            MethodHandle getCompoundM = null;
            MethodHandle getNestedCompound = null;
            MethodHandle setNestedCompound = null;
            boolean reflectionDone = false;

            try {
                final String craftPath = getCraftBukkitPath();
                final String nmsPath = getNmsPath();

                final Class<?> craftItemStack = Class.forName(craftPath + ".inventory.CraftItemStack");
                final Class<?> nmsItemStack = Class.forName(nmsPath + ".ItemStack");
                final Class<?> nbtTagCompound = Class.forName(getNbtTagPath());
                final Class<?> nbtTagBase = Class.forName(getNbtTagBasePath());

                final MethodHandles.Lookup lookup = MethodHandles.lookup();

                nMSCopyItem = lookup.findStatic(craftItemStack, "asNMSCopy",
                        MethodType.methodType(nmsItemStack, ItemStack.class));
                bukkitCopy = lookup.findStatic(craftItemStack, "asBukkitCopy",
                        MethodType.methodType(ItemStack.class, nmsItemStack));

                hasNBTTag = lookup.findVirtual(nmsItemStack, "hasTag",
                        MethodType.methodType(boolean.class));
                getNBTTag = lookup.findVirtual(nmsItemStack, "getTag",
                        MethodType.methodType(nbtTagCompound));
                setNBTTag = lookup.findVirtual(nmsItemStack, "setTag",
                        MethodType.methodType(void.class, nbtTagCompound));

                setNestedCompound = lookup.findVirtual(nbtTagCompound, "set", MethodType.methodType(void.class, String.class, nbtTagBase));
                getNestedCompound = lookup.findVirtual(nbtTagCompound, "get", MethodType.methodType(nbtTagBase, String.class));
                getCompoundM = lookup.findVirtual(nbtTagCompound, "getCompound", MethodType.methodType(nbtTagCompound, String.class));

                tagConstructor = nbtTagCompound.getConstructor();
                reflectionDone = true;
            } catch (IllegalAccessException | ClassNotFoundException | NoSuchMethodException e) {
                logger.logError(e, () -> "Failed to initialize all NMS methods needed for legacy minecraft.");
            }
            nbtTagConstructor = tagConstructor;
            SET_TAG = setNBTTag;
            GET_TAG = getNBTTag;
            HAS_TAG = hasNBTTag;
            AS_BUKKIT_ITEM_COPY = bukkitCopy;
            NMS_ITEM_COPY = nMSCopyItem;
            GET_COMPOUND = getCompoundM;
            GET_NESTED_COMPOUND = getNestedCompound;
            SET_NESTED_COMPOUND = setNestedCompound;
            REFLECTION_READY = reflectionDone;

        }

        private NmsItemSession(@Nonnull ItemStack item) {
            this.nmsItemCopy = toNmsItemStack(item);
            this.bukkitItem = item.clone();
        }

        /**
         * Checks if it has loaded all reflections.
         *
         * @return true if everything is loaded correctly.
         */
        public boolean isReady() {
            if (!REFLECTION_READY)
                return false;

            return nmsItemCopy != null;
        }

        /**
         * Checks if the item has a NBTTagCompound.
         *
         * @return {@code true} if it has an NBTTagCompound.
         */
        public boolean hasTag() {
            if (!REFLECTION_READY || nmsItemCopy == null) return false;
            try {
                return (boolean) HAS_TAG.invoke(this.nmsItemCopy);
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to initialize hasTag");
            }
            return false;
        }

        /**
         * Checks whether this item contains an {@code NBTTagCompound} with the given name.
         * <p>
         * If the name is empty, the <strong>root compound</strong> is evaluated instead.
         * Both root and nested compounds are considered valid targets.
         *
         * @param name the custom key of the nested compound. To target the root compound,
         *             use an empty string or {@link #hasTag()}.
         * @return {@code true} if the specified (or root) compound exists
         */
        public boolean hasTag(@Nonnull final String name) {
            try {
                if (!hasTag()) return false;
                Object root = GET_TAG.invoke(nmsItemCopy);
                if (name.isEmpty()) return root != null;
                Object nested = GET_NESTED_COMPOUND.invoke(root, name);
                return nested != null;
            } catch (Throwable ignored) {
                return false;
            }
        }

        /**
         * Returns the root {@link CompoundTag} of this item, creating one if it does not exist.
         * <p>
         * This method always operates on the root compound. If you want a nested compound,
         * use {@link #getOrCreateCompound(String)} with a specific name.
         *
         * @return the root {@link CompoundTag}, never {@code null} unless reflection failed.
         */
        @Nullable
        public CompoundTag getOrCreateCompound() {
            return getCompoundTag("", false);
        }

        /**
         * Returns a {@link CompoundTag} with the given name, creating it if it does not exist.
         * <p>
         * If {@code name} is empty (""), this will return the root compound, which is equivalent
         * to {@link #getOrCreateCompound()}.
         * <p>
         * Use a non-empty name if you want a nested compound separate from the root.
         *
         * @param name the name of the nested compound, or empty string for root.
         * @return the existing or newly created {@link CompoundTag}, or {@code null} if reflection failed.
         */
        @Nullable
        public CompoundTag getOrCreateCompound(@Nonnull final String name) {
            return getCompoundTag(name, true);
        }


        /**
         * Returns the root {@link CompoundTag} if present.
         * <p>
         * This method does not create a new compound. Use {@link #getOrCreateCompound()} to
         * create a root compound if it does not exist.
         *
         * @return the root {@link CompoundTag} if present, otherwise {@code null}.
         */
        @Nullable
        public CompoundTag getCompound() {
            return getCompound("", false);
        }

        /**
         * Returns the {@link CompoundTag} with the given name if present.
         * <p>
         * If {@code name} is empty (""), this returns the root compound.
         * For a nested compound, pass a non-empty name.
         * <p>
         * This method does not create a compound; use {@link #getOrCreateCompound(String)}
         * to create one if it does not exist.
         *
         * @param name the name of the nested compound, or empty string for root.
         * @return the existing {@link CompoundTag} if present, otherwise {@code null}.
         */
        @Nullable
        public CompoundTag getCompound(@Nonnull final String name) {
            return getCompound(name, true);
        }

        /**
         * Applies the current NBT data of this item to the underlying {@link ItemStack} and
         * returns a new Bukkit {@link ItemStack} instance.
         * <p>
         * This method always applies the root {@link CompoundTag}, including any nested compounds
         * created via {@link #getOrCreateCompound(String)}. The returned item will contain the
         * full NBT structure currently set in this session.
         * <p>
         * The method checks the {@link CompoundState} before applying changes:
         * <ul>
         *     <li>{@link CompoundState#CREATED}: Compound exists and will be applied.</li>
         *     <li>{@link CompoundState#NULL}: No compound exists, nothing is applied.</li>
         *     <li>{@link CompoundState#ERROR}: Reflection failed or compound initialization failed,
         *     nothing is applied.</li>
         *     <li>{@link CompoundState#NOT_CREATED}: No compound has been created yet.</li>
         * </ul>
         * <p>
         * Use {@link #getOrCreateCompound()} or {@link #getOrCreateCompound(String)} to ensure a
         * compound exists before calling this method.
         *
         * @return a new {@link ItemStack} containing the applied NBT, or the original {@link ItemStack}
         * if the compound was not created or an error occurred.
         */
        @Nonnull
        public ItemStack finalizeChanges() {
            if (!REFLECTION_READY || nmsItemCopy == null) return this.bukkitItem;
            if (compoundState != CompoundState.CREATED) {
                logger.log(() -> "FinalizeChanges: " + compoundState.getMessage());
                return this.bukkitItem;
            }
            try {
                Object compound = GET_TAG.invoke(nmsItemCopy);
                if (compound == null) {
                    logger.log(() -> "Failed to initialize the item creation, because the compound is not created yet.");
                    return this.bukkitItem;
                }
                SET_TAG.invoke(nmsItemCopy, compound);
                finalize = true;
                return (ItemStack) AS_BUKKIT_ITEM_COPY.invoke(nmsItemCopy);
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to apply back to itemStack");
            }
            return this.bukkitItem;
        }

        /**
         * Converts a Bukkit ItemStack into its NMS counterpart.
         *
         * @param item Bukkit ItemStack to convert.
         * @return the underlying Nms itemStack.
         */
        private Object toNmsItemStack(@Nonnull final ItemStack item) {
            try {
                return NMS_ITEM_COPY.invoke(item);
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to copy the bukkit stack to nms stack");
            }
            return null;
        }

        /**
         * Internal helper to get or create a {@link CompoundTag}.
         * <p>
         * If {@code usingName} is true and the {@code name} parameter is empty,
         * a debug log is written suggesting to use {@link #getOrCreateCompound()} instead
         * for the root compound.
         *
         * @param name      the name of the nested compound; empty string returns the root.
         * @param usingName whether this call is intended for a named compound.
         * @return the requested {@link CompoundTag}, or null if reflection fails.
         */
        private CompoundTag getCompoundTag(final String name, final boolean usingName) {
            if (usingName && name.isEmpty())
                logger.log(Level.FINE, () -> "Empty string passed to getOrCreateCompound(name). Use getOrCreateCompound() for root instead.");
            try {
                Object root;
                Object nested = null;
                if (hasTag()) {
                    root = GET_TAG.invoke(nmsItemCopy);
                } else {
                    root = nbtTagConstructor.newInstance();
                    SET_TAG.invoke(nmsItemCopy, root);
                }
                if (!name.isEmpty()) {
                    nested = GET_NESTED_COMPOUND.invoke(root, name);
                    if (nested == null) {
                        nested = nbtTagConstructor.newInstance();
                        SET_NESTED_COMPOUND.invoke(root, name, nested);
                    }
                }
                this.compoundState = CompoundState.CREATED;
                return new CompoundTag(nested != null ? nested : root);
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to initialize CompoundTag");
                this.compoundState = CompoundState.ERROR;
            }
            return null;
        }

        /**
         * Internal helper to get a {@link CompoundTag} if present.
         * <p>
         * If {@code usingName} is true and the {@code name} parameter is empty,
         * a debug log is written suggesting to use {@link #getCompound()} instead
         * for the root compound.
         *
         * @param name      the name of the nested compound; empty string returns the root.
         * @param usingName whether this call is intended for a named compound.
         * @return the requested {@link CompoundTag}, or null if not present or reflection fails.
         */
        private CompoundTag getCompound(final String name, final boolean usingName) {
            if (usingName && name.isEmpty())
                logger.log(Level.FINE, () -> "Empty string passed to getCompound(name). Use getCompound() for root instead.");

            try {
                if (!hasTag()) {
                    this.compoundState = CompoundState.NULL;
                    return null;
                }
                Object root = GET_TAG.invoke(nmsItemCopy);
                Object nested = null;
                if (!name.isEmpty()) {
                    nested = GET_NESTED_COMPOUND.invoke(root, name);
                    if (nested == null) {
                        logger.log(Level.CONFIG, () -> "Requested nested compound '" + name + "' not found. Returning root instead.");
                    }
                }
                this.compoundState = CompoundState.CREATED;
                return new CompoundTag(nested != null ? nested : root);
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to initialize CompoundTag");
                this.compoundState = CompoundState.ERROR;
            }
            return null;
        }

        private static String getCraftBukkitPath() {
            return "org.bukkit.craftbukkit." + getPackageVersion();
        }
    }

    /**
     * A per-NBTTagCompound reflective session. Provides low-level access to manipulate
     * properties on the NMS compound.
     *
     * <p>Should generally be used via {@link CompoundTag} rather than directly.</p>
     */
    public static class CompoundSession {
        private static final MethodHandle hasKey;
        private static final MethodHandle remove;
        private static final MethodHandle setString;
        private static final MethodHandle getString;
        private static final MethodHandle setInt;
        private static final MethodHandle getInt;
        private static final MethodHandle getShort;
        private static final MethodHandle setShort;
        private static final MethodHandle setByte;
        private static final MethodHandle getByte;
        private static final MethodHandle setBoolean;
        private static final MethodHandle getBoolean;
        private final Object handle;

        static {
            MethodHandle hasTagKey = null;
            MethodHandle removeM = null;
            MethodHandle setStringM = null;
            MethodHandle getStringM = null;
            MethodHandle setIntM = null;
            MethodHandle getIntM = null;
            MethodHandle getShortM = null;
            MethodHandle setShortM = null;
            MethodHandle setByteM = null;
            MethodHandle getByteM = null;
            MethodHandle setBooleanM = null;
            MethodHandle getBooleanM = null;
            try {
                final Class<?> nbtTag = Class.forName(getNbtTagPath());
                final MethodHandles.Lookup lookup = MethodHandles.lookup();

                Arrays.stream(nbtTag.getMethods()).forEach(method -> {
                    System.out.println("######################################");
                    System.out.println("m " + method.getName());
                    System.out.println("ParameterTypes " + Arrays.toString(method.getParameterTypes()));
                    System.out.println("ReturnType " + method.getReturnType());
                });

                hasTagKey = lookup.findVirtual(nbtTag, "hasKey",
                        MethodType.methodType(boolean.class, String.class));
                removeM = lookup.findVirtual(nbtTag, "remove",
                        MethodType.methodType(void.class, String.class));

                setIntM = lookup.findVirtual(nbtTag, "setInt",
                        MethodType.methodType(void.class, String.class, int.class));
                getIntM = lookup.findVirtual(nbtTag, "getInt",
                        MethodType.methodType(int.class, String.class));

                setShortM = lookup.findVirtual(nbtTag, "setShort",
                        MethodType.methodType(void.class, String.class, short.class));
                getShortM = lookup.findVirtual(nbtTag, "getShort",
                        MethodType.methodType(short.class, String.class));

                setByteM = lookup.findVirtual(nbtTag, "setByte",
                        MethodType.methodType(void.class, String.class, byte.class));
                getByteM = lookup.findVirtual(nbtTag, "getByte",
                        MethodType.methodType(byte.class, String.class));

                setStringM = lookup.findVirtual(nbtTag, "setString",
                        MethodType.methodType(void.class, String.class, String.class));
                getStringM = lookup.findVirtual(nbtTag, "getString",
                        MethodType.methodType(String.class, String.class));

                setBooleanM = lookup.findVirtual(nbtTag, "setBoolean",
                        MethodType.methodType(void.class, String.class, boolean.class));
                getBooleanM = lookup.findVirtual(nbtTag, "getBoolean",
                        MethodType.methodType(boolean.class, String.class));

            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
                logger.logError(e, () -> "Failed to bind NBT methods");
            }
            remove = removeM;
            hasKey = hasTagKey;
            setString = setStringM;
            getString = getStringM;
            setInt = setIntM;
            getInt = getIntM;
            getShort = getShortM;
            setShort = setShortM;
            setByte = setByteM;
            getByte = getByteM;

            setBoolean = setBooleanM;
            getBoolean = getBooleanM;

        }

        CompoundSession(@Nonnull final Object handle) {
            Validate.checkNotNull(handle, "CompoundTag handle cannot be null");
            this.handle = handle;
        }

        /**
         * Checks if it has loaded all reflections.
         *
         * @return true if everything is loaded correctly.
         */
        public static boolean isReady() {
            return hasKey != null && setBoolean != null && getBoolean != null;
        }

        /**
         * This handle is the NBTTagCompound object.
         *
         * @return returns the NBTTagCompound object.
         */
        @Nonnull
        Object getHandle() {
            return handle;
        }

        /**
         * Checks whether this {@link CompoundTag} contains the given key.
         *
         * @param key the NBT key to check
         * @return {@code true} if the key exists, otherwise {@code false}
         */
        public boolean hasKey(@Nonnull final String key) {
            if (hasKey == null) return false;

            try {
                return (boolean) hasKey.invoke(handle, key);
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to check if the compound have the key.");
            }
            return false;
        }


        /**
         * Remove this {@link CompoundTag} value and the given key.
         *
         * @param key the NBT key to remove.
         */
        public void remove(@Nonnull final String key) {
            if (remove == null) return;

            try {
                remove.invoke(handle, key);
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to check if the compound have the key.");
            }
        }

        /**
         * Sets a int value in the underlying NBTTagCompound.
         *
         * @param key   the key to set
         * @param value the int value to assign
         */
        public void setInt(@Nonnull final String key, final int value) {
            if (setInt == null) return;

            try {
                setInt.invoke(handle, key, value);
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to set int value from reflection");
            }
        }

        /**
         * Gets a int value from the underlying NBTTagCompound.
         *
         * @param key the key of the int value
         * @return the stored int value, or {@code -1} if unavailable
         */
        public int getInt(@Nonnull final String key) {
            if (getInt == null) return -1;

            try {
                return (int) getInt.invoke(handle, key);
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to retrieve int value from reflection");
            }
            return -1;
        }

        /**
         * Sets a String value in the underlying NBTTagCompound.
         *
         * @param key   the key to set
         * @param value the String value to assign
         */
        public void setString(@Nonnull final String key, final String value) {
            if (setString == null) return;

            try {
                setString.invoke(handle, key, value);
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to set string value from reflection");
            }
        }

        /**
         * Gets a string value from the underlying NBTTagCompound.
         *
         * @param key the key of the string value
         * @return the stored string value, or empty string if unavailable
         */
        public String getString(@Nonnull final String key) {
            if (getString == null) return "";

            try {
                return (String) getString.invoke(handle, key);
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to retrieve string value from reflection");
            }
            return "";
        }

        /**
         * Sets a byte value in the underlying NBTTagCompound.
         *
         * @param key   the key to set
         * @param value the byte value to assign
         */
        public void setByte(@Nonnull final String key, final byte value) {
            if (setByte == null) return;

            try {
                setByte.invoke(handle, key, value);
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to set byte value from reflection");
            }
        }

        /**
         * Gets a byte value from the underlying NBTTagCompound.
         *
         * @param key the key of the byte value
         * @return the stored byte value, or {@code -1} if unavailable
         */
        public byte getByte(@Nonnull final String key) {
            if (getByte == null) return -1;

            try {
                return (byte) getByte.invoke(handle, key);
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to retrieve byte value from reflection");
            }
            return -1;
        }

        /**
         * Sets a boolean value in the underlying NBTTagCompound.
         *
         * @param key   the key to set
         * @param value the boolean value to assign
         */
        public void setBoolean(@Nonnull final String key, final boolean value) {
            if (setBoolean == null) return;

            try {
                setBoolean.invoke(handle, key, value);
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to set boolean value from reflection");
            }
        }

        /**
         * Gets a boolean value from the underlying NBTTagCompound.
         *
         * @param key the key of the boolean value
         * @return the stored boolean value, or {@code false} if unavailable
         */
        public boolean getBoolean(@Nonnull final String key) {
            if (getBoolean == null) return false;

            try {
                return (boolean) getBoolean.invoke(handle, key);
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to retrieve boolean value from reflection");
            }
            return false;
        }

        /**
         * Sets a short value in the underlying NBTTagCompound.
         *
         * @param key   the key to set
         * @param value the short value to assign
         */
        public void setShort(@Nonnull final String key, final short value) {
            if (setShort == null) return;

            try {
                setShort.invoke(handle, key, value);
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to set short value from reflection");
            }
        }

        /**
         * Gets a short value from the underlying NBTTagCompound.
         *
         * @param key the key of the short value
         * @return the stored short value, or {@code -1} if unavailable
         */
        public short getShort(@Nonnull final String key) {
            if (getShort == null) return -1;

            try {
                return (short) getShort.invoke(handle, key);
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to retrieve short value from reflection");
            }
            return -1;
        }

    }

    private static String getNbtTagPath() {
        return getNmsPath() + ".NBTTagCompound";
    }

    private static String getNbtTagBasePath() {
        return getNmsPath() + ".NBTBase";
    }

    private static String getNmsPath() {
        return "net.minecraft.server." + getPackageVersion();
    }

    /**
     * Extracts the version identifier from the Bukkit server package.
     * This version will only work on legacy, as the path changed in newer
     * minecraft versions.
     * Example: v1_8_R3
     *
     * @return it returns for example v1_8_R3
     */
    private static String getPackageVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

    /**
     * Represents the current state of a compound during retrieval or creation.
     *
     * <p>There are four possible states:</p>
     * <ul>
     *     <li><strong>NOT_CREATED</strong> – The compound has not been created yet.</li>
     *     <li><strong>CREATED</strong> – The compound was successfully retrieved or created.
     *         This may refer to either the root compound or a nested compound.</li>
     *     <li><strong>NULL</strong> – The root or nested compound does not exist.</li>
     *     <li><strong>ERROR</strong> – An error occurred while attempting to retrieve or create the compound.</li>
     * </ul>
     */
    public enum CompoundState {
        NOT_CREATED("Compound has not been created yet and may be null"),
        CREATED("Compound created successfully"),
        ERROR("Failed to initialize compound"),
        NULL("Compound does not exist");

        private final String message;

        CompoundState(@Nonnull final String message) {
            this.message = message;
        }

        /**
         * Returns a descriptive message suitable for logging.
         *
         * @return the descriptive message for this state.
         */
        public String getMessage() {
            return message;
        }
    }
}
