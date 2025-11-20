package org.broken.arrow.library.itemcreator;


import net.md_5.bungee.api.ChatColor;
import org.broken.arrow.library.color.TextTranslator;
import org.broken.arrow.library.itemcreator.meta.BottleEffectMeta;
import org.broken.arrow.library.itemcreator.meta.enhancement.EnhancementMeta;
import org.broken.arrow.library.itemcreator.meta.MetaHandler;
import org.broken.arrow.library.itemcreator.meta.enhancement.EnhancementWrapper;
import org.broken.arrow.library.itemcreator.meta.potion.PotionTypeWrapper;
import org.broken.arrow.library.itemcreator.utility.ConvertToItemStack;
import org.broken.arrow.library.itemcreator.utility.Tuple;
import org.broken.arrow.library.itemcreator.utility.builders.ItemBuilder;
import org.broken.arrow.library.itemcreator.utility.nbt.NBTDataWriter;
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
     * Adds or removes the visual "glow" effect on this item while keeping the
     * enchantments hidden. This method is strictly for the visual effect and
     * does not manage custom enchantments.
     *
     * <p>To add actual enchantments, use
     * {@link #setItemMeta(Consumer)} or {@link #setItemMeta(Function)} together with
     * {@link MetaHandler#setEnhancements(Consumer)}, for example:
     *
     * <ul>
     *   <li>{@link EnhancementMeta#addEnchantments(String...)}</li>
     *   <li>{@link EnhancementMeta#setEnchantment(Enchantment, Consumer)}</li>
     *   <li>{@link EnhancementMeta#addEnchantments(EnhancementWrapper...)}</li>
     * </ul>
     *
     * @param glow {@code true} to apply the glowing effect, {@code false} to remove it
     * @return this instance for chaining
     */
    public CreateItemStack setGlow(final boolean glow) {
        this.glow = glow;
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
     * Sets a custom NBT application function to be used when applying NBT data.
     * Compared to {@link #setItemMetaData(String, Object, boolean)} and
     * {@link #setItemMetaData(String, Object)}, this gives you greater control over
     * which metadata is applied and also allows removing keys.
     *
     * @param function a consumer that modifies the provided {@link NBTDataWriter}
     * @return this instance
     */
    public CreateItemStack setItemNBT(Consumer<NBTDataWriter> function) {
        createNBTWrapperIfMissing();
        nbtDataWrapper.applyNBT(function);
        return this;
    }

    /**
     * Sets custom NBT data on the item.
     *
     * <p><b>Note:</b> This method is less flexible and the naming is misleading, and it may be
     * removed in the future. Prefer {@link #setItemNBT(java.util.function.Consumer)} when
     * working with multiple values, custom logic, or when you need to remove keys.</p>
     *
     * @param itemMetaKey   the key used to retrieve the value
     * @param itemMetaValue the value to set
     * @return this instance
     */
    public CreateItemStack setItemMetaData(final String itemMetaKey, final Object itemMetaValue) {
        return setItemMetaData(itemMetaKey, itemMetaValue, false);
    }

    /**
     * Sets custom NBT data on the item.
     *
     * <p><b>Note:</b> This method is less flexible and the naming is misleading, and it may be
     * removed in the future. Prefer {@link #setItemNBT(java.util.function.Consumer)} when
     * working with multiple values, custom logic, or when you need to remove keys.</p>
     *
     * @param itemMetaKey   the key used to retrieve the value
     * @param itemMetaValue the value to set
     * @param keepClazz     {@code true} to keep the original value type, or {@code false} to convert
     *                      the value to a string
     * @return this instance
     */
    public CreateItemStack setItemMetaData(final String itemMetaKey, final Object itemMetaValue, final boolean keepClazz) {
        createNBTWrapperIfMissing();
        nbtDataWrapper.add(itemMetaKey, itemMetaValue, keepClazz);
        return this;
    }

    /**
     * Map list of metadata you want to set on a item.
     * It uses map key and value form the map.
     *
     * <p><b>Note:</b> This method is less flexible and the naming is misleading, and it may be
     * removed in the future. Prefer {@link #setItemNBT(java.util.function.Consumer)} when
     * working with multiple values, custom logic, or when you need to remove keys.</p>
     *
     * @param itemMetaMap map of values.
     * @return this class.
     */
    public CreateItemStack setItemMetaDataList(final Map<String, Object> itemMetaMap) {
        if (itemMetaMap != null && !itemMetaMap.isEmpty()) {
            createNBTWrapperIfMissing();
            final NBTDataWrapper wrapper = this.nbtDataWrapper;
            for (final Map.Entry<String, Object> itemData : itemMetaMap.entrySet()) {
                wrapper.add(itemData.getKey(), itemData.getValue());
            }
        }
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
     * Set color propitiates for the item. Mostly used for legacy
     * to tell color of for example glass and concrete.
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

    private void addItemMeta(@Nonnull final ItemStack itemStack, @Nonnull final ItemMeta itemMeta) {
        this.setDamageMeta(itemStack, itemMeta);
        if (this.serverVersion > 10.0F)
            setUnbreakableMeta(itemMeta);
        this.addCustomModelData(itemMeta);

        if (isShowEnchantments() || !this.getItemFlags().isEmpty() || this.isGlow())
            hideEnchantments(itemMeta);
    }

    private void setDamageMeta(final ItemStack itemStack, final ItemMeta itemMeta) {
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

    private void createNBTWrapperIfMissing() {
        if(nbtDataWrapper == null)
            nbtDataWrapper = NBTDataWrapper.of(this.itemCreator);
    }


}
