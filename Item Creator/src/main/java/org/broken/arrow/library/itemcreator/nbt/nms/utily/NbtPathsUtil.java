package org.broken.arrow.library.itemcreator.nbt.nms.utily;

import org.broken.arrow.library.itemcreator.ItemCreator;
import org.bukkit.Bukkit;

public class NbtPathsUtil {
    private static final boolean IS_NEVER_16 = ItemCreator.getVersion().compareTo(16, 5).newer();
    private final static boolean IS_AT_LEAST_21_11 = ItemCreator.getVersion().compareTo(21, 11).atLeast();

    /**
     * Retrieve the compound path
     *
     * @return the path to the compound class.
     */
    public static String getCompoundPackage() {
        final String nmsPath = getNmsPath();

        if (IS_NEVER_16) {
            if (ItemCreator.getVersion().versionNewer(20.4))
                return nmsPath + ".nbt.CompoundTag";
            return nmsPath + ".nbt.NBTTagCompound";
        }
        return nmsPath + ".NBTTagCompound";
    }

    /**
     * Get the tag interface for modern Minecraft
     *
     * @return returns the Tag interface.
     * @throws ClassNotFoundException if it not find the class.
     * @
     */
    public static Class<?> getTagInterface() throws ClassNotFoundException {
        return Class.forName("net.minecraft.nbt.Tag");
    }

    /**
     * Get the tag interface for modern Minecraft or void on older versions
     * than 1.21.11.
     *
     * @return returns the Tag interface.
     * @throws ClassNotFoundException if it not find the class.
     * @
     */
    public static Class<?> getTagInterfaceOrVoid() throws ClassNotFoundException {
        return IS_AT_LEAST_21_11 ? Class.forName("net.minecraft.nbt.Tag") : void.class;
    }

    private static String getNmsPath() {
        if (IS_NEVER_16)
            return "net.minecraft";
        return "net.minecraft.server." + getPackageVersion();
    }


    /**
     * Extracts the version identifier from the Bukkit server package.
     * This version will only work on legacy, as the path changed in newer
     * Minecraft versions.
     * Example: v1_8_R3
     *
     * @return it returns for example v1_8_R3
     */
    private static String getPackageVersion() {
        if (ItemCreator.getVersion().versionNewer(20.4))
            return "";
        return Bukkit.getServer().getClass().toGenericString().split("\\.")[3];
    }

}
