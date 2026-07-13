package org.broken.arrow.library.itemcreator.utility.nbt.nms.modal;

import org.broken.arrow.library.itemcreator.utility.nbt.nms.compound.CompoundTag;
import org.broken.arrow.library.itemcreator.utility.nbt.nms.api.CompoundEditor;
import org.broken.arrow.library.itemcreator.utility.nbt.nms.mappings.NBTCompoundMappings;
import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.logging.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static org.broken.arrow.library.itemcreator.utility.nbt.nms.modal.NBTLegacyAdapter.getNbtTagPath;

/**
 * A per-NBTTagCompound reflective session. Provides low-level access to manipulate
 * properties on the NMS compound.
 *
 * <p>Should generally be used via {@link CompoundTag} rather than directly.</p>
 */
public class CompoundSession implements CompoundEditor {
    private static final Logging logger = new Logging(CompoundSession.class);

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

    public CompoundSession(@Nonnull final Object handle) {
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
