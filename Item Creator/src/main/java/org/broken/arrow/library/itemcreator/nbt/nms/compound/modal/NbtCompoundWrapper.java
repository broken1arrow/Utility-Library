package org.broken.arrow.library.itemcreator.nbt.nms.compound.modal;

import org.broken.arrow.library.itemcreator.ItemCreator;
import org.broken.arrow.library.itemcreator.nbt.nms.utily.NbtPathsUtil;
import org.broken.arrow.library.logging.Logging;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * Provides a version-independent wrapper for interacting with Minecraft's
 * internal NBT compound implementation.
 *
 * <p>This class abstracts differences between Minecraft versions by binding
 * required NBT compound methods through {@link MethodHandle}s. Older versions
 * use unobfuscated method names, while newer versions use their remapped
 * internal names.</p>
 *
 * <p>The wrapper provides access to common NBT operations such as reading,
 * writing, and removing primitive values without exposing version-specific
 * NMS classes to callers.</p>
 *
 * <p>The wrapped handle represents the internal NBT compound instance. For
 * Minecraft versions 1.20.5 and newer, this may represent the internal
 * {@code CustomData} storage rather than a direct {@code NBTTagCompound}.</p>
 *
 * <p>This class is intended for internal library usage.</p>
 */
public class NbtCompoundWrapper implements NbtCompoundAccessor {
    private static final Logging logger = new Logging(NbtCompoundWrapper.class);
    private static final boolean LEGACY_NBT_METHOD_NAMES = ItemCreator.getVersion().compareTo(18, 0).older();

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
            final Class<?> nbtCompound = Class.forName(NbtPathsUtil.getCompoundPackage());
            final MethodHandles.Lookup lookup = MethodHandles.lookup();
            NbtMethodMappings names = NbtMethodMappings.of(LEGACY_NBT_METHOD_NAMES);

            hasTagKey = lookup.findVirtual(nbtCompound, names.hasKey,
                    MethodType.methodType(boolean.class, String.class));

            removeM = lookup.findVirtual(nbtCompound, names.remove,
                    MethodType.methodType(void.class, String.class));

            setIntM = lookup.findVirtual(nbtCompound, names.setInt,
                    MethodType.methodType(void.class, String.class, int.class));
            getIntM = lookup.findVirtual(nbtCompound, names.getInt,
                    MethodType.methodType(int.class, String.class));

            setShortM = lookup.findVirtual(nbtCompound, names.setShort,
                    MethodType.methodType(void.class, String.class, short.class));
            getShortM = lookup.findVirtual(nbtCompound, names.getShort,
                    MethodType.methodType(short.class, String.class));

            setByteM = lookup.findVirtual(nbtCompound, names.setByte,
                    MethodType.methodType(void.class, String.class, byte.class));
            getByteM = lookup.findVirtual(nbtCompound, names.getByte,
                    MethodType.methodType(byte.class, String.class));

            setByteArrayM = lookup.findVirtual(nbtCompound, names.setByteArray,
                    MethodType.methodType(void.class, String.class, byte[].class));
            getByteArrayM = lookup.findVirtual(nbtCompound, names.getByteArray,
                    MethodType.methodType(byte[].class, String.class));

            setStringM = lookup.findVirtual(nbtCompound, names.setString,
                    MethodType.methodType(void.class, String.class, String.class));
            getStringM = lookup.findVirtual(nbtCompound, names.getString,
                    MethodType.methodType(String.class, String.class));

            setBooleanM = lookup.findVirtual(nbtCompound, names.setBoolean,
                    MethodType.methodType(void.class, String.class, boolean.class));
            getBooleanM = lookup.findVirtual(nbtCompound, names.getBoolean,
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

    /**
     * Creates an adapter around an internal Minecraft NBT compound instance.
     *
     * <p>The adapter uses the version-specific method bindings initialized during
     * class loading to provide access to NBT operations.</p>
     *
     * <p>This constructor is intended for internal library usage. Creating an
     * instance with an incompatible handle type may result in invocation errors.</p>
     *
     * @param handle the internal NBT compound instance
     */
    public NbtCompoundWrapper(Object handle) {
        this.handle = handle;
    }

    @Override
    public @NonNull Object getHandle() {
        return this.handle;
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

    @Override
    public boolean isReady() {
        return hasKey != null && getBoolean != null;
    }

    private final static class NbtMethodMappings {

        public final String hasKey;
        public final String remove;

        public final String setInt;
        public final String getInt;

        public final String setShort;
        public final String getShort;

        public final String setByte;
        public final String getByte;

        public final String setByteArray;
        public final String getByteArray;

        public final String setString;
        public final String getString;

        public final String setBoolean;
        public final String getBoolean;

        private NbtMethodMappings(boolean old) {
            if (old) {
                hasKey = "hasKey";
                remove = "remove";

                setInt = "setInt";
                getInt = "getInt";

                setShort = "setShort";
                getShort = "getShort";

                setByte = "setByte";
                getByte = "getByte";

                setByteArray = "setByteArray";
                getByteArray = "getByteArray";

                setString = "setString";
                getString = "getString";

                setBoolean = "setBoolean";
                getBoolean = "getBoolean";
            } else {
                hasKey = "e";
                remove = "r";

                setInt = "a";
                getInt = "h";

                setShort = "a";
                getShort = "g";

                setByte = "a";
                getByte = "f";

                setByteArray = "a";
                getByteArray = "m";

                setString = "a";
                getString = "l";

                setBoolean = "a";
                getBoolean = "q";
            }
        }

        public static NbtMethodMappings of(boolean oldVersion) {
            return new NbtMethodMappings(oldVersion);
        }
    }
}