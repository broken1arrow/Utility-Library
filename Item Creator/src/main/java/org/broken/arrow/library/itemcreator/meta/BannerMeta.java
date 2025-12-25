package org.broken.arrow.library.itemcreator.meta;

import org.broken.arrow.library.itemcreator.ItemCreator;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockState;
import org.bukkit.block.banner.Pattern;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents metadata for a banner item, including its base color and
 * decorative patterns.
 * <p>
 * This class provides methods to get and set the banner's base color (the
 * solid background color), manage its patterns, and apply these properties
 * to Bukkit's {@link ItemMeta} instances.
 * </p>
 * <p>
 * It also includes compatibility handling for different Minecraft server
 * versions, especially regarding how base colors and materials are managed.
 * </p>
 */
public class BannerMeta {

    private final List<Pattern> pattern = new ArrayList<>();
    private DyeColor bannerBaseColor;
    private final double serverVersion = ItemCreator.getServerVersion();

    /**
     * Gets the base color of the banner, which is the background color
     * before any patterns are applied.
     *
     * @return the base {@link DyeColor} of the banner, or null if not set.
     */
    @Nullable
    public DyeColor getBannerBaseColor() {
        return bannerBaseColor;
    }

    /**
     * Sets the base color of the banner.
     * <p>
     * On servers running Minecraft 1.13 or later, the base color is determined
     * by the banner's material type. In those versions, this value is used only
     * to select the correct banner material, this method allows you
     * to use the same logic regardless of server version.
     * </p>
     *
     * @param bannerBaseColor the {@link DyeColor} to set as the base color
     * @return this {@code BannerMeta} instance for chaining
     */
    public BannerMeta setBannerBaseColor(final DyeColor bannerBaseColor) {
        this.bannerBaseColor = bannerBaseColor;
        return this;
    }

    /**
     * Adds one or more patterns to the banner.
     * <p>
     * New patterns are appended to the existing list.
     * </p>
     *
     * @param patterns one or more {@link Pattern}s to add.
     * @return this {@code BannerMeta} instance for chaining.
     */
    public BannerMeta addPatterns(final Pattern... patterns) {
        if (patterns == null || patterns.length < 1) return this;

        this.pattern.addAll(Arrays.asList(patterns));
        return this;
    }

    /**
     * Adds a list of patterns to the banner.
     * <p>
     * New patterns are appended to the existing list.
     * </p>
     *
     * @param patterns list of {@link Pattern}s to add.
     * @return this {@code BannerMeta} instance for chaining.
     */
    public BannerMeta addPatterns(final List<Pattern> patterns) {

        this.pattern.addAll(patterns);
        return this;
    }

    /**
     * Returns an unmodifiable list of patterns currently set on the banner.
     *
     * @return the list of banner {@link Pattern}s.
     */
    public List<Pattern> getPatterns() {
        return pattern;
    }

    /**
     * Checks if the provided {@link ItemMeta} represents a banner.
     * <p>
     * This supports both direct {@link org.bukkit.inventory.meta.BannerMeta}
     * instances and block state metas that wrap a {@link Banner} block.
     * </p>
     *
     * @param itemMeta the item meta to check.
     * @return {@code true} if the meta represents a banner; {@code false} otherwise.
     */
    public boolean isBanner(final ItemMeta itemMeta) {
        if (itemMeta instanceof org.bukkit.inventory.meta.BannerMeta)
            return true;
        if (itemMeta instanceof BlockStateMeta) {
            BlockStateMeta blockStateMeta = (BlockStateMeta) itemMeta;
            if (!blockStateMeta.hasBlockState()) return false;
            BlockState blockState = blockStateMeta.getBlockState();
            return blockState instanceof Banner;
        }
        return false;
    }

    /**
     * Applies this banner's patterns and base color to the provided
     * {@link ItemMeta} instance.
     * <p>
     * For server versions prior to 1.13, the base color is also set explicitly.
     * </p>
     *
     * @param itemMeta the {@link ItemMeta} to apply the banner data to.
     */
    public void applyBannerPattern(@Nonnull final ItemMeta itemMeta) {
        List<Pattern> patternList = getPatterns();
        if (patternList == null || patternList.isEmpty())
            return;

        if (itemMeta instanceof org.bukkit.inventory.meta.BannerMeta) {
            final org.bukkit.inventory.meta.BannerMeta bannerMeta = (org.bukkit.inventory.meta.BannerMeta) itemMeta;
            bannerMeta.setPatterns(patternList);
            if (serverVersion < 13.0) {
                bannerMeta.setBaseColor(this.bannerBaseColor);
            }
        }
    }

    /**
     * Gets the banner material corresponding to the current base color.
     * <p>
     * This is only supported on server versions 1.13 and newer.
     * </p>
     *
     * @return the {@link Material} matching the base color banner, or
     *         {@code null} if the server version is below 1.13 or base color is null.
     */
    public Material getBannerMaterialFromColor() {
        if (serverVersion < 13.0)
            return null;
        final DyeColor color = this.getBannerBaseColor();
        if (color == null)
            return null;

        switch (color) {
            case WHITE:
                return Material.WHITE_BANNER;
            case ORANGE:
                return Material.ORANGE_BANNER;
            case MAGENTA:
                return Material.MAGENTA_BANNER;
            case LIGHT_BLUE:
                return Material.LIGHT_BLUE_BANNER;
            case YELLOW:
                return Material.YELLOW_BANNER;
            case LIME:
                return Material.LIME_BANNER;
            case PINK:
                return Material.PINK_BANNER;
            case GRAY:
                return Material.GRAY_BANNER;
            case LIGHT_GRAY:
                return Material.LIGHT_GRAY_BANNER;
            case CYAN:
                return Material.CYAN_BANNER;
            case PURPLE:
                return Material.PURPLE_BANNER;
            case BLUE:
                return Material.BLUE_BANNER;
            case BROWN:
                return Material.BROWN_BANNER;
            case GREEN:
                return Material.GREEN_BANNER;
            case RED:
                return Material.RED_BANNER;
            case BLACK:
                return Material.BLACK_BANNER;
            default:
                return Material.WHITE_BANNER;
        }
    }

}
