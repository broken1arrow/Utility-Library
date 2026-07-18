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
import java.util.Optional;
import java.util.logging.Level;

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
    private static final boolean LEGACY_NBT_METHOD_AT_LEAST_12 = ItemCreator.getVersion().compareTo(12, 0).atLeast();

    private static final MethodHandle hasKey;
    private static final MethodHandle remove;
    private static final MethodHandle isEmpty ;
    private static final MethodHandle setString;
    private static final MethodHandle getString;

    private static final MethodHandle setInt;
    private static final MethodHandle getInt;

    private static final MethodHandle setDouble;
    private static final MethodHandle getDouble;

    private static final MethodHandle setLong;
    private static final MethodHandle getLong;

    private static final MethodHandle getShort;
    private static final MethodHandle setShort;

    private static final MethodHandle setByte;
    private static final MethodHandle getByte;

    private static final MethodHandle setByteArray;
    private static final MethodHandle getByteArray;

    private static final MethodHandle setIntArray;
    private static final MethodHandle getIntArray;

    private static final MethodHandle setLongArray;
    private static final MethodHandle getLongArray;

    private static final MethodHandle setBoolean;
    private static final MethodHandle getBoolean;

    static {
        MethodHandle hasTagKey = null;
        MethodHandle removeM = null;
        MethodHandle isEmptyM = null;
        MethodHandle setStringM = null;
        MethodHandle getStringM = null;
        MethodHandle setIntM = null;
        MethodHandle getIntM = null;
        MethodHandle setDoubleM = null;
        MethodHandle getDoubleM = null;
        MethodHandle setLongM = null;
        MethodHandle getLongM = null;
        MethodHandle getShortM = null;
        MethodHandle setShortM = null;
        MethodHandle setByteM = null;
        MethodHandle getByteM = null;
        MethodHandle setByteArrayM = null;
        MethodHandle getByteArrayM = null;
        MethodHandle setIntArrayM = null;
        MethodHandle getIntArrayM = null;
        MethodHandle setLongArrayM = null;
        MethodHandle getLongArrayM = null;
        MethodHandle setBooleanM = null;
        MethodHandle getBooleanM = null;


        try {
            final Class<?> nbtCompound = Class.forName(NbtPathsUtil.getCompoundPackage());
            final MethodHandles.Lookup lookup = MethodHandles.lookup();
            NbtMethodMappings names = NbtMethodMappings.of(LEGACY_NBT_METHOD_NAMES);

            hasTagKey = lookup.findVirtual(nbtCompound, names.hasKey,
                    MethodType.methodType(boolean.class, String.class));

            isEmptyM = lookup.findVirtual(nbtCompound, names.isEmpty,
                    MethodType.methodType(boolean.class));

            removeM = lookup.findVirtual(nbtCompound, names.remove,
                    MethodType.methodType(void.class, String.class));

            setIntM = lookup.findVirtual(nbtCompound, names.setInt,
                    MethodType.methodType(void.class, String.class, int.class));
            getIntM = lookup.findVirtual(nbtCompound, names.getInt,
                    MethodType.methodType(int.class, String.class));

            setDoubleM = lookup.findVirtual(nbtCompound, names.setDouble,
                    MethodType.methodType(void.class, String.class, double.class));
            getDoubleM = lookup.findVirtual(nbtCompound, names.getDouble,
                    MethodType.methodType(double.class, String.class));

            setLongM = lookup.findVirtual(nbtCompound, names.setLong,
                    MethodType.methodType(void.class, String.class, long.class));
            getLongM = lookup.findVirtual(nbtCompound, names.getLong,
                    MethodType.methodType(long.class, String.class));

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

            setIntArrayM = lookup.findVirtual(nbtCompound, names.setIntArray,
                    MethodType.methodType(void.class, String.class, int[].class));
            getIntArrayM = lookup.findVirtual(nbtCompound, names.getIntArray,
                    MethodType.methodType(int[].class, String.class));

            if (LEGACY_NBT_METHOD_AT_LEAST_12) {
                setLongArrayM = lookup.findVirtual(nbtCompound, names.setLongArray,
                        MethodType.methodType(void.class, String.class, long[].class));
                getLongArrayM = lookup.findVirtual(nbtCompound, names.getLongArray,
                        MethodType.methodType(long[].class, String.class));
            }

            setStringM = lookup.findVirtual(nbtCompound, names.setString,
                    MethodType.methodType(void.class, String.class, String.class));
            getStringM = lookup.findVirtual(nbtCompound, names.getString,
                    MethodType.methodType(String.class, String.class));

            setBooleanM = lookup.findVirtual(nbtCompound, names.setBoolean,
                    MethodType.methodType(void.class, String.class, boolean.class));
            getBooleanM = lookup.findVirtual(nbtCompound, names.getBoolean,
                    MethodType.methodType(boolean.class, String.class));

        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            logger.logError(e, () -> "Failed to bind NBT methods for the NBTTagCompound class");
        }

        remove = removeM;
        hasKey = hasTagKey;
        isEmpty = isEmptyM;
        setString = setStringM;
        getString = getStringM;
        setInt = setIntM;
        getInt = getIntM;
        setDouble = setDoubleM;
        getDouble = getDoubleM;
        setLong = setLongM;
        getLong = getLongM;
        getShort = getShortM;
        setShort = setShortM;
        setByte = setByteM;
        getByte = getByteM;
        setByteArray = setByteArrayM;
        getByteArray = getByteArrayM;
        setIntArray = setIntArrayM;
        getIntArray = getIntArrayM;
        setLongArray = setLongArrayM;
        getLongArray = getLongArrayM;
        setBoolean = setBooleanM;
        getBoolean = getBooleanM;
    }


    private final Object handle;

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
    public boolean isEmpty() {
        if (isEmpty == null) return true;
        try {
            return (boolean) isEmpty.invoke(handle);
        } catch (Throwable e) {
            logger.logError(e, () -> "Failed to set int value from reflection");
        }
        return true;
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
    public void setDouble(@Nonnull final String key, final double value) {
        if (setDouble == null) return;

        try {
            setDouble.invoke(handle, key, value);
        } catch (Throwable e) {
            logger.logError(e, () -> "Failed to set double value from reflection");
        }
    }

    @Override
    public double getDouble(@Nonnull final String key) {
        if (getDouble == null) return -1.0;

        try {
            Object intObject = getDouble.invoke(handle, key);
            if (intObject == null)
                return -1.0;
            return (double) intObject;
        } catch (Throwable e) {
            logger.logError(e, () -> "Failed to retrieve double value from reflection");
        }
        return -1.0;
    }

    @Override
    public void setLong(@Nonnull final String key, final long value) {
        if (setLong == null) return;

        try {
            setLong.invoke(handle, key, value);
        } catch (Throwable e) {
            logger.logError(e, () -> "Failed to set long value from reflection");
        }
    }

    @Override
    public long getLong(@Nonnull final String key) {
        if (getLong == null) return -1;

        try {
            Object intObject = getLong.invoke(handle, key);
            if (intObject == null)
                return -1;
            return (long) intObject;
        } catch (Throwable e) {
            logger.logError(e, () -> "Failed to retrieve long value from reflection");
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
    public void setIntArray(String key, int[] value) {
        if (setIntArray == null) return;

        try {
            setIntArray.invoke(handle, key, value);
        } catch (Throwable e) {
            logger.logError(e, () -> "Failed to set int value from reflection");
        }
    }

    @Override
    public void setLongArray(String key, long[] value) {
        if (value == null) return;
        if (setLongArray != null) {
            try {
                setLongArray.invoke(handle, key, value);
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to set long value from reflection");
            }
            return;
        }
        logger.log(Level.WARNING, () -> "Long Array is not supported on this Minecraft version. Saving as Int Array via bit-splitting instead.");
        int[] fallbackArray = new int[value.length * 2];
        for (int i = 0; i < value.length; i++) {
            long val = value[i];
            fallbackArray[i * 2] = (int) (val >> 32);
            fallbackArray[i * 2 + 1] = (int) val;
        }
        this.setIntArray(key, fallbackArray);
    }

    @Override
    public int @NonNull [] getIntArray(String key) {
        if (getIntArray == null) return new int[0];

        try {
            Object intArray = getIntArray.invoke(handle, key);
            if (intArray == null) return new int[0];

            return (int[]) intArray;
        } catch (Throwable e) {
            logger.logError(e, () -> "Failed to retrieve int value from reflection");
        }
        return new int[0];
    }

    @Override
    public long @NonNull [] getLongArray(String key) {
        if (getLongArray != null) {
            try {
                Object longArray = getLongArray.invoke(handle, key);
                if (longArray == null) return new long[0];
                return (long[]) longArray;
            } catch (Throwable e) {
                logger.logError(e, () -> "Failed to retrieve long value from reflection");
            }
            return new long[0];
        }
        logger.log(Level.WARNING, () -> "Long Array is not supported on this Minecraft version. It will try solve it as a Int Array.");
        int[] intArray = this.getIntArray(key);
        if (intArray.length == 0 || intArray.length % 2 != 0) {
            logger.log(Level.WARNING, () -> "This Int Array could not be restored: " + (intArray.length == 0 ? "The array is empty" : "The Array can't be divided by two."));
            return new long[0];
        }
        long[] restoredArray = new long[intArray.length / 2];
        for (int i = 0; i < restoredArray.length; i++) {
            long high = intArray[i * 2];
            long low = intArray[i * 2 + 1];
            restoredArray[i] = (high << 32) | (low & 0xFFFFFFFFL);
        }
        return restoredArray;
    }

    @Override
    public boolean isReady() {
        return hasKey != null && getBoolean != null;
    }

    private final static class NbtMethodMappings {

        private final String hasKey;
        private final String isEmpty;
        private final String remove;

        private final String setInt;
        private final String getInt;

        private final String setDouble;
        private final String getDouble;

        private final String getLong;
        private final String setLong;

        private final String setShort;
        private final String getShort;

        private final String setByte;
        private final String getByte;

        private final String setByteArray;
        private final String getByteArray;

        private final String setLongArray;
        private final String getLongArray;

        private final String getIntArray;
        private final String setIntArray;

        private final String setString;
        private final String getString;

        private final String setBoolean;
        private final String getBoolean;


        private NbtMethodMappings(boolean old) {
            isEmpty = "isEmpty";
            if (old) {
                hasKey = "hasKey";
                remove = "remove";

                setInt = "setInt";
                getInt = "getInt";

                setDouble = "setDouble";
                getDouble = "getDouble";

                setLong = "setLong";
                getLong = "getLong";

                setShort = "setShort";
                getShort = "getShort";

                setByte = "setByte";
                getByte = "getByte";

                setByteArray = "setByteArray";
                getByteArray = "getByteArray";

                setIntArray = "setIntArray";
                getIntArray = "getIntArray";

                setLongArray = "setLongArray";
                getLongArray = "getLongArray";

                setString = "setString";
                getString = "getString";

                setBoolean = "setBoolean";
                getBoolean = "getBoolean";
            } else {
                hasKey = "e";
                remove = "r";

                setInt = "a";
                getInt = "h";

                setDouble = "a";
                getDouble = "j";

                setLong = "a";
                getLong = "i";

                setShort = "a";
                getShort = "g";

                setByte = "a";
                getByte = "f";

                setByteArray = "a";
                getByteArray = "n";

                setIntArray = "a";
                getIntArray = "o";

                setLongArray = "a";
                getLongArray = "m";

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