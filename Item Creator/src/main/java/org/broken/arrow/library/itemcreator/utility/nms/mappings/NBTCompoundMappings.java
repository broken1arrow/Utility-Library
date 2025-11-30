package org.broken.arrow.library.itemcreator.utility.nms.mappings;

import org.broken.arrow.library.itemcreator.ItemCreator;

public class NBTCompoundMappings {
    private final float serverVersion = ItemCreator.getServerVersion();

    private final String hasKey;
    private final String remove;

    private final String setInt;
    private final String getInt;

    private final String setShort;
    private final String getShort;

    private final String setByte;
    private final String getByte;

    private final String setByteArray;
    private final String getByteArray;

    private final String setString;
    private final String getString;

    private final String setBoolean;
    private final String getBoolean;

    /**
     * Creates a new instance containing the correct method-name mappings
     * for working with NBT compound data, based on the current server version.
     */
    public NBTCompoundMappings() {
        if (serverVersion < 18.0f) {
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

    /**
     * @return the method name used to check whether a key exists.
     */
    public String hasKeyName() {
        return this.hasKey;
    }

    /**
     * @return the method name used to remove a key.
     */
    public String removeName() {
        return this.remove;
    }

    /**
     * @return the method name used to set an int value.
     */
    public String setIntName() {
        return this.setInt;
    }

    /**
     * @return the method name used to get an int value.
     */
    public String getIntName() {
        return this.getInt;
    }

    /**
     * @return the method name used to set a short value.
     */
    public String setShortName() {
        return this.setShort;
    }

    /**
     * @return the method name used to get a short value.
     */
    public String getShortName() {
        return this.getShort;
    }

    /**
     * @return the method name used to set a byte value.
     */
    public String setByteName() {
        return this.setByte;
    }

    /**
     * @return the method name used to get a byte value.
     */
    public String getByteName() {
        return this.getByte;
    }

    /**
     * @return the method name used to set a byte array.
     */
    public String setByteArrayName() {
        return this.setByteArray;
    }

    /**
     * @return the method name used to get a byte array.
     */
    public String getByteArrayName() {
        return this.getByteArray;
    }

    /**
     * @return the method name used to set a string value.
     */
    public String setStringName() {
        return this.setString;
    }

    /**
     * @return the method name used to get a string value.
     */
    public String getStringName() {
        return this.getString;
    }

    /**
     * @return the method name used to set a boolean value.
     */
    public String setBooleanName() {
        return this.setBoolean;
    }

    /**
     * @return the method name used to get a boolean value.
     */
    public String getBooleanName() {
        return this.getBoolean;
    }
}