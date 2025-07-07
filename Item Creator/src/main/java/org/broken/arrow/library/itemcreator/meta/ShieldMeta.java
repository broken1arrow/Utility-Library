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

public class ShieldMeta {

    private BannerMeta bannerMeta;

    /**
     * This method just allow you set color on leather.
     *
     * @param metaConsumer the color you want to set on your item.
     */
    public void setBannerPattern(@Nonnull final Consumer<BannerMeta> metaConsumer) {
        BannerMeta bannerMeta = new BannerMeta();
        metaConsumer.accept(bannerMeta);
        this.bannerMeta = bannerMeta;
    }

    public void applyShieldBanner(@Nonnull final ItemMeta itemMeta) {
        BannerMeta bannerData = this.bannerMeta;
        if (bannerData == null)
            return;
        final List<Pattern> patternList = bannerData.getPatterns();
        final DyeColor baseColor = bannerData.getBannerBaseColor();
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
