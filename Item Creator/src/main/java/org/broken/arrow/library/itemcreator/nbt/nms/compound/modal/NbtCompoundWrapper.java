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
public class NbtCompoundWrapper extends CompoundWrapper {
    private static final Logging logger = new Logging(NbtCompoundWrapper.class);
    private static final boolean LEGACY_NBT_METHOD_NAMES = ItemCreator.getVersion().compareTo(18, 0).older();
    private static final boolean LEGACY_NBT_METHOD_AT_LEAST_12 = ItemCreator.getVersion().compareTo(12, 0).atLeast();

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
        super(handle);
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