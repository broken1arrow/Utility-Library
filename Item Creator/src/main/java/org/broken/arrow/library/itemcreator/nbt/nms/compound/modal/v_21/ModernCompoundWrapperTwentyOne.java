package org.broken.arrow.library.itemcreator.nbt.nms.compound.modal.v_21;

import org.broken.arrow.library.itemcreator.nbt.nms.compound.modal.CompoundWrapper;
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
public class ModernCompoundWrapperTwentyOne extends CompoundWrapper {
    private static final Logging logger = new Logging(ModernCompoundWrapperTwentyOne.class);

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


            hasTagKey = lookup.findVirtual(nbtCompound, "contains",
                    MethodType.methodType(boolean.class, String.class));

            removeM = lookup.findVirtual(nbtCompound, "remove",
                    MethodType.methodType(void.class, String.class));

            isEmptyM = lookup.findVirtual(nbtCompound, "isEmpty",
                    MethodType.methodType(boolean.class));

            setIntM = lookup.findVirtual(nbtCompound,  "putInt",
                    MethodType.methodType(void.class, String.class, int.class));
            getIntM = lookup.findVirtual(nbtCompound, "getInt",
                    MethodType.methodType(int.class, String.class));

            setDoubleM = lookup.findVirtual(nbtCompound, "putDouble",
                    MethodType.methodType(void.class, String.class, double.class));
            getDoubleM = lookup.findVirtual(nbtCompound, "getDouble",
                    MethodType.methodType(double.class, String.class));

            setLongM = lookup.findVirtual(nbtCompound, "putLong",
                    MethodType.methodType(void.class, String.class, long.class));
            getLongM = lookup.findVirtual(nbtCompound, "getLong",
                    MethodType.methodType(long.class, String.class));

            setShortM = lookup.findVirtual(nbtCompound, "putShort",
                    MethodType.methodType(void.class, String.class, short.class));
            getShortM = lookup.findVirtual(nbtCompound,  "getShort",
                    MethodType.methodType(short.class, String.class));

            setByteM = lookup.findVirtual(nbtCompound,  "putByte",
                    MethodType.methodType(void.class, String.class, byte.class));
            getByteM = lookup.findVirtual(nbtCompound, "getByte",
                    MethodType.methodType(byte.class, String.class));

            setByteArrayM = lookup.findVirtual(nbtCompound, "putByteArray",
                    MethodType.methodType(void.class, String.class, byte[].class));
            getByteArrayM = lookup.findVirtual(nbtCompound, "getByteArray",
                    MethodType.methodType(byte[].class, String.class));

            setIntArrayM = lookup.findVirtual(nbtCompound, "putIntArray",
                    MethodType.methodType(void.class, String.class, int[].class));
            getIntArrayM = lookup.findVirtual(nbtCompound, "getIntArray",
                    MethodType.methodType(int[].class, String.class));


            setLongArrayM = lookup.findVirtual(nbtCompound, "putLongArray",
                    MethodType.methodType(void.class, String.class, long[].class));
            getLongArrayM = lookup.findVirtual(nbtCompound, "getLongArray",
                    MethodType.methodType(long[].class, String.class));


            setStringM = lookup.findVirtual(nbtCompound, "putString",
                    MethodType.methodType(void.class, String.class, String.class));
            getStringM = lookup.findVirtual(nbtCompound, "getString",
                    MethodType.methodType(String.class, String.class));

            setBooleanM = lookup.findVirtual(nbtCompound, "putBoolean",
                    MethodType.methodType(void.class, String.class, boolean.class));
            getBooleanM = lookup.findVirtual(nbtCompound, "getBoolean",
                    MethodType.methodType(boolean.class, String.class));

        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            logger.logError(e, () -> "Failed to bind NBT methods");
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
    public ModernCompoundWrapperTwentyOne(Object handle) {
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
        logger.log(Level.WARNING, () -> "Long Array is not supported on this Minecraft version. It will try to solve it as a Int Array.");
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

}