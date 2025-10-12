package org.broken.arrow.library.itemcreator.meta;

import org.broken.arrow.library.itemcreator.meta.enhancement.EnhancementMeta;
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
    private MapWrapperMeta mapMeta;
    private BookMeta bookMeta;

    /**
     * Retrieve the banner meta set.
     *
     * @return the banner set or null if not set yet.
     */
    @Nullable
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

    /**
     * Initializes and configures the banner metadata by using
     * {@link BannerMeta} class.
     *
     * @return the {@link BannerMeta} where you set the properties for your banner.
     */
    public BannerMeta createBannerMeta() {
        this.banner = new BannerMeta();
        return this.banner;
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

    /**
     * Initializes and configures the potion bottle effect metadata by using
     * {@link BottleEffectMeta} class.
     *
     * @return the {@link BottleEffectMeta} where you set the properties for your potion.
     */
    public BottleEffectMeta createBottleEffectMeta() {
        this.bottleEffect = new BottleEffectMeta();
        return this.bottleEffect;
    }

    /**
     * Retrieve the set {@link BottleEffectMeta}.
     *
     * @return instance of the class or {@code null} if not set.
     */
    @Nullable
    public BottleEffectMeta getBottleEffect() {
        return bottleEffect;
    }


    /**
     * Initializes and configures the enchantment metadata using the provided function.
     *
     * <p>This method is useful when you want to configure {@link EnhancementMeta} inline
     * using a lambda expression.</p>
     *
     * @param metaFunction a consumer that accepts and modifies the {@link EnhancementMeta} instance.
     */
    public void setEnhancements(Consumer<EnhancementMeta> metaFunction) {
        final EnhancementMeta enhancementMeta = new EnhancementMeta();
        metaFunction.accept(enhancementMeta);
        this.enhancements = enhancementMeta;
    }

    /**
     * Initializes and returns the enchantment metadata instance.
     *
     * <p>This method is useful when you prefer to configure {@link EnhancementMeta}
     * manually instead of using a lambda.</p>
     *
     * @return the {@link EnhancementMeta} instance to configure enhancement properties.
     */
    public EnhancementMeta createEnhancementMeta() {
        this.enhancements = new EnhancementMeta();
        return this.enhancements;
    }
    /**
     * Retrieve the set {@link EnhancementMeta}.
     *
     * @return instance of the class or {@code null} if not set.
     */
    @Nullable
    public EnhancementMeta getEnhancements() {
        return enhancements;
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

    /**
     * Initializes and configures the firework metadata by using
     * {@link FireworkMeta} class.
     *
     * @return the {@link FireworkMeta}} where you set the properties for your firework effect.
     */
    public FireworkMeta createFireworkMeta() {
        this.firework = new FireworkMeta();
        return this.firework;
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
     * Initializes and configures the leatherMeta metadata by using
     * {@link LeatherMeta} class.
     *
     * @return the {@link LeatherMeta}} where you set the properties for your firework effect.
     */
    public LeatherMeta createLeatherMeta() {
        this.leatherMeta = new LeatherMeta();
        return this.leatherMeta;
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
     * Initializes and configures the shield metadata by using
     * {@link ShieldMeta} class.
     *
     * @return the {@link ShieldMeta}} where you set the propitiates for your shield.
     */
    public ShieldMeta createShieldMeta() {
        this.shieldMeta = new ShieldMeta();
        return this.shieldMeta;
    }

    /**
     * Initializes and configures the shield metadata using the provided function.
     *
     * @param metaFunction a consumer that modifies and returns a value from the {@link ShieldMeta}.
     */
    public void setMapMeta(final Consumer<MapWrapperMeta> metaFunction) {
        final MapWrapperMeta meta = new MapWrapperMeta();
        metaFunction.accept(meta);
        this.mapMeta = meta;
    }

    /**
     * Initializes and configures the shield metadata by using
     * {@link ShieldMeta} class.
     *
     * @return the {@link ShieldMeta}} where you set the propitiates for your shield.
     */
    public MapWrapperMeta createMapMeta() {
        this.mapMeta = new MapWrapperMeta();
        return this.mapMeta;
    }

    /**
     * Initializes and configures the book metadata using the provided function.
     *
     * @param metaFunction a consumer that modifies and returns a value from the {@link BookMeta}.
     */
    public void setBookMeta(final Consumer<BookMeta>  metaFunction) {
        BookMeta meta = new BookMeta();
        metaFunction.accept(meta);
        this.bookMeta = meta;
    }

    /**
     * Initializes and configures the book metadata by using
     * {@link BookMeta } class.
     *
     * @return the {@link BookMeta }} where you set the propitiates for your book.
     */
    public BookMeta createBookMeta() {
        this.bookMeta = new BookMeta();
        return this.bookMeta;
    }

    /**
     * Retrieve the set {@link BookMeta} class.
     *
     * @return instance of the {@link BookMeta} class or {@code null} if not set.
     */
    @Nullable
    public BookMeta getBookMeta() {
        return bookMeta;
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
     *
     * @param itemStack the item whose material may be updated (e.g., for banner colors).
     * @param itemMeta  the metadata instance to modify by applying patterns, effects, or colors.
     */
    public void applyMeta(@Nonnull final ItemStack itemStack, @Nullable final ItemMeta itemMeta) {
        if (itemMeta != null) {
            this.setBannerMeta(itemStack, itemMeta);

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
            if (mapMeta != null) {
                mapMeta.applyMapMeta(itemStack, itemMeta);
            }
            if(this.bookMeta != null)
                this.bookMeta.applyBookMenta(itemMeta);
        }
    }

    private void setBannerMeta(@Nonnull ItemStack itemStack, @Nonnull ItemMeta itemMeta) {
        BannerMeta bannerMeta = this.banner;
        if (bannerMeta != null && bannerMeta.isBanner(itemMeta)) {
            Material materialFromColor = bannerMeta.getBannerMaterialFromColor();
            if (materialFromColor != null && materialFromColor != itemStack.getType())
                itemStack.setType(materialFromColor);
            bannerMeta.applyBannerPattern(itemMeta);
        }
    }
}
