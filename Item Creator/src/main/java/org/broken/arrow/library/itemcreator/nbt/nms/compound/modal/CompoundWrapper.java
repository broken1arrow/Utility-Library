package org.broken.arrow.library.itemcreator.nbt.nms.compound.modal;

import java.lang.invoke.MethodHandle;

public abstract class CompoundWrapper implements NbtCompoundAccessor {

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

}
