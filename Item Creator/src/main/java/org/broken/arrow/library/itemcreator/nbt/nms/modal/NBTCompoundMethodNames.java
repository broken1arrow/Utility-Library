package org.broken.arrow.library.itemcreator.nbt.nms.modal;

import org.broken.arrow.library.itemcreator.ItemCreator;

/**
 * Provides version-dependent method name mappings for interacting with
 * NBT (Named Binary Tag) compounds in Minecraft's internal (NMS) ItemStack class.
 *
 * <p>This class bridges differences between obfuscated and deobfuscated
 * server versions, where method names change across releases.</p>
 *
 * <p><strong>Version scope:</strong> This mapping applies to Minecraft versions
 * up to and including <b>1.20.4</b>. In newer versions, ItemStack NBT handling
 * is implemented differently and is managed elsewhere in this library.</p>
 *
 * <p>The returned method names are intended for use with reflection or
 * {@link java.lang.invoke.MethodHandle} lookups.</p>
 *
 * <p>This class does not perform any NBT operations itself — it only supplies
 * the correct method names for the current server version.</p>
 */
public class NBTCompoundMethodNames {
    private final static boolean IS_LEGACY_PRE_1_18 = ItemCreator.getVersion().compareTo(18, 0).older();
    private final static boolean IS_AT_LEAST_1_19 = ItemCreator.getVersion().compareTo(19, 0).atLeast();
    private final static boolean IS_NEWER_THAN_1_20 = ItemCreator.getVersion().compareTo(20, 0).newer();

    public final String hasTagMethod;
    public final String getTagMethod;
    public final String setTagMethod;

    public final String setNestedCompound;
    public final String getNestedCompound;

    private final String isEmptyMethod;

    public final String getCompound;


    /**
     * Creates a new instance containing the correct method-name mappings
     * for accessing NBT tags inside the Minecraft ItemStack class,
     * based on the current server version.
     */
    public NBTCompoundMethodNames() {
        isEmptyMethod = "isEmpty";
        if (IS_LEGACY_PRE_1_18) {
            hasTagMethod = "hasTag";
            getTagMethod = "getTag";
            setTagMethod = "setTag";

            setNestedCompound = "set";
            getNestedCompound = "get";

            getCompound = "getCompound";
        } else {
            hasTagMethod = hasTagMethodName();
            getTagMethod = getTagMethodName();
            setTagMethod = "c";

            setNestedCompound = "a";
            getNestedCompound = "c";

            getCompound = "p";
        }
    }

    /**
     * Returns the internal method name used to check whether
     * an NBT compound contains a specific key.
     *
     * @return obfuscated or deobfuscated method name depending on version
     */
    public String hasTagName() {
        return this.hasTagMethod;
    }

    /**
     * Returns the internal method name used to retrieve
     * a value from an NBT compound by key.
     *
     * @return obfuscated or deobfuscated method name depending on version
     */
    public String getTagName() {
        return this.getTagMethod;
    }

    /**
     * Returns the internal method name used to assign
     * a value to an NBT compound by key.
     *
     * @return obfuscated or deobfuscated method name depending on version
     */
    public String setTagName() {
        return this.setTagMethod;
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

    /**
     * Retrieve the empty method name
     *
     * @return the method name used to get a compound tag.
     */
    public String getEmptyName() {
        return isEmptyMethod;
    }

    private String hasTagMethodName() {
        if (IS_NEWER_THAN_1_20) {
            return "u";
        }
        if (IS_AT_LEAST_1_19) {
            return "t";
        }
        return "s";
    }

    private String getTagMethodName() {
        if (IS_NEWER_THAN_1_20) {
            return "v";
        }
        if (IS_AT_LEAST_1_19) {
            return "u";
        }
        return "t";
    }
}