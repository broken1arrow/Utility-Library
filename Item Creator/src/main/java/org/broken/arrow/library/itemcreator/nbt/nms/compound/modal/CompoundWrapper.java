package org.broken.arrow.library.itemcreator.nbt.nms.compound.modal;

import org.broken.arrow.library.itemcreator.nbt.nms.compound.modal.v_21.ModernCompoundWrapperTwentyOne;
import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.logging.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.util.Optional;
import java.util.logging.Level;

public abstract class CompoundWrapper implements NbtCompoundAccessor {
    private static final Logging logger = new Logging(CompoundWrapper.class);

    protected static MethodHandle hasKey;
    protected static MethodHandle remove;
    protected static MethodHandle isEmpty;

    protected static MethodHandle setString;
    protected static MethodHandle getString;

    protected static MethodHandle setInt;
    protected static MethodHandle getInt;

    protected static MethodHandle setDouble;
    protected static MethodHandle getDouble;

    protected static MethodHandle setLong;
    protected static MethodHandle getLong;

    protected static MethodHandle getShort;
    protected static MethodHandle setShort;

    protected static MethodHandle setByte;
    protected static MethodHandle getByte;

    protected static MethodHandle setByteArray;
    protected static MethodHandle getByteArray;

    protected static MethodHandle setIntArray;
    protected static MethodHandle getIntArray;

    protected static MethodHandle setLongArray;
    protected static MethodHandle getLongArray;

    protected static MethodHandle setBoolean;
    protected static MethodHandle getBoolean;

    protected final Object handle;

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
    protected CompoundWrapper(Object handle) {
        if (handle instanceof Optional<?>) {
            this.handle = ((Optional<?>) handle).orElse(null);
            Validate.checkNotNull(this.handle, "The NBT compound handle can't be null");
        } else {
            this.handle = handle;
        }
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
            logger.logError(e, () -> "Failed to check if the compound have the key.");
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

    @Override
    public boolean isReady() {
        return hasKey != null && getBoolean != null;
    }

}
