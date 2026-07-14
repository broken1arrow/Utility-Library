package org.broken.arrow.library.itemcreator.nbt.nms.compound.modal.v_21;

import org.broken.arrow.library.itemcreator.nbt.nms.compound.modal.NbtCompoundAccessor;
import org.broken.arrow.library.itemcreator.nbt.nms.utily.NbtPathsUtil;
import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.logging.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;
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
public class ModernCompoundWrapper implements NbtCompoundAccessor {
    private static final Logging logger = new Logging(ModernCompoundWrapper.class);

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
            final Class<?> nbtTag = NbtPathsUtil.getTagInterface();
            final MethodHandles.Lookup lookup = MethodHandles.lookup();

            hasTagKey = lookup.findVirtual(nbtCompound, "contains",
                    MethodType.methodType(boolean.class, String.class));

            removeM = lookup.findVirtual(nbtCompound, "remove",
                    MethodType.methodType(nbtTag, String.class));

            setIntM = lookup.findVirtual(nbtCompound, "putInt",
                    MethodType.methodType(void.class, String.class, int.class));
            getIntM = lookup.findVirtual(nbtCompound, "getIntOr",
                    MethodType.methodType(int.class, String.class, int.class));

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
    public ModernCompoundWrapper(Object handle) {
        if (handle instanceof Optional<?>) {
            this.handle = ((Optional<?>) handle).orElse(null);
            Validate.checkNotNull(this.handle,"The NBT compound handle can't be null");
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
            Object intObject = getInt.invoke(handle, key, -1);
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
            Object stringObject = getString.invoke(handle, key, "");
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
            Object byteObject = getByte.invoke(handle, key, (byte) -1);
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
            if (byteArray instanceof Optional)
                return ((Optional<byte[]>) byteArray).orElse(new byte[0]);
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
            Object booleanObject = getBoolean.invoke(handle, key, false);
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
            Object shortObject = getShort.invoke(handle, key, (short) -1);
            if (shortObject == null) return -1;
            return (short) shortObject;
        } catch (Throwable e) {
            logger.logError(e, () -> "Failed to retrieve short value from reflection");
        }
        return -1;
    }

    @Override
    public boolean isReady() {
        return remove != null && getBoolean != null;
    }

}
