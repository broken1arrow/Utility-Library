package org.broken.arrow.library.itemcreator.meta;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * A handler for managing and applying custom metadata to an {@link ItemStack}.
 * <p>
 * This class acts as a central point for configuring various types of item metadata
 * such as banners, potion effects, enchantments, fireworks, and more. It enables a
 * functional-style setup where metadata components can be configured via lambda consumer
 * before being applied to the associated {@link ItemStack}.
 *
 * <p><strong>Usage example:</strong>
 * <pre>
 * {@code
 * ItemStack item = new ItemStack(Material.FIREWORK_ROCKET);
 * MetaHandler metaHandler = new MetaHandler(item);
 * metaHandler.setFirework(fireworkMeta -> {
 *      fireworkMeta.setFireworkEffect(builder ->
 *              builder.withColor(Color.GREEN));
 *      return metaHandler;
 * }).applyMeta();
 * }
 * </pre>
 */
public class MetaHandler {

    private BannerMeta banner;
    private BottleEffectMeta bottleEffect;
    private EnhancementMeta enhancements;
    private FireworkMeta firework;
    private LeatherMeta leatherMeta;
    private ShieldMeta shieldMeta;

    /**
     * Constructs a new {@code MetaHandler} for the given item.
     */
    public MetaHandler() {
    }


    public BannerMeta getBanner() {
        return banner;
    }

    /**
     * Initializes and configures the banner metadata using the provided function.
     *
     * @param metaFunction a consumer that modifies and returns a value from the {@link BannerMeta}.
     */
    public void setBanner(Consumer<BannerMeta> metaFunction) {
        final BannerMeta bannerData = new BannerMeta();
        metaFunction.accept(bannerData);
        this.banner = bannerData;
    }

    public BottleEffectMeta getBottleEffect() {
        return bottleEffect;
    }

    /**
     * Initializes and configures the potion bottle effect metadata using the provided function.
     *
     * @param metaFunction a consumer that modifies and returns a value from the {@link BottleEffectMeta}.
     */
    public void setBottleEffect(Consumer<BottleEffectMeta> metaFunction) {
        final BottleEffectMeta bottleEffectMeta = new BottleEffectMeta();
        metaFunction.accept(bottleEffectMeta);
        this.bottleEffect = bottleEffectMeta;
    }

    public EnhancementMeta getEnhancements() {
        return enhancements;
    }

    /**
     * Initializes and configures the enchantment metadata using the provided function.
     *
     * @param metaFunction a consumer that modifies and returns a value from the {@link EnhancementMeta}.
     */
    public void setEnhancements(Consumer<EnhancementMeta> metaFunction) {
        final EnhancementMeta enhancementMeta = new EnhancementMeta();
        metaFunction.accept(enhancementMeta);
        this.enhancements = enhancementMeta;
    }

    public FireworkMeta getFirework() {
        return firework;
    }

    /**
     * Initializes and configures the firework metadata using the provided function.
     *
     * @param metaFunction a consumer that modifies and returns a value from the {@link FireworkMeta}.
     */
    public void setFirework(Consumer<FireworkMeta> metaFunction) {
        final FireworkMeta fireworkMeta = new FireworkMeta();
        metaFunction.accept(fireworkMeta);
        this.firework = fireworkMeta;
    }

    public LeatherMeta getLeatherMeta() {
        return leatherMeta;
    }

    /**
     * Initializes and configures the firework metadata using the provided function.
     *
     * @param metaFunction a consumer that modifies and returns a value from the {@link LeatherMeta}.
     */
    public void setLeatherMeta(final Consumer<LeatherMeta> metaFunction) {
        final LeatherMeta leatherData = new LeatherMeta();
        metaFunction.accept(leatherData);
        this.leatherMeta = leatherData;
    }

    /**
     * Initializes and configures the shield metadata using the provided function.
     *
     * @param metaFunction a consumer that modifies and returns a value from the {@link ShieldMeta}.
     */
    public void setShieldMeta(final Consumer<ShieldMeta> metaFunction) {
        final ShieldMeta shieldData = new ShieldMeta();
        metaFunction.accept(shieldData);
        this.shieldMeta = shieldData;
    }

    /**
     * Applies all configured metadata (if any) to the provided {@link ItemMeta},
     * and updates the {@link ItemStack}'s type when necessary (e.g., for banner base colors).
     * <p>
     * This method does <strong>not</strong> apply the metadata to the item directly;
     * it only modifies the {@code itemMeta}. You must call {@link ItemStack#setItemMeta(ItemMeta)}
     * yourself afterward.
     * </p>
     * <p>
     * For certain items like banners, this method may update the {@link ItemStack}'s
     * material type to match the base color. This is relevant in Minecraft 1.13+,
     * where each banner color uses a separate material. If no base color is set,
     * or you're on an older Minecraft version, the item type will remain unchanged.
     * </p>
     * @param itemStack the item whose material may be updated (e.g., for banner colors).
     * @param itemMeta  the metadata instance to modify by applying patterns, effects, or colors.
     */
    public void applyMeta(@Nonnull final ItemStack itemStack,@Nullable final ItemMeta itemMeta) {
        if (itemMeta != null) {
            BannerMeta bannerMeta = this.banner;
            if (bannerMeta != null && bannerMeta.isBanner(itemMeta)) {
                Material materialFromColor = bannerMeta.getBannerMaterialFromColor();
                if (materialFromColor != null && materialFromColor != itemStack.getType())
                    itemStack.setType(materialFromColor);
                bannerMeta.applyBannerPattern(itemMeta);
            }
            if (bottleEffect != null)
                bottleEffect.applyBottleEffects(itemMeta);
            if (enhancements != null)
                enhancements.applyEnchantments(itemMeta);
            if (firework != null)
                firework.applyFireworkEffect(itemMeta);
            if (leatherMeta != null)
                leatherMeta.applyLeatherColor(itemMeta);
            if (shieldMeta != null) {
                shieldMeta.applyShieldBanner(itemMeta);
            }
        }
    }
}
