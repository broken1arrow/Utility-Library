package org.broken.arrow.library.itemcreator.meta;

import org.bukkit.DyeColor;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockState;
import org.bukkit.block.banner.Pattern;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

/**
 * Represents metadata for a shield item, allowing the application of banner patterns
 * to customize the shield's appearance.
 */
public class ShieldMeta {

    private BannerMeta bannerMeta;

    /**
     * Sets the banner pattern on this shield metadata by applying a consumer
     * to a new BannerMeta instance.
     *
     * @param metaConsumer the consumer that configures the BannerMeta patterns and colors
     */
    public void setBannerPattern(@Nonnull final Consumer<BannerMeta> metaConsumer) {
        BannerMeta bannerPattern = new BannerMeta();
        metaConsumer.accept(bannerPattern);
        this.bannerMeta = bannerPattern;
    }

    /**
     * Applies the stored banner pattern to the given ItemMeta if it is a BlockStateMeta
     * representing a Banner. This updates the banner's base color and patterns.
     *
     * @param itemMeta the ItemMeta instance to apply the banner pattern to
     */
    public void applyShieldBanner(@Nonnull final ItemMeta itemMeta) {
        BannerMeta bannerPattern = this.bannerMeta;
        if (bannerPattern == null)
            return;
        final List<Pattern> patternList = bannerPattern.getPatterns();
        final DyeColor baseColor = bannerPattern.getBannerBaseColor();
        if (itemMeta instanceof BlockStateMeta) {
            if (patternList.isEmpty())
                return;

            BlockStateMeta blockStateMeta = (BlockStateMeta) itemMeta;
            if (!blockStateMeta.hasBlockState()) return;

            BlockState blockState = blockStateMeta.getBlockState();

            if (blockState instanceof Banner) {
                Banner banner = ((Banner) blockState);
                banner.setBaseColor(baseColor);
                banner.setPatterns(patternList);
                banner.update();
                ((BlockStateMeta) itemMeta).setBlockState(blockState);
            }
        }
    }

}
