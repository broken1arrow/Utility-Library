package org.broken.arrow.library.itemcreator.serialization;

import com.google.common.collect.Multimap;
import com.google.gson.GsonBuilder;
import org.broken.arrow.library.itemcreator.ItemCreator;
import org.broken.arrow.library.itemcreator.meta.BottleEffectMeta;
import org.broken.arrow.library.itemcreator.serialization.typeadapter.BottleEffectMetaAdapter;
import org.broken.arrow.library.itemcreator.serialization.typeadapter.FireworkMetaAdapter;
import org.broken.arrow.library.itemcreator.utility.PotionData;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class SerializeItem {
    private Material type;
    private int amount = 1;
    private String name;
    private List<String> lore;
    private final Map<String, Integer> enchantments = new HashMap<>();
    private Integer customModelData;
    private boolean unbreakable;
    private final Set<ItemFlag> itemFlags = new HashSet<>();
    // Skull
    private String skullOwner;
    private OfflinePlayer owningPlayer;
    private PlayerProfile ownerProfile;
    // Potion
    private BottleEffectMeta potionEffects;
    // Attributes
    private List<AttributeModifierWrapper> attributeModifiers;
    // Armor Color
    private Color armorColor;
    // Banner
    private DyeColor baseColor;
    private List<org.broken.arrow.library.itemcreator.meta.BannerMeta> patterns;
    // Firework
    private org.broken.arrow.library.itemcreator.meta.FireworkMeta fireworkMeta;
    private org.broken.arrow.library.itemcreator.meta.BookMeta bookMenta;


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
            getAttributeModifiers(meta, data);

            if (meta instanceof SkullMeta && ((SkullMeta) meta).hasOwner()) {
                final SkullMeta skull = (SkullMeta) meta;
                setOwner(data, skull);
            }

            if (meta instanceof PotionMeta) {
                final PotionMeta potionMeta = (PotionMeta) meta;
                data.potionEffects = new BottleEffectMeta();
                if (potionMeta.hasCustomEffects())
                    potionMeta.getCustomEffects().forEach(data.potionEffects::addPotionEffects);
                if (potionMeta.hasColor())
                    data.potionEffects.setBottleColor(colorMeta -> colorMeta.setRgb(potionMeta.getColor()));
                data.potionEffects.setPotionData(PotionData.findPotionByType(potionMeta.getBasePotionType()));
            }

            if (meta instanceof LeatherArmorMeta) {
                final LeatherArmorMeta armorMeta = (LeatherArmorMeta) meta;
                data.armorColor = armorMeta.getColor();
            }

            if (meta instanceof BannerMeta) {
                final BannerMeta bannerMeta = (BannerMeta) meta;
                org.broken.arrow.library.itemcreator.meta.BannerMeta bannerData = new org.broken.arrow.library.itemcreator.meta.BannerMeta();
                data.baseColor = bannerMeta.getBaseColor();
                data.patterns = bannerMeta.getPatterns().stream()
                        .map(pattern -> {
                            bannerData.setBannerBaseColor(pattern.getColor());
                            bannerData.addPatterns(pattern);
                            return bannerData;
                        })
                        .collect(Collectors.toList());
            }
            if (meta instanceof BookMeta) {
                final BookMeta bookMeta = (BookMeta) meta;
                data.bookMenta = org.broken.arrow.library.itemcreator.meta.BookMeta.setBookMeta(bookMeta);

            }
            if (meta instanceof FireworkMeta) {
                final FireworkMeta fwm = (FireworkMeta) meta;
                data.fireworkMeta = new org.broken.arrow.library.itemcreator.meta.FireworkMeta();
                data.fireworkMeta.setFireworkEffects(fwm.getEffects());
                data.fireworkMeta.setPower(fwm.getPower());
            }
        }
        return data;
    }

    public ItemStack toItemStack() {
        ItemStack item = new ItemStack(type, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (name != null) meta.setDisplayName(name);
            if (lore != null) meta.setLore(lore);
            if (customModelData != null) meta.setCustomModelData(customModelData);
            meta.setUnbreakable(unbreakable);
            itemFlags.forEach(meta::addItemFlags);

            if (!enchantments.isEmpty())
                for (Map.Entry<String, Integer> e : enchantments.entrySet()) {
                    Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(e.getKey()));
                    if (enchant != null) meta.addEnchant(enchant, e.getValue(), true);
                }
            final List<AttributeModifierWrapper> modifiers = this.attributeModifiers;
            if (modifiers != null && !modifiers.isEmpty()) {
                modifiers.forEach(attributeModifierWrapper -> {
                            final AttributeModifierWrapper.AttributeEntry modifier = attributeModifierWrapper.toModifier();
                            meta.addAttributeModifier(modifier.getAttribute(), modifier.getAttributeModifier());
                        }
                );
            }

            for (Map.Entry<String, Integer> e : enchantments.entrySet()) {
                Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(e.getKey()));
                if (enchant != null) meta.addEnchant(enchant, e.getValue(), true);
            }

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

            if (meta instanceof BannerMeta && patterns != null) {
                BannerMeta bannerMeta = (BannerMeta) meta;
                if (baseColor != null) bannerMeta.setBaseColor(baseColor);
                bannerMeta.setPatterns(patterns.stream().flatMap(bannerPatterns -> bannerPatterns.getPatterns().stream())
                        .collect(Collectors.toList()));
            }
            if (meta instanceof BookMeta && this.bookMenta != null) {
                final BookMeta bookMeta = (BookMeta) meta;
                this.bookMenta.applyBookMenta(bookMeta);
            }

            if (meta instanceof FireworkMeta && fireworkMeta != null) {
                FireworkMeta fwm = (FireworkMeta) meta;
                fireworkMeta.applyFireworkEffect(fwm);
            }

            item.setItemMeta(meta);
        }
        return item;
    }


    public String toJson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(FireworkMeta.class, new FireworkMetaAdapter())
                .registerTypeAdapter(BottleEffectMeta.class, new BottleEffectMetaAdapter())
                .create()
                .toJson(this);
    }

    public static SerializeItem fromJson(String json) {
        return new GsonBuilder()
                .registerTypeAdapter(FireworkMeta.class, new FireworkMetaAdapter())
                .registerTypeAdapter(BottleEffectMeta.class, new BottleEffectMetaAdapter())
                .create()
                .fromJson(json, SerializeItem.class);
    }

    private static void getAttributeModifiers(final ItemMeta meta, final SerializeItem data) {
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

    private void setOwnerToMeta(SkullMeta skull) {
        if (this.skullOwner != null) {
            skull.setOwner(this.skullOwner);
            return;
        }
        if (this.owningPlayer != null)
            skull.setOwningPlayer(this.owningPlayer);
        if (this.ownerProfile != null)
            skull.setOwnerProfile(this.ownerProfile);
    }


    private static void setOwner(SerializeItem data, SkullMeta skull) {
        final float serverVersion = ItemCreator.getServerVersion();
        if (serverVersion < 12.0f) {
            try {
                data.skullOwner = skull.getOwner();
            } catch (NoSuchMethodError ignore) {
                data.owningPlayer = skull.getOwningPlayer();
            }
            return;
        }
        data.owningPlayer = skull.getOwningPlayer();
        if (serverVersion > 18.0f) {
            data.ownerProfile = skull.getOwnerProfile();
        }
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
                Objects.equals(owningPlayer, that.owningPlayer) &&
                Objects.equals(ownerProfile, that.ownerProfile) &&
                Objects.equals(potionEffects, that.potionEffects) &&
                Objects.equals(attributeModifiers, that.attributeModifiers) &&
                Objects.equals(armorColor, that.armorColor) && baseColor == that.baseColor &&
                Objects.equals(patterns, that.patterns) &&
                Objects.equals(fireworkMeta, that.fireworkMeta) &&
                Objects.equals(bookMenta, that.bookMenta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, amount, name, lore, enchantments, customModelData, unbreakable, itemFlags, skullOwner, owningPlayer, ownerProfile, potionEffects, attributeModifiers, armorColor, baseColor, patterns, fireworkMeta, bookMenta);
    }
}
