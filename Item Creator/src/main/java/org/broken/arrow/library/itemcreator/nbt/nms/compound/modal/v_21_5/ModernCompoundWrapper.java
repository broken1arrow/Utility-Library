package org.broken.arrow.library.itemcreator.nbt.nms.compound.modal.v_21_5;

import org.broken.arrow.library.itemcreator.nbt.nms.compound.modal.CompoundWrapper;
import org.broken.arrow.library.itemcreator.nbt.nms.utily.NbtPathsUtil;
import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.logging.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Optional;

/**
 * Provides a version-independent adapter for interacting with Minecraft's
 * internal NBT compound implementation.
 *
 * <p>This class abstracts differences between Minecraft versions by binding
 * the required NBT compound methods through {@link MethodHandle}s. Older
 * versions use unobfuscated method names, while newer versions use their
 * remapped internal names.</p>
 *
 * <p>The adapter supports reading, writing, and removing primitive NBT values
 * without exposing version-specific NMS classes to callers.</p>
 *
 * <p>The wrapped handle represents an internal NBT compound instance. For
 * Minecraft versions 1.20.5 and newer, this may be the internal
 * {@code CustomData} compound representation rather than a direct
 * {@code NBTTagCompound} instance.</p>
 *
 * <p>This class is intended for internal use by the library and should not
 * normally be instantiated directly.</p>
 */
public class ModernCompoundWrapper extends CompoundWrapper {
    private static final Logging logger = new Logging(ModernCompoundWrapper.class);

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
                    MethodType.methodType(NbtPathsUtil.getTagInterfaceOrVoid(), String.class));

            isEmptyM = lookup.findVirtual(nbtCompound, "isEmpty",
                    MethodType.methodType(boolean.class));

            setIntM = lookup.findVirtual(nbtCompound, "putInt",
                    MethodType.methodType(void.class, String.class, int.class));
            getIntM = lookup.findVirtual(nbtCompound, "getIntOr",
                    MethodType.methodType(int.class, String.class, int.class));

            setDoubleM = lookup.findVirtual(nbtCompound, "putDouble",
                    MethodType.methodType(void.class, String.class, double.class));
            getDoubleM = lookup.findVirtual(nbtCompound, "getDoubleOr",
                    MethodType.methodType(double.class, String.class, double.class));

            setLongM = lookup.findVirtual(nbtCompound, "putLong",
                    MethodType.methodType(void.class, String.class, long.class));
            getLongM = lookup.findVirtual(nbtCompound, "getLongOr",
                    MethodType.methodType(long.class, String.class, long.class));

            setShortM = lookup.findVirtual(nbtCompound, "putShort",
                    MethodType.methodType(void.class, String.class, short.class));
            getShortM = lookup.findVirtual(nbtCompound, "getShortOr",
                    MethodType.methodType(short.class, String.class, short.class));

            setByteM = lookup.findVirtual(nbtCompound, "putByte",
                    MethodType.methodType(void.class, String.class, byte.class));
            getByteM = lookup.findVirtual(nbtCompound, "getByteOr",
                    MethodType.methodType(byte.class, String.class, byte.class));

            setByteArrayM = lookup.findVirtual(nbtCompound, "putByteArray",
                    MethodType.methodType(void.class, String.class, byte[].class));
            getByteArrayM = lookup.findVirtual(nbtCompound, "getByteArray",
                    MethodType.methodType(Optional.class, String.class));

            setIntArrayM = lookup.findVirtual(nbtCompound, "putIntArray",
                    MethodType.methodType(void.class, String.class, int[].class));
            getIntArrayM = lookup.findVirtual(nbtCompound, "getIntArray",
                    MethodType.methodType(Optional.class, String.class));

            setLongArrayM = lookup.findVirtual(nbtCompound, "putLongArray",
                    MethodType.methodType(void.class, String.class, long[].class));
            getLongArrayM = lookup.findVirtual(nbtCompound, "getLongArray",
                    MethodType.methodType(Optional.class, String.class));

            setStringM = lookup.findVirtual(nbtCompound, "putString",
                    MethodType.methodType(void.class, String.class, String.class));
            getStringM = lookup.findVirtual(nbtCompound, "getStringOr",
                    MethodType.methodType(String.class, String.class, String.class));

            setBooleanM = lookup.findVirtual(nbtCompound, "putBoolean",
                    MethodType.methodType(void.class, String.class, boolean.class));
            getBooleanM = lookup.findVirtual(nbtCompound, "getBooleanOr",
                    MethodType.methodType(boolean.class, String.class, boolean.class));

        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            logger.logError(e, () -> "Failed to bind NBT methods");
        }
        remove = removeM;
        hasKey = hasTagKey;
        isEmpty = isEmptyM;
        setString = setStringM;
        getString = getStringM;
        setDouble = setDoubleM;
        getDouble = getDoubleM;
        setLong = setLongM;
        getLong = getLongM;
        setInt = setIntM;
        getInt = getIntM;
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
    public ModernCompoundWrapper(Object handle) {
        super(handle);
    }

    @Override
    @Nullable
    public byte[] getByteArray(@Nonnull final String key) {
        if (getByteArray == null) return new byte[0];

        try {
            Object byteArray = getByteArray.invoke(handle, key);
            if (byteArray == null) return null;
            if (byteArray instanceof Optional)
                return ((Optional<byte[]>) byteArray).orElse(new byte[0]);
            if (byteArray instanceof byte[])
                return (byte[]) byteArray;
        } catch (Throwable e) {
            logger.logError(e, () -> "Failed to retrieve byte value from reflection");
        }
        return new byte[0];
    }

    @Override
    public void setIntArray(String key, int[] value) {
        if (setIntArray == null) return;

        try {
            setIntArray.invoke(handle, key, value);
        } catch (Throwable e) {
            logger.logError(e, () -> "Failed to set byte value from reflection");
        }
    }

    @Override
    public void setLongArray(String key, long[] value) {
        if (setLongArray == null) return;

        try {
            setLongArray.invoke(handle, key, value);
        } catch (Throwable e) {
            logger.logError(e, () -> "Failed to set byte value from reflection");
        }
    }

    @Override
    public int @NonNull [] getIntArray(String key) {
        if (getIntArray == null) return new int[0];

        try {
            Object intArray = getIntArray.invoke(handle, key);
            if (intArray == null) return new int[0];
            if (intArray instanceof Optional)
                return ((Optional<int[]>) intArray).orElse(new int[0]);
            if (intArray instanceof int[]) {
                return (int[]) intArray;
            }
        } catch (Throwable e) {
            logger.logError(e, () -> "Failed to retrieve int value from reflection");
        }
        return new int[0];
    }

    @Override
    public long @NonNull [] getLongArray(String key) {
        if (getLongArray == null) return new long[0];

        try {
            Object longArray = getLongArray.invoke(handle, key);
            if (longArray == null) return new long[0];
            if (longArray instanceof Optional)
                return ((Optional<long[]>) longArray).orElse(new long[0]);
            if (longArray instanceof long[]) {
                return (long[]) longArray;
            }
        } catch (Throwable e) {
            logger.logError(e, () -> "Failed to retrieve long value from reflection");
        }
        return new long[0];
    }

    @Override
    public boolean isReady() {
        return remove != null && getBoolean != null;
    }


}
