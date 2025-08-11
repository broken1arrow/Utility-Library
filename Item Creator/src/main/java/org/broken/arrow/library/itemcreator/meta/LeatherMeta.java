package org.broken.arrow.library.itemcreator.meta;

import org.bukkit.Color;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * The leather colors for item type like leather armor.
 */
public class LeatherMeta {

    private ColorMeta colorMeta;

    /**
     * This method just allow you set color on leather.
     *
     * @param metaConsumer the color you want to set on your item.
     */
    public void setLeatherColor(@Nonnull final Consumer<ColorMeta> metaConsumer) {
        ColorMeta colorData = new ColorMeta();
        metaConsumer.accept(colorData);
        this.colorMeta = colorData;
    }

    /**
     * Apply the metadata if it set.
     * @param itemMeta the metadata from the item to modify.
     */
    public void applyLeatherColor(@Nonnull final ItemMeta itemMeta) {
        ColorMeta color = this.colorMeta;
        if (color == null || !color.isColorSet())
            return;

        if (itemMeta instanceof LeatherArmorMeta) {
            final LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemMeta;
            leatherArmorMeta.setColor(Color.fromBGR(color.getBlue(), color.getGreen(), color.getRed()));
        }
    }

}
