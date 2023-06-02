package org.broken.arrow.itemcreator.library;


import de.tr7zw.changeme.nbtapi.metodes.RegisterNbtAPI;
import org.broken.arrow.itemcreator.library.utility.ConvertToItemStack;
import org.broken.arrow.itemcreator.library.utility.ServerVersion;
import org.broken.arrow.itemcreator.library.utility.Tuple;
import org.broken.arrow.itemcreator.library.utility.Validate;
import org.broken.arrow.itemcreator.library.utility.builders.ItemBuilder;
import org.broken.lib.rbg.TextTranslator;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.broken.arrow.itemcreator.library.ItemCreator.getNbtApi;


/**
 * Create items with your set data. When you make a item it will also detect minecraft version
 * and provide help when make items for diffrent minecraft versions (you can ether let it auto convert colors depending
 * on version or hardcode it self).
 */

public class CreateItemStack {
	static Logger logger;

	static {
		logger = Logger.getLogger("itemcreator");
	}

	private final Object item;
	private String rgb;
	private final Iterable<?> itemArray;
	private final String displayName;
	private String color;
	private final List<String> lore;
	private final Map<Enchantment, Tuple<Integer, Boolean>> enchantments = new HashMap<>();
	private final List<ItemFlag> visibleItemFlags = new ArrayList<>();
	private final List<ItemFlag> flagsToHide = new ArrayList<>();
	private final List<Pattern> pattern = new ArrayList<>();
	private final List<PotionEffect> portionEffects = new ArrayList<>();
	private final List<FireworkEffect> fireworkEffects = new ArrayList<>();
	private MetaDataWraper metadata;
	private int amoutOfItems;
	private int red = -1;
	private int green = -1;
	private int blue = -1;
	private short data = -1;
	private int customModeldata = -1;
	private boolean glow;
	private boolean showEnchantments;
	private boolean waterBottle;
	private boolean unbreakable;
	private boolean keepAmount;
	private boolean keepOldMeta = true;
	private boolean copyOfItem;
	private static ConvertToItemStack convertItems;
	private final RegisterNbtAPI nbtApi;

	public CreateItemStack(final ItemBuilder itemBuilder) {
		if (convertItems == null)
			convertItems = new ConvertToItemStack();
		this.item = itemBuilder.getItem();
		this.itemArray = itemBuilder.getItemArray();
		this.displayName = itemBuilder.getDisplayName();
		this.lore = itemBuilder.getLore();
		this.nbtApi = getNbtApi();

	}

	/**
	 * Amount of items you want to create.
	 *
	 * @param amoutOfItems item amount.
	 * @return this class.
	 */
	public CreateItemStack setAmoutOfItems(final int amoutOfItems) {
		this.amoutOfItems = amoutOfItems;
		return this;
	}

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
		return pattern;
	}

	/**
	 * Add one or several patterns.
	 *
	 * @param patterns to add to the list.
	 * @return this class.
	 */
	public CreateItemStack addPattern(final Pattern... patterns) {
		if (patterns == null || patterns.length < 1) return this;

		this.pattern.addAll(Arrays.asList(patterns));
		return this;
	}

	/**
	 * Add list of patterns (if it exist old patterns in the list, will the new ones be added ontop).
	 *
	 * @param pattern list some contains patterns.
	 * @return this class.
	 */
	public CreateItemStack addPattern(final List<Pattern> pattern) {

		this.pattern.addAll(pattern);
		return this;
	}

	/**
	 * Get enchantments for this item.
	 *
	 * @return map with enchantment level and if it shall ignore level reestriction.
	 */
	public Map<Enchantment, Tuple<Integer, Boolean>> getEnchantments() {
		return enchantments;
	}

	/**
	 * Check if it water Bottle. Becuse
	 * only exist matrial portion, so need this method.
	 *
	 * @return true if it a water Bottle item.
	 */
	public boolean isWaterBottle() {
		return waterBottle;
	}

	public CreateItemStack setWaterBottle(final boolean waterBottle) {
		this.waterBottle = waterBottle;
		return this;
	}

	/**
	 * If it shall keep the old amount of items (if you modify old itemstack).
	 *
	 * @return true if you keep old amunt.
	 */
	public boolean isKeepAmount() {
		return keepAmount;
	}

	/**
	 * Set if you want to keep old amount.
	 *
	 * @param keepAmount set it to true if you want keep old amount.
	 * @return
	 */
	public CreateItemStack setKeepAmount(final boolean keepAmount) {
		this.keepAmount = keepAmount;
		return this;
	}

	/**
	 * if it shall keep old medatada (only work if you modify old itemstack).
	 * Defult it will keep the meta.
	 *
	 * @return true if you keep old meta.
	 */
	public boolean isKeepOldMeta() {
		return keepOldMeta;
	}

	/**
	 * Set if it shall keep the old metadata or not.
	 * Defult it will keep the meta.
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
	 * @return list of efects set on this item.
	 */
	public List<FireworkEffect> getFireworkEffects() {
		return fireworkEffects;
	}

	/**
	 * Add firework effects on this item.
	 *
	 * @param fireworkEffects list of effects you want to add.
	 */
	public void setFireworkEffects(final List<FireworkEffect> fireworkEffects) {
		fireworkEffects.addAll(fireworkEffects);
	}

	/**
	 * Add own enchantments. Set {@link #setShowEnchantments(boolean)} to true
	 * if you whant to hide all enchants (defult so will it not hide enchants).
	 * <p>
	 * This method uses varargs and add it to list, like this enchantment;level;levelRestriction or
	 * enchantment;level and it will sett last one to false.
	 * <p>
	 * <p>
	 * Example usage here:
	 * "PROTECTION_FIRE;1;false","PROTECTION_EXPLOSIONS;15;true","WATER_WORKER;1;false".
	 *
	 * @param enchantments list of enchantments you want to add.
	 * @return this class.
	 */

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
	 */

	public CreateItemStack addEnchantments(final Enchantment... enchantments) {
		for (final Enchantment enchant : enchantments) {
			addEnchantments(enchant, true, 1);
		}
		return this;
	}

	/**
	 * Add own enchantments. Set {@link #setShowEnchantments(boolean)} to true
	 * if you whant to hide all enchants (defult so will it not hide enchants).
	 *
	 * @param enchantmentMap add direcly a map with enchants and level and levelRestrictions.
	 * @param override       the old value in the map if you set it to true.
	 * @return this class.
	 */
	public CreateItemStack addEnchantments(final Map<Enchantment, Tuple<Integer, Boolean>> enchantmentMap, final boolean override) {
		Validate.checkNotNull(enchantmentMap, "this map is null");
		if (enchantmentMap.isEmpty())
			logger.log(Level.INFO, "This map is empty so no enchantments vill be added");

		enchantmentMap.forEach((key, value) -> {
			if (!override)
				this.enchantments.putIfAbsent(key, value);
			else
				this.enchantments.put(key, value);
		});
		return this;
	}

	/**
	 * Add own enchantments. Set {@link #setShowEnchantments(boolean)} to true
	 * if you whant to hide all enchants (defult so will it not hide enchants).
	 *
	 * @param enchant          enchantments you want to add, suport string and Enchantment class.
	 * @param levelRestriction bypass the level limit.
	 * @param enchantmentLevel set level for this enchantment.
	 * @return this class.
	 */

	public CreateItemStack addEnchantments(final Object enchant, final boolean levelRestriction, final int enchantmentLevel) {
		Enchantment enchantment = null;
		if (enchant instanceof String)
			enchantment = Enchantment.getByKey(NamespacedKey.minecraft((String) enchant));
		else if (enchant instanceof Enchantment)
			enchantment = (Enchantment) enchant;

		if (enchantment != null)
			this.enchantments.put(enchantment, new Tuple<>(enchantmentLevel, levelRestriction));
		else
			logger.log(Level.INFO, "your enchantment: " + enchant + " ,are not valid.");

		return this;
	}


	/**
	 * When use {@link #addEnchantments(Object, boolean, int)}   or {@link #addEnchantments(String...)} and
	 * want to not show enchants set it to true. When use {@link #setGlow(boolean)} it will defult hide
	 * enchants, if you set #setGlow to true and set this to true it will show the enchantments.
	 *
	 * @param showEnchantments true and will show enchants.
	 * @return this class.
	 */
	public CreateItemStack setShowEnchantments(final boolean showEnchantments) {
		this.showEnchantments = showEnchantments;
		return this;
	}

	/**
	 * Set custom metadata on item.
	 *
	 * @param itemMetaKey   key for get value.
	 * @param itemMetaValue value you want to set.
	 * @return this class.
	 */
	public CreateItemStack setItemMetaData(final String itemMetaKey, final Object itemMetaValue) {
		return setItemMetaData(itemMetaKey, itemMetaValue, false);
	}

	/**
	 * Set custom metadata on item.
	 *
	 * @param itemMetaKey   key for get value.
	 * @param itemMetaValue value you want to set.
	 * @param keepclazz     true if it shall keep all data on the item or false to convert value to string.
	 * @return this class.
	 */
	public CreateItemStack setItemMetaData(final String itemMetaKey, final Object itemMetaValue, final boolean keepclazz) {
		metadata = MetaDataWraper.of().add(itemMetaKey, itemMetaValue, keepclazz);
		return this;
	}

	/**
	 * Set your metadata on the item. Use {@link MetaDataWraper} class.
	 * To set key and value.
	 *
	 * @param wraper values from MetaDataWraper.
	 * @return this class.
	 */
	public CreateItemStack setItemMetaDataList(final MetaDataWraper wraper) {
		metadata = wraper;
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
			final MetaDataWraper wraper = MetaDataWraper.of();
			for (final Map.Entry<String, Object> itemdata : itemMetaMap.entrySet()) {
				wraper.add(itemdata.getKey(), itemdata.getValue());
			}
			metadata = wraper;
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
	 * Old methoid to set data on a item.
	 *
	 * @return number.
	 */
	public short getData() {
		return data;
	}

	/**
	 * Set data on a item, this only suport short
	 * so it is a limit of how high you can set it.
	 *
	 * @param data the number you want to set.
	 * @return this class.
	 */
	public CreateItemStack setData(final short data) {
		this.data = data;
		return this;
	}

	/**
	 * Get Custom Modeldata on the item. use this insted of set data on a item.
	 *
	 * @return Modeldata number.
	 */

	public int getCustomModeldata() {
		return customModeldata;
	}

	/**
	 * Set Custom Modeldata on a item. will work on newer minecraft versions.
	 *
	 * @param customModeldata number.
	 * @return this class.
	 */
	public CreateItemStack setCustomModeldata(final int customModeldata) {
		this.customModeldata = customModeldata;
		return this;
	}

	/**
	 * Get all portions effects for this item.
	 *
	 * @return list of portions effects.
	 */
	public List<PotionEffect> getPortionEffects() {
		return portionEffects;
	}

	/**
	 * Add one or several portions effects to list.
	 *
	 * @param potionEffects you want to set on the item.
	 * @return this class.
	 */
	public CreateItemStack addPortionEffects(final PotionEffect... potionEffects) {
		if (potionEffects.length == 0) return this;

		portionEffects.addAll(Arrays.asList(potionEffects));
		return this;
	}

	/**
	 * Add a list of effects to the list. If it exist old effects this will add the effects ontop of the old ones.
	 *
	 * @param potionEffects list of effects you want to add.
	 * @return this class.
	 */
	public CreateItemStack addPortionEffects(final List<PotionEffect> potionEffects) {
		if (potionEffects.isEmpty()) {
			logger.log(Level.INFO, "This list of portion effects is empty so no values vill be added");
			return this;
		}

		portionEffects.addAll(potionEffects);
		return this;
	}

	/**
	 * Set a list of effects to the list. If it exist old effects in the list, this will be removed.
	 *
	 * @param potionEffects list of effects you want to set.
	 * @return this class.
	 */
	public CreateItemStack setPortionEffects(final List<PotionEffect> potionEffects) {
		if (potionEffects.isEmpty()) {
			logger.log(Level.INFO, "This list of portion effects is empty so no values vill be added");
			return this;
		}
		portionEffects.clear();
		portionEffects.addAll(potionEffects);
		return this;
	}

	/**
	 * Get the rbg colors, used to dye lether armor,potions and fierworks.
	 *
	 * @return string with the colors, like this #,#,#.
	 */
	public String getRgb() {
		return rgb;
	}

	/**
	 * Set the 3 colors auto.
	 *
	 * @param rgb string need to be formated like this #,#,#.
	 * @return this class.
	 */
	public CreateItemStack setRgb(final String rgb) {
		this.rgb = rgb;

		final String[] colors = this.getRgb().split(",");
		Validate.checkBoolean(colors.length < 4, "rgb is not format correcly. Should be formated like this 'r,b,g'. Example '20,15,47'.");
		try {
			red = Integer.parseInt(colors[0]);
			green = Integer.parseInt(colors[2]);
			blue = Integer.parseInt(colors[1]);
		} catch (final NumberFormatException exception) {
			logger.log(Level.WARNING, "you don´t use numbers inside this " + rgb);
			exception.printStackTrace();
		}

		return this;
	}

	/**
	 * Get red color
	 *
	 * @return color number.
	 */
	public int getRed() {
		return red;
	}

	/**
	 * Get greencolor
	 *
	 * @return color number.
	 */
	public int getGreen() {
		return green;
	}

	/**
	 * Get blue color
	 *
	 * @return color number.
	 */
	public int getBlue() {
		return blue;
	}

	/**
	 * Hide one or several metadata values on a itemstack.
	 *
	 * @param itemFlags add one or several flags you not want to hide.
	 * @return this class.
	 */
	public CreateItemStack setItemFlags(final ItemFlag... itemFlags) {
		this.setItemFlags(Arrays.asList(itemFlags));
		return this;
	}

	/**
	 * Don´t hide one or several metadata values on a itemstack.
	 *
	 * @param itemFlags add one or several flags you not want to hide.
	 * @return this class.
	 */
	public CreateItemStack setItemFlags(final List<ItemFlag> itemFlags) {
		Validate.checkNotNull(itemFlags, "flags list is null");
		this.visibleItemFlags.addAll(itemFlags);
		return this;
	}

	/**
	 * Hide one or several metadata values on a itemstack.
	 *
	 * @param itemFlags add one or several flags you want to hide.
	 * @return this class.
	 */
	public CreateItemStack setFlagsToHide(final List<ItemFlag> itemFlags) {
		Validate.checkNotNull(itemFlags, "flags list is null");
		this.flagsToHide.addAll(itemFlags);
		return this;
	}

	/**
	 * Get the list of flags set on this item.
	 *
	 * @return list of flags.
	 */
	public List<ItemFlag> getFlagsToHide() {
		return flagsToHide;
	}

	/**
	 * Get if it has create copy of item or not.
	 *
	 * @return true if it shall make copy of orginal item.
	 */

	public boolean isCopyOfItem() {
		return copyOfItem;
	}

	/**
	 * If it shall create copy of the item or change orginal item.
	 *
	 * @param copyItem true if you want to create copy.
	 * @return this class.
	 */
	public CreateItemStack setCopyOfItem(final boolean copyItem) {
		this.copyOfItem = copyItem;
		return this;
	}

	public CreateItemStack setColor(String colorName) {
		this.color = colorName;
		return this;
	}

	/**
	 * Create itemstack, call it after you added all data you want
	 * on the item.
	 *
	 * @return new itemstack with amount of 1 if you not set it.
	 */
	public ItemStack makeItemStack() {
		final ItemStack itemstack = checkTypeOfItem();

		return createItem(itemstack);
	}

	/**
	 * Create itemstack array, call it after you added all data you want
	 * on the item.
	 *
	 * @return new itemstack array with amount of 1 if you not set it.
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

	@Nonnull
	private ItemStack createItem(final ItemStack itemstack) {
		if (itemstack == null) return new ItemStack(Material.AIR);
		ItemStack itemstackNew = itemstack;
		final RegisterNbtAPI nbtApi = this.nbtApi;
		if (!this.keepOldMeta) {
			itemstackNew = new ItemStack(itemstack.getType());
			if (this.keepAmount)
				itemstackNew.setAmount(itemstack.getAmount());
		}
		if (this.isCopyOfItem() && this.keepOldMeta) {
			itemstackNew = new ItemStack(itemstackNew);
		}

		if (!isAir(itemstackNew.getType())) {
			if (nbtApi != null) {
				final Map<String, Object> metadataMap = this.getMetadataMap();
				if (metadataMap != null)
					for (final Map.Entry<String, Object> entitys : metadataMap.entrySet()) {
						itemstackNew = nbtApi.getCompMetadata().setMetadata(itemstackNew, entitys.getKey(), entitys.getValue());
					}
			}
			final ItemMeta itemMeta = itemstackNew.getItemMeta();
			if (itemMeta != null) {
				if (this.displayName != null) {
					itemMeta.setDisplayName(translateColors(this.displayName));
				}
				if (this.lore != null && !this.lore.isEmpty()) {
					itemMeta.setLore(translateColors(this.lore));
				}
				addItemMeta(itemMeta);
			}
			itemstackNew.setItemMeta(itemMeta);
			if (!this.keepAmount)
				itemstackNew.setAmount(this.amoutOfItems <= 0 ? 1 : this.amoutOfItems);
		}
		return itemstackNew;
	}

	/**
	 * Check if the material is an air block.
	 *
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

	private ItemStack checkTypeOfItem() {
		Object object = this.item;
		if (object != null) {
			if (object instanceof ItemStack)
				return (ItemStack) object;
			if (object instanceof Material)
				return new ItemStack((Material) object);
			if (object instanceof String) {
				if (this.color != null)
					return getConvertItems().checkItem(object, this.color);
				return this.checkTypeOfItem(object);
			}
		}
		return null;
	}

	private ItemStack checkTypeOfItem(final Object object) {
		return getConvertItems().checkItem(object);
	}

	private void addItemMeta(final ItemMeta itemMeta) {
		addBannerPatterns(itemMeta);
		addLeatherArmorColors(itemMeta);
		addFireworkEffect(itemMeta);
		addEnchantments(itemMeta);
		addBottleEffects(itemMeta);
		if (ServerVersion.newerThan(ServerVersion.v1_10))
			addUnbreakableMeta(itemMeta);
		addCustomModelData(itemMeta);

		if (isShowEnchantments() || !this.getFlagsToHide().isEmpty() || this.isGlow())
			hideEnchantments(itemMeta);
	}

	private void hideEnchantments(final ItemMeta itemMeta) {
		if (!this.getFlagsToHide().isEmpty()) {
			itemMeta.addItemFlags(this.getFlagsToHide().toArray(new ItemFlag[0]));
		} else {
			itemMeta.addItemFlags(Arrays.stream(ItemFlag.values()).filter(itemFlag -> !visibleItemFlags.contains(itemFlag)).toArray(ItemFlag[]::new));
		}
	}

	public boolean addEnchantments(final ItemMeta itemMeta) {
		if (!this.getEnchantments().isEmpty()) {
			boolean haveEnchant = false;
			for (final Map.Entry<Enchantment, Tuple<Integer, Boolean>> enchant : this.getEnchantments().entrySet()) {
				if (enchant == null) {
					logger.log(Level.INFO, "Your enchantment are null.");
					continue;
				}
				final Tuple<Integer, Boolean> level = enchant.getValue();
				haveEnchant = itemMeta.addEnchant(enchant.getKey(), level.getFirst() <= 0 ? 1 : level.getFirst(), level.getSecond());
			}
			if (isShowEnchantments() || !this.getFlagsToHide().isEmpty())
				hideEnchantments(itemMeta);
			return haveEnchant;
		} else if (this.isGlow()) {
			return itemMeta.addEnchant(Enchantment.SILK_TOUCH, 1, false);
		}
		return false;
	}

	private void addBannerPatterns(final ItemMeta itemMeta) {
		if (getPattern() == null || getPattern().isEmpty())
			return;

		if (itemMeta instanceof BannerMeta) {
			final BannerMeta bannerMeta = (BannerMeta) itemMeta;
			bannerMeta.setPatterns(getPattern());
		}
	}

	private void addLeatherArmorColors(final ItemMeta itemMeta) {
		if (getRgb() == null || getRed() < 0)
			return;

		if (itemMeta instanceof LeatherArmorMeta) {
			final LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemMeta;
			leatherArmorMeta.setColor(Color.fromBGR(getBlue(), getGreen(), getRed()));
		}
	}

	private void addBottleEffects(final ItemMeta itemMeta) {

		if (itemMeta instanceof PotionMeta) {
			final PotionMeta potionMeta = (PotionMeta) itemMeta;

			if (isWaterBottle()) {
				final PotionData potionData = new PotionData(PotionType.WATER);
				potionMeta.setBasePotionData(potionData);
				return;
			}
			if (getPortionEffects() == null || getPortionEffects().isEmpty()) {
				return;
			}
			if (getRgb() == null || (getRed() < 0 && getGreen() < 0 && getBlue() < 0)) {
				logger.log(Level.WARNING, "You have not set colors correctly, you have set this: " + getRgb() + " should be in this format Rgb: #,#,#");
				return;
			}
			potionMeta.setColor(Color.fromBGR(getBlue(), getGreen(), getRed()));
			getPortionEffects().forEach((portionEffect) -> potionMeta.addCustomEffect(portionEffect, true));

		}
	}

	private void addFireworkEffect(final ItemMeta itemMeta) {
		if (getRgb() == null || (getRed() < 0 && getGreen() < 0 && getBlue() < 0))
			return;

		if (itemMeta instanceof FireworkEffectMeta) {
			if (getRgb() == null || (getRed() < 0 && getGreen() < 0 && getBlue() < 0)) {
				logger.log(Level.WARNING, "You have not set colors correctly, you have set this: " + getRgb() + " should be in this format Rgb: #,#,#");
				return;
			}
			final FireworkEffectMeta fireworkEffectMeta = (FireworkEffectMeta) itemMeta;
			final FireworkEffect.Builder builder = FireworkEffect.builder();
			builder.withColor(Color.fromBGR(getBlue(), getGreen(), getRed()));
			if (this.getFireworkEffects() != null && !this.getFireworkEffects().isEmpty()) {
				this.getFireworkEffects().get(0).getFadeColors();
				builder.flicker(false)
						.with(FireworkEffect.Type.BURST)
						.trail(true)
						.withFade();
			}

			fireworkEffectMeta.setEffect(builder.build());
		}
	}

	private void addUnbreakableMeta(final ItemMeta itemMeta) {
		itemMeta.setUnbreakable(isUnbreakable());
	}

	private void addCustomModelData(final ItemMeta itemMeta) {
		if (this.getCustomModeldata() > 0)
			itemMeta.setCustomModelData(this.getCustomModeldata());
	}

	private boolean isShowEnchantments() {
		return showEnchantments;
	}


	private List<String> translateColors(final List<String> rawLore) {
		final List<String> lores = new ArrayList<>();
		for (final String lore : rawLore)
			if (lore != null)
				lores.add(TextTranslator.toSpigotFormat(lore));
		return lores;
	}

	private String translateColors(final String rawSingelLine) {
		return TextTranslator.toSpigotFormat(rawSingelLine);
	}

	
	public static ConvertToItemStack getConvertItems() {
		if (convertItems == null)
			convertItems = new ConvertToItemStack();
		return convertItems;
	}


	public static List<String> formatColors(final List<String> rawLore) {
		final List<String> lores = new ArrayList<>();
		for (final String lore : rawLore)
			lores.add(translateHexCodes(lore));
		return lores;
	}

	public static String formatColors(final String rawSingelLine) {
		return translateHexCodes(rawSingelLine);
	}

	private static String translateHexCodes(final String textTranslate) {

		return TextTranslator.toSpigotFormat(textTranslate);
	}

	private MetaDataWraper getMetadata() {
		return metadata;
	}

	private Map<String, Object> getMetadataMap() {
		if (metadata != null)
			return metadata.getMetaDataMap();
		return null;
	}


}
