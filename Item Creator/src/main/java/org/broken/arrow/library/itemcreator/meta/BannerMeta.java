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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BannerMeta {

    private final List<Pattern> pattern = new ArrayList<>();
    private DyeColor bannerBaseColor;
    private final float serverVersion = ItemCreator.getServerVersion();

    /**
     * Get the base color for the banner (the color before add patterns).
     *
     * @return the color.
     */

    public DyeColor getBannerBaseColor() {
        return bannerBaseColor;
    }

    /**
     * Set the base color for the banner.
     *
     * @param bannerBaseColor the color.
     * @return this class.
     */
    public BannerMeta setBannerBaseColor(final DyeColor bannerBaseColor) {
        this.bannerBaseColor = bannerBaseColor;
        return this;
    }

    /**
     * Add one or several patterns.
     *
     * @param patterns to add to the list.
     * @return this class.
     */
    public BannerMeta addPatterns(final Pattern... patterns) {
        if (patterns == null || patterns.length < 1) return this;

        this.pattern.addAll(Arrays.asList(patterns));
        return this;
    }


    /**
     * Add list of patterns (if it exist old patterns in the list, will the new ones be added on top).
     *
     * @param pattern list some contains patterns.
     * @return this class.
     */
    public BannerMeta addPatterns(final List<Pattern> pattern) {

        this.pattern.addAll(pattern);
        return this;
    }

    /**
     * Get pattern for the banner.
     *
     * @return list of patterns.
     */
    public List<Pattern> getPatterns() {
        return pattern;
    }

    public boolean isBanner(final ItemMeta itemMeta) {
        if(itemMeta instanceof org.bukkit.inventory.meta.BannerMeta)
            return true;
        if (itemMeta instanceof BlockStateMeta) {
            BlockStateMeta blockStateMeta = (BlockStateMeta) itemMeta;
            if (!blockStateMeta.hasBlockState()) return false;
            BlockState blockState = blockStateMeta.getBlockState();
            return blockState instanceof Banner;
        }
        return false;
    }

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
