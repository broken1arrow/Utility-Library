package org.broken.arrow.library.itemcreator.serialization.itemstack;

import com.google.common.collect.Multimap;
import com.google.gson.GsonBuilder;
import org.broken.arrow.library.itemcreator.ItemCreator;
import org.broken.arrow.library.itemcreator.SkullCreator;
import org.broken.arrow.library.itemcreator.meta.BottleEffectMeta;
import org.broken.arrow.library.itemcreator.meta.MapWrapperMeta;
import org.broken.arrow.library.itemcreator.serialization.AttributeModifierWrapper;
import org.broken.arrow.library.itemcreator.serialization.typeadapter.BottleEffectMetaAdapter;
import org.broken.arrow.library.itemcreator.serialization.typeadapter.FireworkMetaAdapter;
import org.broken.arrow.library.itemcreator.serialization.typeadapter.MapMetaAdapter;
import org.broken.arrow.library.itemcreator.meta.potion.PotionData;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.map.MapView;
import org.bukkit.profile.PlayerProfile;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a serializable version of a Bukkit {@link ItemStack} including its
 * type, metadata, enchantments, attributes, colors, and other special properties.
 * <p>
 * Provides methods for converting between {@link ItemStack} and its JSON form,
 * and reconstructing fully-featured items from serialized data.
 * </p>
 * <p>
 * Supports advanced item data such as banners, books, potions, skull owners,
 * armor colors, attributes, and firework effects.
 * </p>
 */
public class SerializeItem {

    private final Set<ItemFlag> itemFlags = new HashSet<>();
    private final Map<String, Integer> enchantments = new HashMap<>();
    private List<AttributeModifierWrapper> attributeModifiers;
    private List<org.broken.arrow.library.itemcreator.meta.BannerMeta> patterns;
    private Material type;
    private Color armorColor;
    private String name;
    private List<String> lore;
    private Integer customModelData;
    private String skullOwner;
    private UUID skinPlayerId;
    private BottleEffectMeta potionEffects;
    private org.broken.arrow.library.itemcreator.meta.FireworkMeta fireworkMeta;
    private org.broken.arrow.library.itemcreator.meta.BookMeta bookMenta;
    private MapWrapperMeta mapViewMeta;
    private String skullUrl;
    private int amount = 1;
    private boolean unbreakable;

    /**
     * Creates a serializable item representation from an ItemStack.
     *
     * @param item the ItemStack to serialize.
     * @return the created SerializeItem instance.
     */
    public static SerializeItem fromItemStack(@Nonnull final ItemStack item) {
        final SerializeItem data = new SerializeItem();
        data.type = item.getType();
        data.amount = item.getAmount();

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (meta.hasDisplayName()) data.name = meta.getDisplayName();
            if (meta.hasLore()) data.lore = meta.getLore();
            if (meta.hasCustomModelData()) data.customModelData = meta.getCustomModelData();
            data.unbreakable = meta.isUnbreakable();
            data.itemFlags.addAll(meta.getItemFlags());

            meta.getEnchants().forEach((e, lvl) -> data.enchantments.put(e.getKey().getKey(), lvl));
            retrieveAttributeModifiers(meta, data);
            retrievePotionMeta(meta, data);
            retrieveBannerMeta(meta, data);
            retrieveFireworkMeta(meta, data);

            if (meta instanceof SkullMeta && ((SkullMeta) meta).hasOwner()) {
                final SkullMeta skull = (SkullMeta) meta;
                setOwner(data, skull);
            }
            if (meta instanceof LeatherArmorMeta) {
                final LeatherArmorMeta armorMeta = (LeatherArmorMeta) meta;
                data.armorColor = armorMeta.getColor();
            }

            if (meta instanceof BookMeta) {
                final BookMeta bookMeta = (BookMeta) meta;
                data.bookMenta = org.broken.arrow.library.itemcreator.meta.BookMeta.setBookMeta(bookMeta);

            }
            if (meta instanceof MapMeta) {
                final MapMeta mapMeta = (MapMeta) meta;
                if (mapMeta.hasMapView()) {
                    final MapView mapView = mapMeta.getMapView();
                    if (mapView != null) {
                        final MapWrapperMeta mapWrapperMeta = new MapWrapperMeta();
                        mapWrapperMeta.createMapView(mapView);
                        data.mapViewMeta = mapWrapperMeta;
                    }
                }
            }
        }
        return data;
    }

    /**
     * Converts this serialized item back to a Bukkit ItemStack.
     *
     * @return a new ItemStack with the stored properties
     */
    public ItemStack toItemStack() {
        ItemStack item = new ItemStack(type, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (name != null) meta.setDisplayName(name);
            if (lore != null) meta.setLore(lore);
            if (customModelData != null) meta.setCustomModelData(customModelData);
            meta.setUnbreakable(unbreakable);
            itemFlags.forEach(meta::addItemFlags);

            this.setEnchantment(meta);
            this.setAttributeModifier(meta);
            this.setBannerMeta(meta);

            if (meta instanceof SkullMeta) {
                SkullMeta skull = (SkullMeta) meta;
                setOwnerToMeta(skull);
            }

            if (meta instanceof PotionMeta && potionEffects != null) {
                BottleEffectMeta effect = potionEffects;
                effect.applyBottleEffects(meta);
            }

            if (meta instanceof LeatherArmorMeta && armorColor != null) {
                LeatherArmorMeta armorMeta = (LeatherArmorMeta) meta;
                armorMeta.setColor(armorColor);
            }

            if (meta instanceof BookMeta && this.bookMenta != null) {
                final BookMeta bookMeta = (BookMeta) meta;
                this.bookMenta.applyBookMenta(bookMeta);
            }

            if (meta instanceof FireworkMeta && fireworkMeta != null) {
                FireworkMeta fwm = (FireworkMeta) meta;
                fireworkMeta.applyFireworkEffect(fwm);
            }

            if (meta instanceof MapMeta && mapViewMeta != null) {
                MapMeta fwm = (MapMeta) meta;
                mapViewMeta.applyMapMeta(fwm);
            }

            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Serializes this item to a pretty-printed JSON string.
     *
     * @return JSON representation of this item
     */
    public String toJson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(FireworkMeta.class, new FireworkMetaAdapter())
                .registerTypeAdapter(BottleEffectMeta.class, new BottleEffectMetaAdapter())
                .registerTypeAdapter(MapWrapperMeta.class, new MapMetaAdapter())
                .create()
                .toJson(this);
    }

    /**
     * Creates a serialized item from a JSON string.
     *
     * @param json the JSON data
     * @return the deserialized SerializeItem
     */
    public static SerializeItem fromJson(String json) {
        return new GsonBuilder()
                .registerTypeAdapter(FireworkMeta.class, new FireworkMetaAdapter())
                .registerTypeAdapter(BottleEffectMeta.class, new BottleEffectMetaAdapter())
                .registerTypeAdapter(MapWrapperMeta.class, new MapMetaAdapter())
                .create()
                .fromJson(json, SerializeItem.class);
    }

    /**
     * Retrieve the item-stack type.
     *
     * @return the item material type
     */
    public Material getType() {
        return type;
    }

    /**
     * Retrieve the item-stack amount.
     *
     * @return the item amount
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Retrieve the item-stack display name.
     *
     * @return the display name, or null if not set
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieve the item-stack lore.
     *
     * @return the item lore, or null if not set
     */
    public List<String> getLore() {
        return lore;
    }

    /**
     * Retrieve the item-stack enchantments.
     *
     * @return the stored enchantments mapped by key
     */
    public Map<String, Integer> getEnchantments() {
        return enchantments;
    }

    /**
     * Retrieve the item-stack custom model data.
     *
     * @return the custom model data, or null if not set
     */
    public Integer getCustomModelData() {
        return customModelData;
    }

    /**
     * Retrieve the item-stack is unbreakable.
     *
     * @return true if the item is unbreakable
     */
    public boolean isUnbreakable() {
        return unbreakable;
    }

    /**
     * Retrieve the item-stack flags.
     *
     * @return the set of item flags
     */
    public Set<ItemFlag> getItemFlags() {
        return itemFlags;
    }

    /**
     * Retrieve the item-stack owner profile if item is skull.
     *
     * @return the skull owner name, or null if not set
     */
    public String getSkullOwner() {
        return skullOwner;
    }

    /**
     * Retrieve the item-stack owner profile if item is skull.
     *
     * @return the owning player, or null if not set
     */
    public UUID getSkinPlayerId() {
        return skinPlayerId;
    }

    /**
     * Retrieve the item-stack potion effects.
     *
     * @return the stored potion effects, or null if not set
     */
    public BottleEffectMeta getPotionEffects() {
        return potionEffects;
    }

    /**
     * Retrieve the item-stack attribute modifier.
     *
     * @return the attribute modifiers, or null if not set
     */
    public List<AttributeModifierWrapper> getAttributeModifiers() {
        return attributeModifiers;
    }

    /**
     * Retrieve the item-stack leather armor color.
     *
     * @return the leather armor color, or null if not set
     */
    public Color getArmorColor() {
        return armorColor;
    }


    /**
     * Retrieve the item-stack banner patterns.
     *
     * @return the banner patterns, or null if not set
     */
    public List<org.broken.arrow.library.itemcreator.meta.BannerMeta> getPatterns() {
        return patterns;
    }

    /**
     * Retrieve the item-stack firework metadata.
     *
     * @return the firework metadata, or null if not set
     */
    public org.broken.arrow.library.itemcreator.meta.FireworkMeta getFireworkMeta() {
        return fireworkMeta;
    }

    /**
     * Retrieve the item-stack book metadata.
     *
     * @return the book metadata, or null if not set
     */
    public org.broken.arrow.library.itemcreator.meta.BookMeta getBookMenta() {
        return bookMenta;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final SerializeItem that = (SerializeItem) o;
        return amount == that.amount && unbreakable == that.unbreakable &&
                type == that.type &&
                Objects.equals(name, that.name) &&
                Objects.equals(lore, that.lore) &&
                Objects.equals(enchantments, that.enchantments) &&
                Objects.equals(customModelData, that.customModelData) &&
                Objects.equals(itemFlags, that.itemFlags) &&
                Objects.equals(skullOwner, that.skullOwner) &&
                Objects.equals(skinPlayerId, that.skinPlayerId) &&
                Objects.equals(potionEffects, that.potionEffects) &&
                Objects.equals(attributeModifiers, that.attributeModifiers) &&
                Objects.equals(armorColor, that.armorColor)  &&
                Objects.equals(patterns, that.patterns) &&
                Objects.equals(fireworkMeta, that.fireworkMeta) &&
                Objects.equals(bookMenta, that.bookMenta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, amount, name, lore, enchantments, customModelData, unbreakable, itemFlags, skullOwner, skinPlayerId,  potionEffects, attributeModifiers, armorColor, patterns, fireworkMeta, bookMenta);
    }

    private void setOwnerToMeta(@Nonnull final SkullMeta skull) {
        final UUID playerId = this.skinPlayerId;
        if (playerId == null)
            return;
        SkullCreator.setSkullUrl(skull, playerId, this.skullUrl);
        if (this.skullOwner != null) {
            skull.setOwner(this.skullOwner);
            return;
        }
        skull.setOwningPlayer(Bukkit.getOfflinePlayer(playerId));
    }


    private static void setOwner(final SerializeItem data, final SkullMeta skull) {
        final float serverVersion = ItemCreator.getServerVersion();
        data.skullUrl = SkullCreator.getSkullUrl(skull);
        if (serverVersion < 12.0f) {
            try {
                data.skullOwner = skull.getOwner();
            } catch (NoSuchMethodError ignore) {
                final OfflinePlayer owningPlayer = skull.getOwningPlayer();
                if (owningPlayer != null)
                    data.skinPlayerId = owningPlayer.getUniqueId();
            }
            return;
        }
        final OfflinePlayer owningPlayer = skull.getOwningPlayer();
        if (owningPlayer != null)
            data.skinPlayerId = owningPlayer.getUniqueId();

    }

    private static void retrieveFireworkMeta(final ItemMeta meta, final SerializeItem data) {
        if (meta instanceof FireworkMeta) {
            final FireworkMeta fwm = (FireworkMeta) meta;
            data.fireworkMeta = new org.broken.arrow.library.itemcreator.meta.FireworkMeta();
            data.fireworkMeta.setFireworkEffects(fwm.getEffects());
            data.fireworkMeta.setPower(fwm.getPower());
        }
    }

    private static void retrievePotionMeta(final ItemMeta meta, final SerializeItem data) {
        if (meta instanceof PotionMeta) {
            final PotionMeta potionMeta = (PotionMeta) meta;
            data.potionEffects = new BottleEffectMeta();
            if (potionMeta.hasCustomEffects())
                potionMeta.getCustomEffects().forEach(data.potionEffects::addPotionEffects);
            if (potionMeta.hasColor())
                data.potionEffects.setBottleColor(colorMeta -> colorMeta.setRgb(potionMeta.getColor()));
            data.potionEffects.setPotionData(PotionData.findPotionByType(potionMeta.getBasePotionType()));
        }
    }

    private static void retrieveBannerMeta(final ItemMeta meta, final SerializeItem data) {
        if (meta instanceof BannerMeta) {
            final BannerMeta bannerMeta = (BannerMeta) meta;
            org.broken.arrow.library.itemcreator.meta.BannerMeta bannerData = new org.broken.arrow.library.itemcreator.meta.BannerMeta();
            bannerData.setBannerBaseColor(bannerMeta.getBaseColor());
            data.patterns = bannerMeta.getPatterns().stream()
                    .map(pattern -> {
                        bannerData.addPatterns(pattern);
                        return bannerData;
                    })
                    .collect(Collectors.toList());
        }
    }

    private static void retrieveAttributeModifiers(final ItemMeta meta, final SerializeItem data) {
        if (meta.hasAttributeModifiers()) {
            Multimap<Attribute, AttributeModifier> attributeModifierData = meta.getAttributeModifiers();
            if (attributeModifierData != null) {
                data.attributeModifiers = new ArrayList<>();
                attributeModifierData.forEach((attribute, attributeModifier) ->
                        data.attributeModifiers.add(AttributeModifierWrapper.from(attribute, attributeModifier)
                        ));
            }
        }
    }

    private void setBannerMeta(final ItemMeta meta) {
        if (meta instanceof BannerMeta && patterns != null) {
            BannerMeta bannerMeta = (BannerMeta) meta;
            final DyeColor baseColor = bannerMeta.getBaseColor();
            if (baseColor != null) bannerMeta.setBaseColor(baseColor);
            bannerMeta.setPatterns(patterns.stream().flatMap(bannerPatterns -> bannerPatterns.getPatterns().stream())
                    .collect(Collectors.toList()));
        }
    }

    private void setEnchantment(final ItemMeta meta) {
        if (!enchantments.isEmpty())
            for (Map.Entry<String, Integer> e : enchantments.entrySet()) {
                Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(e.getKey()));
                if (enchant != null) meta.addEnchant(enchant, e.getValue(), true);
            }
    }

    private void setAttributeModifier(final ItemMeta meta) {
        final List<AttributeModifierWrapper> modifiers = this.attributeModifiers;
        if (modifiers != null && !modifiers.isEmpty()) {
            modifiers.forEach(attributeModifierWrapper -> {
                        final AttributeModifierWrapper.AttributeEntry modifier = attributeModifierWrapper.toModifier();
                        meta.addAttributeModifier(modifier.getAttribute(), modifier.getAttributeModifier());
                    }
            );
        }
    }
}
