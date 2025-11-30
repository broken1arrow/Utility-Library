package org.broken.arrow.library.itemcreator.utility.nms.mappings;

import org.broken.arrow.library.itemcreator.ItemCreator;

public class NBTItemTagMappings {
    public final String hasTag;
    public final String getTag;
    public final String setTag;

    public final String setNestedCompound;
    public final String getNestedCompound;

    public final String getCompound;

    /**
     * Creates a new instance containing the correct method-name mappings
     * for accessing NBT tags inside the Minecraft ItemStack class,
     * based on the current server version.
     */
    public NBTItemTagMappings() {
        float version = ItemCreator.getServerVersion();

        if (version < 18.0f) {
            hasTag = "hasTag";
            getTag = "getTag";
            setTag = "setTag";

            setNestedCompound = "set";
            getNestedCompound = "get";

            getCompound = "getCompound";
        } else {
            hasTag = "s";
            getTag = "t";
            setTag = "c";

            setNestedCompound = "a";
            getNestedCompound = "c";

            getCompound = "p";
        }
    }

    /**
     * @return the method name used to check whether a tag exists.
     */
    public String hasTagName() {
        return this.hasTag;
    }

    /**
     * @return the method name used to get a tag value.
     */
    public String getTagName() {
        return this.getTag;
    }

    /**
     * @return the method name used to set a tag value.
     */
    public String setTagName() {
        return this.setTag;
    }

    /**
     * @return the method name used to set a nested compound tag.
     */
    public String setNestedCompoundName() {
        return this.setNestedCompound;
    }

    /**
     * @return the method name used to get a nested compound tag.
     */
    public String getNestedCompoundName() {
        return this.getNestedCompound;
    }

    /**
     * @return the method name used to get a compound tag.
     */
    public String getCompoundName() {
        return this.getCompound;
    }
}