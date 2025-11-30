package org.broken.arrow.library.itemcreator.utility.nms;

import org.broken.arrow.library.itemcreator.ItemCreator;
import org.broken.arrow.library.itemcreator.utility.compound.CompoundTag;
import org.broken.arrow.library.itemcreator.utility.compound.NbtData;
import org.broken.arrow.library.itemcreator.utility.nms.api.CompoundEditor;
import org.broken.arrow.library.itemcreator.utility.nms.api.NbtEditor;
import org.broken.arrow.library.itemcreator.utility.nms.mappings.NBTCompoundMappings;
import org.broken.arrow.library.itemcreator.utility.nms.mappings.NBTItemTagMappings;
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
import java.util.logging.Level;

/**
 * Provides reflective access to NMS ItemStacks and NBTTagCompounds in legacy
 * Minecraft versions see {@link NbtData} for more info.
 *
 * <p>This class is intended as an internal utility for low-level item modifications.
 * It should generally be accessed through {@link NbtData} or {@link CompoundTag} rather than
 * directly by plugin developers.</p>
 */
public class NBTAdapter {
    private static final Logging logger = new Logging(NBTAdapter.class);

    private NBTAdapter() {
    }

    /**
     * Creates a new {@link NbtEditor} for the given Bukkit {@link ItemStack}.
     *
     * <p>This method converts the provided {@link ItemStack} into its internal
     * NMS representation and prepares all required reflective access for
     * reading and writing its underlying {@code NBTTagCompound}.</p>
     *
     * <p>If the required NMS classes or methods could not be resolved during
     * startup, this method will throw an {@link IllegalStateException}. Always
     * verify availability first via {@link NbtEditor#isReady()}, it will have
     * checks so nothing breaks if you miss it.</p>
     *
     * <p>This method is intended to be used internally by {@link NbtData} and
     * other bridge classes. End-user plugin code should prefer {@link NbtData}.</p>
     *
     * @param stack the Bukkit ItemStack to wrap
     * @return a new {@link  NbtEditor} for the given ItemStack
     * @throws IllegalStateException if the NMS bridge is not available
     */
    public static NbtEditor session(@Nonnull final ItemStack stack) {
        if (!NmsItemSession.REFLECTION_READY) {
            logger.log(Level.WARNING, () -> "NMS bridge not loaded");
            return null;
        }
        return new NmsItemSession(stack);
    }

    /**
     * Creates a new {@link CompoundEditor} for the given NBTTagCompound handle.
     *
     * <p>This method is responsible for binding reflective access to the
     * underlying {@code NBTTagCompound} instance. It enables operations such
     * as {@code hasKey}, {@code setBoolean}, and {@code getBoolean}</p>
     *
     * <p>This is a low-level internal factory and is used by {@link CompoundTag}.
     * End-user code should never call this method directly.</p>
     *
     * @param handle the raw NBTTagCompound instance from NMS
     * @return a new {@link CompoundEditor} bound to the provided handle
     * @throws IllegalStateException if the CompoundSession layer is not available
     */
    public static CompoundEditor compoundSession(@Nonnull final Object handle) {
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
    public static class NmsItemSession implements NbtEditor {
        private static final MethodHandle NMS_ITEM_COPY;
        private static final MethodHandle AS_BUKKIT_ITEM_COPY;
        private static final MethodHandle HAS_TAG;
        private static final MethodHandle GET_TAG;
        private static final MethodHandle SET_TAG;
        private static final Constructor<?> nbtTagConstructor;
        private static final MethodHandle GET_NESTED_COMPOUND;
        private static final MethodHandle SET_NESTED_COMPOUND;

        protected static final boolean REFLECTION_READY;
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
            MethodHandle getNestedCompound = null;
            MethodHandle setNestedCompound = null;
            boolean reflectionDone = false;

            try {
                final String craftPath = getCraftBukkitPath();
                final String nmsPath = getItemStackPath();
                final Class<?> craftItemStack = Class.forName(craftPath + ".inventory.CraftItemStack");
                final Class<?> nmsItemStack = Class.forName(nmsPath);
                final Class<?> nbtTagCompound = Class.forName(getNbtTagPath());
                final Class<?> nbtTagBase = Class.forName(getNbtTagBasePath());

                final MethodHandles.Lookup lookup = MethodHandles.lookup();
                final NBTItemTagMappings itemTagName = new NBTItemTagMappings();

                nMSCopyItem = lookup.findStatic(craftItemStack, "asNMSCopy",
                        MethodType.methodType(nmsItemStack, ItemStack.class));
                bukkitCopy = lookup.findStatic(craftItemStack, "asBukkitCopy",
                        MethodType.methodType(ItemStack.class, nmsItemStack));


                hasNBTTag = lookup.findVirtual(nmsItemStack, itemTagName.hasTagName(),
                        MethodType.methodType(boolean.class));
                getNBTTag = lookup.findVirtual(nmsItemStack, itemTagName.getTagName(),
                        MethodType.methodType(nbtTagCompound));
                setNBTTag = lookup.findVirtual(nmsItemStack, itemTagName.setTagName(),
                        MethodType.methodType(void.class, nbtTagCompound));

                if (ItemCreator.getServerVersion() < 13.0)
                    setNestedCompound = lookup.findVirtual(nbtTagCompound, itemTagName.setNestedCompoundName(), MethodType.methodType(void.class, String.class, nbtTagBase));
                else
                    setNestedCompound = lookup.findVirtual(nbtTagCompound, itemTagName.setNestedCompoundName(), MethodType.methodType(nbtTagBase, String.class, nbtTagBase));
                getNestedCompound = lookup.findVirtual(nbtTagCompound, itemTagName.getNestedCompoundName(), MethodType.methodType(nbtTagBase, String.class));

                tagConstructor = nbtTagCompound.getConstructor();
                reflectionDone = true;
            } catch (IllegalAccessException | ClassNotFoundException | NoSuchMethodException e) {
                logger.logError(e, () -> "Failed to initialize all NMS methods needed for modern minecraft.");
            }
            nbtTagConstructor = tagConstructor;
            SET_TAG = setNBTTag;
            GET_TAG = getNBTTag;
            HAS_TAG = hasNBTTag;
            AS_BUKKIT_ITEM_COPY = bukkitCopy;
            NMS_ITEM_COPY = nMSCopyItem;
            GET_NESTED_COMPOUND = getNestedCompound;
            SET_NESTED_COMPOUND = setNestedCompound;
            REFLECTION_READY = reflectionDone;

        }

        NmsItemSession(@Nonnull ItemStack item) {
            this.nmsItemCopy = toNmsItemStack(item);
            this.bukkitItem = item.clone();
        }

        @Override
        public boolean isReady() {
            if (!REFLECTION_READY)
                return false;

            return nmsItemCopy != null;
        }

        @Override
        public boolean hasTag() {
            if (!REFLECTION_READY || nmsItemCopy == null) return false;
            try {
                return (boolean) HAS_TAG.invoke(this.nmsItemCopy);
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to initialize hasTag");
            }
            return false;
        }

        @Override
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

        @Override
        @Nullable
        public CompoundTag getOrCreateCompound() {
            return getOrCreateCompoundTag("", false);
        }

        @Override
        @Nullable
        public CompoundTag getOrCreateCompound(@Nonnull final String name) {
            return getOrCreateCompoundTag(name, true);
        }

        @Override
        @Nullable
        public CompoundTag getCompound() {
            return getCompound("", false);
        }

        @Override
        @Nullable
        public CompoundTag getCompound(@Nonnull final String name) {
            return getCompound(name, true);
        }

        @Override
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
        private CompoundTag getOrCreateCompoundTag(final String name, final boolean usingName) {
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
                return new CompoundTag((nested != null ? nested : root));
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
                return new CompoundTag((nested != null ? nested : root));
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to initialize CompoundTag");
                this.compoundState = CompoundState.ERROR;
            }
            return null;
        }


    }

    /**
     * A per-NBTTagCompound reflective session. Provides low-level access to manipulate
     * properties on the NMS compound.
     *
     * <p>Should generally be used via {@link CompoundTag} rather than directly.</p>
     */
    public static class CompoundSession implements CompoundEditor {
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
        private static final MethodHandle setByteArray;
        private static final MethodHandle getByteArray;
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
            MethodHandle setByteArrayM = null;
            MethodHandle getByteArrayM = null;
            MethodHandle setBooleanM = null;
            MethodHandle getBooleanM = null;
            try {
                final Class<?> nbtTag = Class.forName(getNbtTagPath());
                final MethodHandles.Lookup lookup = MethodHandles.lookup();
                final NBTCompoundMappings compoundName = new NBTCompoundMappings();

                hasTagKey = lookup.findVirtual(nbtTag, compoundName.hasKeyName(),
                        MethodType.methodType(boolean.class, String.class));
                removeM = lookup.findVirtual(nbtTag, compoundName.removeName(),
                        MethodType.methodType(void.class, String.class));


                setIntM = lookup.findVirtual(nbtTag, compoundName.setIntName(),
                        MethodType.methodType(void.class, String.class, int.class));
                getIntM = lookup.findVirtual(nbtTag, compoundName.getIntName(),
                        MethodType.methodType(int.class, String.class));

                setShortM = lookup.findVirtual(nbtTag, compoundName.setShortName(),
                        MethodType.methodType(void.class, String.class, short.class));
                getShortM = lookup.findVirtual(nbtTag, compoundName.getShortName(),
                        MethodType.methodType(short.class, String.class));

                setByteM = lookup.findVirtual(nbtTag, compoundName.setByteName(),
                        MethodType.methodType(void.class, String.class, byte.class));
                getByteM = lookup.findVirtual(nbtTag, compoundName.getByteName(),
                        MethodType.methodType(byte.class, String.class));

                setByteArrayM = lookup.findVirtual(nbtTag, compoundName.setByteArrayName(),
                        MethodType.methodType(void.class, String.class, byte[].class));
                getByteArrayM = lookup.findVirtual(nbtTag, compoundName.getByteArrayName(),
                        MethodType.methodType(byte[].class, String.class));

                setStringM = lookup.findVirtual(nbtTag, compoundName.setStringName(),
                        MethodType.methodType(void.class, String.class, String.class));
                getStringM = lookup.findVirtual(nbtTag, compoundName.getStringName(),
                        MethodType.methodType(String.class, String.class));

                setBooleanM = lookup.findVirtual(nbtTag, compoundName.setBooleanName(),
                        MethodType.methodType(void.class, String.class, boolean.class));
                getBooleanM = lookup.findVirtual(nbtTag, compoundName.getBooleanName(),
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
            setByteArray = setByteArrayM;
            getByteArray = getByteArrayM;
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

        @Override
        @Nonnull
        public Object getHandle() {
            return handle;
        }

        @Override
        public boolean hasKey(@Nonnull final String key) {
            if (hasKey == null) return false;

            try {
                return (boolean) hasKey.invoke(handle, key);
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to check if the compound have the key.");
            }
            return false;
        }

        @Override
        public void remove(@Nonnull final String key) {
            if (remove == null) return;

            try {
                remove.invoke(handle, key);
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to check if the compound have the key.");
            }
        }

        @Override
        public void setInt(@Nonnull final String key, final int value) {
            if (setInt == null) return;

            try {
                setInt.invoke(handle, key, value);
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to set int value from reflection");
            }
        }

        @Override
        public int getInt(@Nonnull final String key) {
            if (getInt == null) return -1;

            try {
                Object intObject = getInt.invoke(handle, key);
                if (intObject == null)
                    return -1;
                return (int) intObject;
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to retrieve int value from reflection");
            }
            return -1;
        }

        @Override
        public void setString(@Nonnull final String key, final String value) {
            if (setString == null) return;

            try {
                setString.invoke(handle, key, value);
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to set string value from reflection");
            }
        }

        @Override
        @Nonnull
        public String getString(@Nonnull final String key) {
            if (getString == null) return "";

            try {
                Object stringObject = getString.invoke(handle, key);
                if (stringObject == null) return "";
                return (String) stringObject;
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to retrieve string value from reflection");
            }
            return "";
        }

        @Override
        public void setByte(@Nonnull final String key, final byte value) {
            if (setByte == null) return;

            try {
                setByte.invoke(handle, key, value);
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to set byte value from reflection");
            }
        }

        @Override
        public byte getByte(@Nonnull final String key) {
            if (getByte == null) return -1;

            try {
                Object byteObject = getByte.invoke(handle, key);
                if (byteObject == null) return -1;
                return (byte) byteObject;
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to retrieve byte value from reflection");
            }
            return -1;
        }

        @Override
        public void setByteArray(@Nonnull final String key, final byte[] value) {
            if (setByteArray == null) return;

            try {
                setByteArray.invoke(handle, key, value);
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to set byte value from reflection");
            }
        }

        @Override
        @Nullable
        public byte[] getByteArray(@Nonnull final String key) {
            if (getByteArray == null) return new byte[0];

            try {
                Object byteArray = getByteArray.invoke(handle, key);
                if (byteArray == null) return null;
                return (byte[]) byteArray;
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to retrieve byte value from reflection");
            }
            return new byte[0];
        }

        @Override
        public void setBoolean(@Nonnull final String key, final boolean value) {
            if (setBoolean == null) return;

            try {
                setBoolean.invoke(handle, key, value);
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to set boolean value from reflection");
            }
        }

        @Override
        public boolean getBoolean(@Nonnull final String key) {
            if (getBoolean == null) return false;

            try {
                Object booleanObject = getBoolean.invoke(handle, key);
                if (booleanObject == null) return false;
                return (boolean) booleanObject;
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to retrieve boolean value from reflection");
            }
            return false;
        }

        @Override
        public void setShort(@Nonnull final String key, final short value) {
            if (setShort == null) return;

            try {
                setShort.invoke(handle, key, value);
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to set short value from reflection");
            }
        }

        @Override
        public short getShort(@Nonnull final String key) {
            if (getShort == null) return -1;

            try {
                Object shortObject = getShort.invoke(handle, key);
                if (shortObject == null) return -1;
                return (short) shortObject;
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to retrieve short value from reflection");
            }
            return -1;
        }
    }

    private static String getNbtTagBasePath() {
        final String nmsPath = getNmsPath();
        if (ItemCreator.getServerVersion() > 16.5)
            return nmsPath + ".nbt.NBTBase";

        return nmsPath + ".NBTBase";
    }

    private static String getCraftBukkitPath() {
        final String packageVersion = getPackageVersion();
        if (packageVersion.isEmpty())
            return "org.bukkit.craftbukkit";
        return "org.bukkit.craftbukkit." + packageVersion;
    }

    private static String getItemStackPath() {
        final String nmsPath = getNmsPath();
        if (ItemCreator.getServerVersion() > 16.5)
            return nmsPath + ".world.item.ItemStack";
        return nmsPath + ".ItemStack";
    }

    private static String getNbtTagPath() {
        final String nmsPath = getNmsPath();
        if (ItemCreator.getServerVersion() > 16.5)
            return nmsPath + ".nbt.NBTTagCompound";

        return nmsPath + ".NBTTagCompound";
    }

    private static String getNmsPath() {
        if (ItemCreator.getServerVersion() > 16.5)
            return "net.minecraft";
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
        if (ItemCreator.getServerVersion() > 19.4)
            return "";
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
