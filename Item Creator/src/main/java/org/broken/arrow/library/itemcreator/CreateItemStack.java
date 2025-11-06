package org.broken.arrow.library.itemcreator;


import net.md_5.bungee.api.ChatColor;
import org.broken.arrow.library.color.TextTranslator;
import org.broken.arrow.library.itemcreator.meta.BottleEffectMeta;
import org.broken.arrow.library.itemcreator.meta.enhancement.EnhancementMeta;
import org.broken.arrow.library.itemcreator.meta.MetaHandler;
import org.broken.arrow.library.itemcreator.meta.potion.PotionTypeWrapper;
import org.broken.arrow.library.itemcreator.utility.ConvertToItemStack;
import org.broken.arrow.library.itemcreator.utility.Tuple;
import org.broken.arrow.library.itemcreator.utility.builders.ItemBuilder;
import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.logging.Validate;
import org.broken.arrow.library.nbt.RegisterNbtAPI;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;


/**
 * Create items with your set data. When you make a item it will also detect minecraft version
 * and provide help when make items for different minecraft versions (you can ether let it auto convert colors depending
 * on version or hardcode itself).
 */

public class CreateItemStack {
    private static final Logging logger = new Logging(CreateItemStack.class);
    private final ItemCreator itemCreator;
    private final ConvertToItemStack convertItems;
    private final ItemBuilder itemBuilder;
    private final Iterable<?> itemArray;
    private final String displayName;
    private final List<String> loreList;
    private final RegisterNbtAPI nbtApi;
    private final float serverVersion;
    private final boolean haveTextTranslator;
    private final boolean enableColorTranslation;

    private String color;
    private List<ItemFlag> itemFlags;
    private NBTDataWrapper nbtDataWrapper;

    private MetaHandler metaHandler;
    private int amountOfItems;
    private byte data = -1;
    private int customModelData = -1;
    private short damage = 0;
    private boolean glow;
    private boolean unbreakable;
    private boolean keepAmount;
    private boolean keepOldMeta = true;
    private boolean copyOfItem;

    /**
     * Create an instance for the CreateItemStack that wraps your item creation.
     *
     * @param itemCreator The main utility class.
     * @param itemBuilder The builder for the item stacks.
     */
    public CreateItemStack(final ItemCreator itemCreator, final ItemBuilder itemBuilder) {
        this.itemCreator = itemCreator;
        this.serverVersion = ItemCreator.getServerVersion();
        this.convertItems = itemCreator.getConvertItems();

        this.itemBuilder = itemBuilder;
        this.itemArray = itemBuilder.getItemArray();
        this.displayName = itemBuilder.getDisplayName();
        this.loreList = itemBuilder.getLore();
        this.nbtApi = itemCreator.getNbtApi();
        this.haveTextTranslator = itemCreator.isHaveTextTranslator();
        this.enableColorTranslation = itemCreator.isEnableColorTranslation();
    }

    /**
     * Amount of items you want to create.
     *
     * @param amountOfItems item amount.
     * @return this class.
     */
    public CreateItemStack setAmountOfItems(final int amountOfItems) {
        this.amountOfItems = amountOfItems;
        return this;
    }

    /**
     * Applies properties specific to a certain item meta type. This method safely verifies whether the metadata
     * can be applied to the item, so using it on an incompatible item type will have no effect and cause no issues.
     *
     * <p>
     * Use this method when you need to both apply metadata and return a custom value from the operation.
     * If you only need to modify metadata without returning a result, consider using
     * {@link #setItemMeta(Consumer)} instead it uses {@code CreateItemStack} so you can chain
     * multiple methods.
     * </p>
     *
     * @param metaModifier a function that receives a {@link MetaHandler} and returns a result of type {@code T}.
     * @param <T>          the type of value to return from the modifier function.
     * @return the result of applying the {@code metaModifier}, or {@code null} if none is returned.
     */
    @Nullable
    public <T> T setItemMeta(@Nonnull final Function<MetaHandler, T> metaModifier) {
        this.metaHandler = new MetaHandler();
        return metaModifier.apply(metaHandler);
    }

    /**
     * Applies properties specific to a certain item type. It will automatically verify whether the metadata
     * can be applied to the item, so using it on an incompatible item type does not cause any issues.
     *
     * @param metaModifier a consumer used to modify the metadata for your specific item type.
     * @return this instance for chaining.
     */
    public CreateItemStack setItemMeta(@Nonnull final Consumer<MetaHandler> metaModifier) {
        this.metaHandler = new MetaHandler();
        metaModifier.accept(metaHandler);
        return this;
    }

    /**
     * If the items shall have glow effect, without show enchantments.
     *
     * @return {@code true} does the item have a glow effect.
     */
    public boolean isGlow() {
        return glow;
    }

    /**
     * Set glow on item and will not show the enchantments.
     * Use {@link #addEnchantments(Object, boolean, int)} or {@link #addEnchantments(String...)}, for set custom
     * enchants.
     *
     * @param glow set it true and the item will glow.
     * @return this class.
     */
    public CreateItemStack setGlow(final boolean glow) {
        this.glow = glow;
        return this;
    }

    /**
     * Get pattern for the banner.
     *
     * @return list of patterns.
     */
    public List<Pattern> getPattern() {
        return new ArrayList<>();
    }

    /**
     * Add one or several patterns.
     *
     * @param patterns to add to the list.
     * @return this class.
     * @deprecated use {@link #setItemMeta(Consumer)}
     */
    @Deprecated
    public CreateItemStack addPattern(final Pattern... patterns) {
        if (patterns == null || patterns.length < 1) return this;

        final MetaHandler metadata = this.getOrCreateMetaHandler();
        final org.broken.arrow.library.itemcreator.meta.BannerMeta bannerData = metadata.getBanner();

        metadata.setBanner(bannerMeta -> {
            bannerMeta.addPatterns(patterns);
            bannerMeta.setBannerBaseColor(bannerData != null ? bannerData.getBannerBaseColor() : null);
        });
        return this;
    }

    /**
     * Add list of patterns (if it exist old patterns in the list, will the new ones be added on top).
     *
     * @param patterns list some contains patterns.
     * @return this class.
     * @deprecated use {@link #setItemMeta(Consumer)}
     */
    @Deprecated
    public CreateItemStack addPattern(final List<Pattern> patterns) {
        final MetaHandler metadata = this.getOrCreateMetaHandler();
        org.broken.arrow.library.itemcreator.meta.BannerMeta bannerData = metadata.getBanner();

        metadata.setBanner(bannerMeta -> {
            bannerMeta.addPatterns(patterns);
            bannerMeta.setBannerBaseColor(bannerData != null ? bannerData.getBannerBaseColor() : null);
        });
        return this;
    }

    /**
     * Get the base color for the banner (the color before add patterns).
     *
     * @return the color.
     */

    public DyeColor getBannerBaseColor() {
        return null;
    }

    /**
     * Set the base color for the banner.
     *
     * @param bannerBaseColor the color.
     * @return this class.
     * @deprecated use {@link #setItemMeta(Consumer)}
     */
    @Deprecated
    public CreateItemStack setBannerBaseColor(DyeColor bannerBaseColor) {
        final MetaHandler metadata = this.getOrCreateMetaHandler();
        final org.broken.arrow.library.itemcreator.meta.BannerMeta bannerData = metadata.getBanner();

        metadata.setBanner(bannerMeta -> {
            bannerMeta.addPatterns(bannerData != null ? bannerData.getPatterns() : null);
            bannerMeta.setBannerBaseColor(bannerBaseColor);
        });

        return this;
    }

    /**
     * Get enchantments for this item.
     *
     * @return map with enchantment level and if it shall ignore level restriction.
     * @deprecated use {@link #setItemMeta(Consumer)}
     */
    @Deprecated
    public Map<Enchantment, Tuple<Integer, Boolean>> getEnchantments() {
        return new HashMap<>();
    }

    /**
     * Check if it water Bottle. Because
     * only exist material portion, so need this method.
     *
     * @return true if it is a water Bottle item.
     */
    public boolean isWaterBottle() {
        MetaHandler meta = getMetaHandler();
        if (meta == null)
            return false;
        final BottleEffectMeta bottleEffect = meta.getBottleEffect();
        if (bottleEffect == null)
            return false;
        return bottleEffect.isWaterBottle();
    }

    /**
     * Set if the portion is a water bottle, as it is
     * not same thing as a potion.
     *
     * @param waterBottle {@code true} if it should be a water bottle otherwise it will be a potion.
     * @return this class
     * @deprecated use {@link #setItemMeta(Consumer)}
     */
    @Deprecated
    public CreateItemStack setWaterBottle(final boolean waterBottle) {
        final MetaHandler metadata = this.getOrCreateMetaHandler();
        metadata.createBottleEffectMeta().setWaterBottle(waterBottle);
        return this;
    }

    /**
     * If it shall keep the old amount of items (if you modify old itemStack).
     *
     * @return true if you keep old amount.
     */
    public boolean isKeepAmount() {
        return keepAmount;
    }

    /**
     * Set if you want to keep old amount.
     *
     * @param keepAmount set it to true if you want keep old amount.
     * @return this class instance.
     */
    public CreateItemStack setKeepAmount(final boolean keepAmount) {
        this.keepAmount = keepAmount;
        return this;
    }

    /**
     * if it shall keep old metadata (only work if you modify old itemstack).
     * Default it will keep the meta.
     *
     * @return true if you keep old meta.
     */
    public boolean isKeepOldMeta() {
        return keepOldMeta;
    }

    /**
     * Set if it shall keep the old metadata or not.
     * Default it will keep the meta.
     *
     * @param keepOldMeta set to false if you not want to keep old metadata.
     * @return this class.
     */
    public CreateItemStack setKeepOldMeta(final boolean keepOldMeta) {
        this.keepOldMeta = keepOldMeta;
        return this;
    }

    /**
     * Get list of firework effects
     *
     * @return list of effects set on this item.
     */
    public FireworkEffect getFireworkEffect() {
        return null;
    }

    /**
     * Add firework effect on this item.
     *
     * @param fireworkEffect effect you want to add to your firework.
     * @deprecated use {@link #setItemMeta(Consumer)}
     */
    @Deprecated
    public void setFireworkEffect(final FireworkEffect fireworkEffect) {
        MetaHandler metadata = this.getOrCreateMetaHandler();

        metadata.setFirework(fireworkMeta -> {
            fireworkMeta.addFireworkEffect(fireworkEffect);
        });
    }

    /**
     * Retrieve the item damage.
     *
     * @return the damage
     */
    public short getDamage() {
        return damage;
    }

    /**
     * Set the item damage if the item support it.
     *
     * @param damage the damage to set.
     */
    public void setDamage(short damage) {
        this.damage = damage;
    }


    /**
     * Add own enchantments.
     * <p>
     * This method uses varargs and add it to list, like this enchantment;level;levelRestriction or
     * enchantment;level and it will sett last one to false.
     * <p>
     * Example usage here:
     * "PROTECTION_FIRE;1;false","PROTECTION_EXPLOSIONS;15;true","WATER_WORKER;1;false".
     *
     * @param enchantments list of enchantments you want to add.
     * @return this class.
     * @deprecated use {@link #setItemMeta(Consumer)}
     */
    @Deprecated
    public CreateItemStack addEnchantments(final String... enchantments) {
        for (final String enchant : enchantments) {
            final int middle = enchant.indexOf(";");
            final int last = enchant.lastIndexOf(";");
            addEnchantments(enchant.substring(0, middle), last > 0 && Boolean.getBoolean(enchant.substring(last + 1)), Integer.parseInt(enchant.substring(middle + 1, Math.max(last, enchant.length()))));
        }
        return this;
    }

    /**
     * Add enchantments. Will set levelRestriction to true and level to 1.
     *
     * @param enchantments list of enchantments you want to add.
     * @return this class.
     * @deprecated use {@link #setItemMeta(Consumer)}
     */
    @Deprecated
    public CreateItemStack addEnchantments(final Enchantment... enchantments) {
        for (final Enchantment enchant : enchantments) {
            addEnchantments(enchant, true, 1);
        }
        return this;
    }

    /**
     * Add own enchantments.
     *
     * @param enchantmentMap add directly a map with enchants and level and levelRestrictions.
     * @param override       the old value in the map if you set it to true.
     * @return this class.
     * @deprecated use {@link #setItemMeta(Consumer)}
     */
    @Deprecated
    public CreateItemStack addEnchantments(final Map<Enchantment, Tuple<Integer, Boolean>> enchantmentMap, final boolean override) {
        Validate.checkNotNull(enchantmentMap, "this map is null");
        if (enchantmentMap.isEmpty())
            logger.log(() -> "This map is empty so no enchantments will be added");
        final MetaHandler metadata = this.getOrCreateMetaHandler();

        enchantmentMap.forEach((key, value) -> {
            metadata.setEnhancements(enhancementMeta ->
                    enhancementMeta
                            .setEnchantment(key)
                            .setLevel(value.getFirst())
                            .setIgnoreLevelRestriction(value.getSecond())
            );
        });
        return this;
    }

    /**
     * Add own enchantments.
     *
     * @param enchant          enchantments you want to add, support string and Enchantment class.
     * @param levelRestriction bypass the level limit.
     * @param enchantmentLevel set level for this enchantment.
     * @return this class.
     * @deprecated use {@link #setItemMeta(Consumer)}
     */
    @Deprecated
    public CreateItemStack addEnchantments(final Object enchant, final boolean levelRestriction, final int enchantmentLevel) {
        Enchantment enchantment = null;
        if (enchant instanceof String)
            enchantment = Enchantment.getByKey(NamespacedKey.minecraft((String) enchant));
        else if (enchant instanceof Enchantment)
            enchantment = (Enchantment) enchant;

        if (enchantment == null) {
            logger.log(() -> "your enchantment: " + enchant + " ,are not valid.");
            return this;
        }
        final MetaHandler meta = getOrCreateMetaHandler();
        final Enchantment finalEnchantment = enchantment;

        meta.setEnhancements(enhancementMeta ->
                enhancementMeta.setEnchantment(finalEnchantment)
                        .setLevel(enchantmentLevel)
                        .setIgnoreLevelRestriction(levelRestriction)
        );
        return this;
    }

    /**
     * Set custom nbt data on item.
     *
     * @param itemMetaKey   key for get value.
     * @param itemMetaValue value you want to set.
     * @return this class.
     */
    public CreateItemStack setItemMetaData(final String itemMetaKey, final Object itemMetaValue) {
        return setItemMetaData(itemMetaKey, itemMetaValue, false);
    }

    /**
     * Set custom nbt data on item.
     *
     * @param itemMetaKey   key for get value.
     * @param itemMetaValue value you want to set.
     * @param keepClazz     true if it shall keep all data on the item or false to convert value to string.
     * @return this class.
     */
    public CreateItemStack setItemMetaData(final String itemMetaKey, final Object itemMetaValue, final boolean keepClazz) {
        nbtDataWrapper = NBTDataWrapper.of(this.itemCreator).add(itemMetaKey, itemMetaValue, keepClazz);
        return this;
    }

    /**
     * Set your metadata on the item. Use {@link NBTDataWrapper} class.
     * To set key and value.
     *
     * @param wrapper values from MetaDataWrapper.
     * @return this class.
     */
    public CreateItemStack setItemMetaDataList(final NBTDataWrapper wrapper) {
        nbtDataWrapper = wrapper;
        return this;
    }

    /**
     * Map list of metadata you want to set on a item.
     * It use map key and value form the map.
     *
     * @param itemMetaMap map of values.
     * @return this class.
     */
    public CreateItemStack setItemMetaDataList(final Map<String, Object> itemMetaMap) {
        if (itemMetaMap != null && !itemMetaMap.isEmpty()) {
            final NBTDataWrapper wrapper = NBTDataWrapper.of(this.itemCreator);
            for (final Map.Entry<String, Object> itemData : itemMetaMap.entrySet()) {
                wrapper.add(itemData.getKey(), itemData.getValue());
            }
            nbtDataWrapper = wrapper;
        }
        return this;
    }

    /**
     * if this item is unbreakable or not
     *
     * @return true if the item is unbreakable.
     */
    public boolean isUnbreakable() {
        return unbreakable;
    }

    /**
     * Set if you can break the item or not.
     *
     * @param unbreakable true if the tool shall not break
     * @return this class.
     */
    public CreateItemStack setUnbreakable(final boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    /**
     * Old method to set data on a item.
     *
     * @return number.
     */
    public Byte getData() {
        return data;
    }

    /**
     * Set data on a item.
     *
     * @param data the byte you want to set.
     * @return this class.
     */
    public CreateItemStack setData(final byte data) {
        this.data = data;
        return this;
    }

    /**
     * Get Custom Model data on the item. use this instead of set data on a item.
     *
     * @return Model data number.
     */

    public int getCustomModelData() {
        return customModelData;
    }

    /**
     * Set Custom Model data on a item. will work on newer minecraft versions.
     *
     * @param customModelData number.
     * @return this class.
     */
    public CreateItemStack setCustomModelData(final int customModelData) {
        this.customModelData = customModelData;
        return this;
    }

    /**
     * Get all portions effects for this item.
     *
     * @return list of portions effects.
     */
    public List<PotionEffect> getPortionEffects() {
        return new ArrayList<>();
    }

    /**
     * Set a list of effects to the list. If it exist old effects in the list, this will be removed.
     *
     * @param potionEffects list of effects you want to set.
     * @return this class.
     * @deprecated use {@link #setItemMeta(Consumer)}
     */
    @Deprecated
    public CreateItemStack setPortionEffects(final List<PotionEffect> potionEffects) {
        if (potionEffects.isEmpty()) {
            logger.log(() -> "This list of portion effects is empty so no values will be added");
            return this;
        }
        final MetaHandler metadata = this.getOrCreateMetaHandler();

        metadata.setBottleEffect(enhancementMeta ->
                enhancementMeta.setPotionEffects((potionEffects)
                ));
        return this;
    }

    /**
     * Add one or several portions effects to list.
     *
     * @param potionEffects you want to set on the item.
     * @return this class.
     * @deprecated use {@link #setItemMeta(Consumer)}
     */
    @Deprecated
    public CreateItemStack addPortionEffects(final PotionEffect... potionEffects) {
        if (potionEffects.length == 0) return this;

        final MetaHandler metadata = this.getOrCreateMetaHandler();
        final BottleEffectMeta bottleEffectMeta = metadata.getBottleEffect();

        metadata.setBottleEffect(effectMeta -> {
            effectMeta.setPotionEffects(bottleEffectMeta != null ? bottleEffectMeta.getPotionEffects() : null);
            effectMeta.addPotionEffects(potionEffects);
        });
        return this;
    }

    /**
     * Add a list of effects to the list. If it exist old effects this will add the effects on top of the old ones.
     *
     * @param potionEffects list of effects you want to add.
     * @return this class.
     */
    public CreateItemStack addPortionEffects(final List<PotionEffect> potionEffects) {
        if (potionEffects.isEmpty()) {
            logger.log(() -> "This list of portion effects is empty so no values will be added");
            return this;
        }
        final MetaHandler metadata = this.getOrCreateMetaHandler();
        final BottleEffectMeta bottleEffectMeta = metadata.getBottleEffect();

        metadata.setBottleEffect(effectMeta -> {
            List<PotionEffect> potionEffectsList = new ArrayList<>();
            if (bottleEffectMeta != null)
                potionEffectsList = bottleEffectMeta.getPotionEffects();
            potionEffectsList.addAll(potionEffects);
            effectMeta.setPotionEffects(potionEffectsList);
        });
        return this;
    }

    /**
     * Get the rbg colors, used to dye leather armor,potions and fireworks.
     *
     * @return string with the colors, like this #,#,#.
     */
    public String getRgb() {
        return "";
    }

    /**
     * Set the 3 colors auto.
     *
     * @param rgb string need to be formatted like this #,#,#.
     * @return this class.
     * @deprecated use {@link #setItemMeta(Consumer)}
     */
    @Deprecated
    public CreateItemStack setRgb(final String rgb) {
        return this;
    }

    /**
     * Retrieve if all colors is set.
     *
     * @return true if the colors is set.
     */
    public boolean isColorSet() {
        return getRed() >= 0 && getGreen() >= 0 && getBlue() >= 0;
    }

    /**
     * Get red color.
     *
     * @return color number.
     */
    public int getRed() {
        return 0;
    }

    /**
     * Get green color.
     *
     * @return color number.
     */
    public int getGreen() {
        return 0;
    }

    /**
     * Get blue color
     *
     * @return color number.
     */
    public int getBlue() {
        return 0;
    }

    /**
     * Get the list of flags set on this item.
     *
     * @return list of flags.
     */
    @Nonnull
    public List<ItemFlag> getItemFlags() {
        if (itemFlags == null) return new ArrayList<>();
        return itemFlags;
    }

    /**
     * Hide one or several metadata values on a itemstack.
     *
     * @param itemFlags add one or several flags you not want to hide.
     * @return this class.
     */
    public CreateItemStack setItemFlags(final ItemFlag... itemFlags) {
        return this.setItemFlags(Arrays.asList(itemFlags));
    }

    /**
     * Don´t hide one or several metadata values on a itemstack.
     *
     * @param itemFlags add one or several flags you not want to hide.
     * @return this class.
     */
    public CreateItemStack setItemFlags(final List<ItemFlag> itemFlags) {
        Validate.checkNotNull(itemFlags, "flags list should not be null");
        this.itemFlags = itemFlags;
        return this;
    }

    /**
     * Get if it has created copy of item or not.
     *
     * @return true if it shall make copy of original item.
     */

    public boolean isCopyOfItem() {
        return copyOfItem;
    }

    /**
     * If it shall create copy of the item or change original item.
     *
     * @param copyItem true if you want to create copy.
     * @return this class.
     */
    public CreateItemStack setCopyOfItem(final boolean copyItem) {
        this.copyOfItem = copyItem;
        return this;
    }

    /**
     * Set color propitiates for the item.
     *
     * @param colorName the color name to set.
     * @return this class.
     */
    public CreateItemStack setColor(String colorName) {
        this.color = colorName;
        return this;
    }

    /**
     * Get the class for set nbt data on your item.
     *
     * @return the nbtdata instance.
     */
    @Nullable
    public NBTDataWrapper getNbtDataWrapper() {
        return nbtDataWrapper;
    }

    /**
     * Create itemStack, call it after you added all data you want
     * on the item.
     *
     * @return new itemStack with amount of 1 if you not set it.
     */
    public ItemStack makeItemStack() {
        final ItemStack itemstack = checkTypeOfItem();

        return createItem(itemstack);
    }

    /**
     * Create itemStack array, call it after you added all data you want
     * on the item.
     *
     * @return new itemStack array with amount of 1 if you not set it.
     */
    public ItemStack[] makeItemStackArray() {
        ItemStack itemstack = null;
        final List<ItemStack> list = new ArrayList<>();

        if (this.itemArray != null)
            for (final Object itemStringName : this.itemArray) {
                itemstack = checkTypeOfItem(itemStringName);
                if (itemstack == null) continue;
                list.add(createItem(itemstack));
            }
        return itemstack != null ? list.toArray(new ItemStack[0]) : new ItemStack[]{new ItemStack(Material.AIR)};
    }

    /**
     * Check if the material is an air block.
     *
     * @param material material to check.
     * @return True if this material is an air block.
     */
    public boolean isAir(final Material material) {
        switch (material) {
            case AIR:
            case CAVE_AIR:
            case VOID_AIR:
                // ----- Legacy Separator -----
            case LEGACY_AIR:
                return true;
            default:
                return false;
        }
    }

    /**
     * If it shall display the enchantments on the item.
     *
     * @return true if it shall show the enchantments.
     */
    public boolean isShowEnchantments() {
        MetaHandler handler = this.getMetaHandler();
        if (handler == null) return false;

        final EnhancementMeta enhancements = handler.getEnhancements();
        if (enhancements == null)
            return false;
        return enhancements.isShowEnchantments();
    }

    /**
     * The class that translate the item-stack depending on what for type of
     * item provided, like if it strings,material or the actual item-stack.
     *
     * @return the ConvertToItemStack instance.
     */
    public ConvertToItemStack getConvertItems() {
        return convertItems;
    }


    @Nonnull
    private ItemStack createItem(final ItemStack itemstack) {
        if (itemstack == null) return new ItemStack(Material.AIR);
        ItemStack itemStackNew = itemstack;
        if (!this.keepOldMeta) {
            itemStackNew = new ItemStack(itemstack.getType());
            if (this.keepAmount)
                itemStackNew.setAmount(itemstack.getAmount());
        }
        if (this.isCopyOfItem() && this.keepOldMeta) {
            itemStackNew = new ItemStack(itemStackNew);
        }

        itemStackNew = getItemStack(itemStackNew);
        return itemStackNew;
    }

    @Nonnull
    private ItemStack getItemStack(@Nonnull ItemStack itemStack) {
        if (!isAir(itemStack.getType())) {
            itemStack = setNbt(itemStack);

            final ItemMeta itemMeta = itemStack.getItemMeta();
            setMetadata(itemStack, itemMeta);
            if (this.metaHandler != null)
                this.metaHandler.applyMeta(itemStack, itemMeta);
            itemStack.setItemMeta(itemMeta);

            if (!this.keepAmount)
                itemStack.setAmount(this.amountOfItems <= 0 ? 1 : this.amountOfItems);
        }
        return itemStack;
    }

    private ItemStack setNbt(ItemStack itemStack) {
        NBTDataWrapper dataWrapper = this.getNbtDataWrapper();
        if (dataWrapper != null)
            itemStack = dataWrapper.applyNBT(itemStack);
        return itemStack;
    }

    private void setMetadata(ItemStack itemStack, final ItemMeta itemMeta) {
        if (itemMeta != null) {
            if (this.displayName != null) {
                itemMeta.setDisplayName(translateColors(this.displayName));
            }
            if (this.loreList != null && !this.loreList.isEmpty()) {
                itemMeta.setLore(translateColors(this.loreList));
            }
            addItemMeta(itemStack, itemMeta);
        }
    }


    private ItemStack checkTypeOfItem() {
        ItemBuilder builder = this.itemBuilder;
        if (builder.isItemSet()) {
            ItemStack result = null;
            if (builder.getItemStack() != null) {
                result = builder.getItemStack();
            }
            ConvertToItemStack convertToItemStack = this.getConvertItems();
            if (builder.getMaterial() != null) {
                if (serverVersion > 12.2f) {
                    result = new ItemStack(builder.getMaterial());
                } else {
                    result = convertToItemStack.checkItem(builder.getMaterial(), this.getDamage(), this.color, this.getData());
                }
            }
            if (builder.getStringItem() != null) {
                result = convertToItemStack.checkItem(builder.getStringItem(), this.getDamage(), this.color, this.getData());
            }
            return result;
        }
        return null;
    }

    private ItemStack checkTypeOfItem(final Object object) {
        return getConvertItems().checkItem(object);
    }

    private void addItemMeta(@Nonnull final ItemStack itemStack,@Nonnull final ItemMeta itemMeta) {
        this.setDamageMeta(itemStack, itemMeta);
        if (this.serverVersion > 10.0F)
            setUnbreakableMeta(itemMeta);
        this.addCustomModelData(itemMeta);

        if (isShowEnchantments() || !this.getItemFlags().isEmpty() || this.isGlow())
            hideEnchantments(itemMeta);
    }

    private void setDamageMeta(final ItemStack itemStack,final ItemMeta itemMeta) {
        short dmg = getDmg(itemMeta);
        if (dmg > 0) {
            if (serverVersion > 12.2F) {
                ((Damageable) itemMeta).setDamage(dmg);
            } else {
                itemStack.setDurability(dmg);
            }
        }
    }

    private short getDmg(@Nonnull final ItemMeta itemMeta) {
        final MetaHandler handler = getMetaHandler();
        if (itemMeta instanceof PotionMeta && handler != null) {
            final BottleEffectMeta bottleEffect = handler.getBottleEffect();

            if (bottleEffect != null && bottleEffect.getPotionEffects().isEmpty()) {
                final PotionTypeWrapper potionTypeWrapper = bottleEffect.getPotionTypeWrapper();
                if (potionTypeWrapper != null) {
                    return potionTypeWrapper.toLegacyDamage();
                }
            }
        }
        return this.getDamage();
    }

    private void hideEnchantments(final ItemMeta itemMeta) {
        for (ItemFlag itemFlag : this.getItemFlags()) {
            itemMeta.addItemFlags(itemFlag);
        }
    }


    private void setUnbreakableMeta(final ItemMeta itemMeta) {
        itemMeta.setUnbreakable(isUnbreakable());
    }

    private void addCustomModelData(final ItemMeta itemMeta) {
        if (this.getCustomModelData() > 0)
            itemMeta.setCustomModelData(this.getCustomModelData());
    }

    private List<String> translateColors(final List<String> rawLore) {
        if (!this.enableColorTranslation) {
            return new ArrayList<>(rawLore);
        }
        final List<String> listOfLore = new ArrayList<>();
        for (final String lore : rawLore)
            if (lore != null)
                listOfLore.add(setColors(lore));
        return listOfLore;
    }

    private String translateColors(final String rawSingleLine) {
        if (!this.enableColorTranslation) {
            return rawSingleLine;
        }
        return setColors(rawSingleLine);
    }

    private String setColors(final String rawSingleLine) {
        if (haveTextTranslator)
            return TextTranslator.toSpigotFormat(rawSingleLine);
        return ChatColor.translateAlternateColorCodes('&', rawSingleLine);
    }

    @Nullable
    private MetaHandler getMetaHandler() {
        return this.metaHandler;
    }

    @Nonnull
    private MetaHandler getOrCreateMetaHandler() {
        if (this.metaHandler == null) {
            this.metaHandler = new MetaHandler();
        }
        return this.metaHandler;
    }

    @Nonnull
    private Map<String, Object> getNBTdataMap() {
        NBTDataWrapper meta = getNbtDataWrapper();
        if (meta != null)
            return meta.getMetaDataMap();
        return new HashMap<>();
    }


}
