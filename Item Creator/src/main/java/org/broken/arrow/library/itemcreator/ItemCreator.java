package org.broken.arrow.library.itemcreator;


import org.broken.arrow.library.color.TextTranslator;
import org.broken.arrow.library.itemcreator.utility.ConvertToItemStack;
import org.broken.arrow.library.itemcreator.utility.ServerVersion;
import org.broken.arrow.library.itemcreator.utility.builders.ItemBuilder;
import org.broken.arrow.library.logging.Validate;
import org.broken.arrow.library.logging.Validate.ValidateExceptions;
import org.broken.arrow.library.nbt.RegisterNbtAPI;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * A utility class to create and manipulate ItemStacks with ease.
 * Supports setting display names, lore, NBT data, and color translation.
 */
public class ItemCreator {

    private static ServerVersion serverVersion;
    private final NBTManger nbtManger;
    private final ConvertToItemStack convertItems;
    private final Plugin plugin;
    private boolean haveTextTranslator = true;
    private boolean enableColorTranslation = true;

    /**
     * Prevents empty constructor usage.
     * Throws an exception if called.
     */
    private ItemCreator() {
        throw new ValidateExceptions("should not use empty constructor");
    }

    /**
     * Constructs an ItemCreator instance associated with the given plugin.
     *
     * @param plugin the plugin instance
     */
    public ItemCreator(final Plugin plugin) {
        this(plugin, false);
    }

    /**
     * Constructs an ItemCreator instance associated with the given plugin.
     *
     * @param plugin        the plugin instance
     * @param turnOffLogger whether to disable logging for NBT manager
     */
    public ItemCreator(final Plugin plugin, boolean turnOffLogger) {
        this.plugin = plugin;
        this.nbtManger = new NBTManger(plugin, turnOffLogger);
        setServerVersion(plugin);

        this.convertItems = new ConvertToItemStack(serverVersion.getServerVersion());

        try {
            TextTranslator.getInstance();
        } catch (NoClassDefFoundError ignore) {
            haveTextTranslator = false;
        }
    }

    /**
     * Starts the creation of a simple item. The item will not have a display name or lore.
     * Complete the creation by calling {@link CreateItemStack#makeItemStack()}.
     *
     * @param item The ItemStack to alter.
     * @return A CreateItemStack instance to build the item.
     */
    public CreateItemStack of(ItemStack item) {
        return of(item, null);
    }

    /**
     * Starts the creation of a simple item. The item will not have a display name or lore.
     * Complete the creation by calling {@link CreateItemStack#makeItemStack()}.
     *
     * @param material The material type of the item.
     * @return A CreateItemStack instance to build the item.
     */
    public CreateItemStack of(Material material) {
        return of(material, null);
    }

    /**
     * Starts the creation of a simple item by item name.
     * The item will not have a display name or lore.
     * Complete the creation by calling {@link CreateItemStack#makeItemStack()}.
     *
     * @param item The name of the item.
     * @return A CreateItemStack instance to build the item.
     */
    public CreateItemStack of(String item) {
        return of(item, null);
    }

    /**
     * Starts the creation of an item with a display name and optional lore.
     * Complete the creation by calling {@link CreateItemStack#makeItemStack()}.
     *
     * @param item        The ItemStack to alter.
     * @param displayName The display name of the item, or null for none.
     * @param lore        The lore of the item, varargs or null.
     * @return A CreateItemStack instance to build the item.
     */
    public CreateItemStack of(ItemStack item, String displayName, String... lore) {
        return of(item, displayName, lore != null ? Arrays.asList(lore) : null);
    }


    /**
     * Starts the creation of an item with a display name and optional lore.
     * Complete the creation by calling {@link CreateItemStack#makeItemStack()}.
     *
     * @param material    The material type of the item.
     * @param displayName The display name of the item, or null for none.
     * @param lore        The lore of the item, varargs or null.
     * @return A CreateItemStack instance to build the item.
     */
    public CreateItemStack of(Material material, String displayName, String... lore) {
        return of(material, displayName, lore != null ? Arrays.asList(lore) : null);
    }

    /**
     * Starts the creation of an item with a display name and optional lore by item name.
     * Complete the creation by calling {@link CreateItemStack#makeItemStack()}.
     *
     * @param itemName    The name of the item.
     * @param displayName The display name of the item, or null for none.
     * @param lore        The lore of the item, varargs or null.
     * @return A CreateItemStack instance to build the item.
     */
    public CreateItemStack of(String itemName, String displayName, String... lore) {
        return of(itemName, displayName, lore != null ? Arrays.asList(lore) : null);
    }

    /**
     * Starts the creation of an item with a display name and lore.
     * Complete the creation by calling {@link CreateItemStack#makeItemStack()}.
     *
     * @param item        The ItemStack to alter.
     * @param displayName The display name of the item, or null.
     * @param lore        The lore of the item, or null.
     * @return A CreateItemStack instance to build the item.
     */
    public CreateItemStack of(ItemStack item, String displayName, List<String> lore) {
        return createStack(item, displayName, lore);
    }

    /**
     * Starts the creation of an item with a display name and lore.
     * Complete the creation by calling {@link CreateItemStack#makeItemStack()}.
     *
     * @param material    The material type of the item.
     * @param displayName The display name of the item, or null.
     * @param lore        The lore of the item, or null.
     * @return A CreateItemStack instance to build the item.
     */
    public CreateItemStack of(Material material, String displayName, List<String> lore) {
        return createStack(material, displayName, lore);
    }

    /**
     * Starts the creation of an item with a display name and lore by item name.
     * Complete the creation by calling {@link CreateItemStack#makeItemStack()}.
     *
     * @param itemName    The name of the item.
     * @param displayName The display name of the item, or null.
     * @param lore        The lore of the item, or null.
     * @return A CreateItemStack instance to build the item.
     */
    public CreateItemStack of(String itemName, String displayName, List<String> lore) {
        return createStack(itemName, displayName, lore);
    }

    /**
     * Creates the item stack based on the given input type, display name, and lore.
     *
     * @param item        The name, Material, or ItemStack of the item.
     * @param displayName The display name of the item, or null.
     * @param lore        The lore of the item, or null.
     * @return A CreateItemStack instance to build the item.
     */
    private CreateItemStack createStack(Object item, String displayName, List<String> lore) {
        ItemBuilder itemBuilder;
        if (item instanceof ItemStack)
            itemBuilder = new ItemBuilder(this, (ItemStack) item, displayName, lore);
        else if (item instanceof Material)
            itemBuilder = new ItemBuilder(this, (Material) item, displayName, lore);
        else
            itemBuilder = new ItemBuilder(this, item + "", displayName, lore);
        return itemBuilder.build();
    }

    /**
     * Starts the creation of an item using an existing ItemBuilder.
     * Complete the creation by calling {@link CreateItemStack#makeItemStack()}.
     *
     * @param itemBuilder The ItemBuilder instance to use.
     * @return A CreateItemStack instance to build the item.
     */
    public CreateItemStack of(ItemBuilder itemBuilder) {
        return itemBuilder.build();
    }

    /**
     * Starts the creation of multiple items from an iterable collection.
     * The display name and lore will be applied to all items if not null.
     * Complete the creation by calling {@link CreateItemStack#makeItemStackArray()}.
     *
     * @param itemArray   The iterable collection of items.
     * @param displayName The display name to apply to all items, or null to keep original.
     * @param lore        The lore to apply to all items, or null to keep original.
     * @param <T>         The type of items in the iterable.
     * @return A CreateItemStack instance to build the items.
     */
    public <T> CreateItemStack of(Iterable<T> itemArray, String displayName, List<String> lore) {
        ItemBuilder itemBuilder = new ItemBuilder(this, itemArray, displayName, lore);
        return itemBuilder.build();
    }

    /**
     * Checks whether color translation is enabled.
     *
     * @return {@code true} if color translation is enabled, {@code false} otherwise.
     */
    public boolean isEnableColorTranslation() {
        return enableColorTranslation;
    }

    /**
     * Enables or disables automatic color translation.
     * By default, color codes in text will be translated automatically.
     *
     * @param enableColorTranslation {@code true} to enable automatic color translation, {@code false} to disable.
     */
    public void setEnableColorTranslation(boolean enableColorTranslation) {
        this.enableColorTranslation = enableColorTranslation;
    }

    /**
     * Gets the NBTManager instance used to manipulate item NBT data.
     *
     * @return the NBTManager instance
     */
    @Nullable
    public RegisterNbtAPI getNbtApi() {
        return nbtManger.getNbtApi();
    }

    /**
     * Gets the ConvertToItemStack instance used for conversions.
     *
     * @return the ConvertToItemStack instance
     */
    public ConvertToItemStack getConvertItems() {
        return convertItems;
    }

    /**
     * Retrieves the plugin instance associated with this module.
     *
     * @return the plugin instance.
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Returns true if the correct module is imported
     * for color translations, it will use bukkits methods
     * to translate colors.
     *
     * @return Returns true if the module exist.
     */
    public boolean isHaveTextTranslator() {
        return haveTextTranslator;
    }


    /**
     * Returns the current server version as a float.
     *
     * @return the server version
     */
    public static float getServerVersion() {
        return serverVersion.getServerVersion();
    }

    /**
     * Return the enchantment's name across Minecraft versions.
     *
     * <p>On Minecraft 1.13 and newer this returns the NamespacedKey key
     * (enchantment.getKey().getKey()). On older versions this returns
     * {@link Enchantment#getName()}, which on legacy servers may be a numeric id string
     * or a legacy name. {@link Enchantment#getByName(String)} can resolve either form.
     *
     * @param enchantment the enchantment to get the name for
     * @return the enchantment name ({@link NamespacedKey#getKey()} on 1.13+, otherwise {@link Enchantment#getName()}
     * @throws NullPointerException if {@code enchantment} is null
     */
    @Nonnull
    public static String getEnchantmentName(@Nonnull final Enchantment enchantment) {
        Validate.checkNotNull(enchantment, "Enchantment must be set to use this method.");

        final String enhancementName;
        if (ItemCreator.getServerVersion() > 12.2F)
            enhancementName = enchantment.getKey().getKey();
        else
            enhancementName = enchantment.getName();

        return enhancementName;
    }

    /**
     * Resolve an Enchantment by name, using a NamespacedKey on modern servers and legacy names on older servers.
     *
     * <p>Resolution order:
     * <ol>
     *   <li>On Minecraft 1.13+ this tries {@link Enchantment#getByKey(NamespacedKey)} with
     *       {@link NamespacedKey#minecraft(String)} constructed from {@code name}.</li>
     *   <li>If unresolved (or on older servers) this tries {@link Enchantment#getByName(String)} using {@code name}.</li>
     *   <li>If still unresolved returns {@link Enchantment#VANISHING_CURSE} as a safe default.</li>
     * </ol>
     *
     * <p>Notes:
     * <ul>
     *   <li>The {@code enhancementName} parameter should be the NamespacedKey key for modern servers (e.g. "sharpness")
     *       or the legacy enchantment name/id for older servers. Use {@link #getEnchantment(NamespacedKey)} on modern
     *       Minecraft versions if you want access to enchantments outside the Minecraft-provided ones.</li>
     * </ul>
     *
     * @param enhancementName the enchantment name or key (non-null)
     * @return the resolved Enchantment, or {@link Enchantment#VANISHING_CURSE} if none found
     * @throws NullPointerException if {@code enhancementName} is null
     */
    @Nonnull
    public static Enchantment getEnchantment(@Nonnull final String enhancementName) {
        return getEnchantment(null, enhancementName);
    }

    /**
     * Resolve an Enchantment using a provided {@link NamespacedKey}.
     *
     * <p>Resolution order:
     * <ol>
     *   <li>Try {@link Enchantment#getByKey(NamespacedKey)} with {@code key}.</li>
     *   <li>If unresolved, returns {@link Enchantment#VANISHING_CURSE} as a safe default.</li>
     * </ol>
     *
     * @param key the NamespacedKey to try first (non-null)
     * @return the resolved Enchantment, or {@link Enchantment#VANISHING_CURSE} if none found
     * @throws NullPointerException if {@code key} is null
     */
    @Nonnull
    public static Enchantment getEnchantment(@Nonnull final NamespacedKey key) {
        Validate.checkNotNull(key, "key must not be null");
        return getEnchantment(key, null);
    }


    /**
     * Sets the server version based on the plugin's server version.
     *
     * @param plugin the plugin instance
     */
    private static void setServerVersion(Plugin plugin) {
        serverVersion = new ServerVersion(plugin);
    }

    /**
     * Resolve an Enchantment with an optional {@link NamespacedKey} and a name fallback.
     *
     * <p>Resolution order:
     * <ol>
     *   <li>If {@code key} is non-null, try {@link Enchantment#getByKey(NamespacedKey)} with {@code key}.</li>
     *   <li>If that fails and the server is 1.13+, try {@link Enchantment#getByKey(NamespacedKey)}
     *       with {@link NamespacedKey#minecraft(String)} constructed from {@code name}.</li>
     *   <li>If still unresolved (or on older servers) try {@link Enchantment#getByName(String)} with {@code name}.</li>
     *   <li>If unresolved return {@link Enchantment#VANISHING_CURSE} as a safe default.</li>
     * </ol>
     *
     * <p>Notes:
     * <ul>
     *   <li>The {@code name} parameter should be the NamespacedKey key for modern servers (e.g. "sharpness")
     *       or the legacy enchantment name/id for older servers.</li>
     * </ul>
     *
     * @param key  the NamespacedKey to try first (maybe null)
     * @param name the enchantment name or key (maybe null)
     * @return the resolved Enchantment, or {@link Enchantment#VANISHING_CURSE} if none found
     */
    @Nonnull
    private static Enchantment getEnchantment(@Nullable final NamespacedKey key, @Nullable final String name) {
        if (key != null) {
            Enchantment enchantment = Enchantment.getByKey(key);
            if (enchantment != null) return enchantment;
            else return Enchantment.VANISHING_CURSE;
        }
        if (name == null) return Enchantment.VANISHING_CURSE;

        if (ItemCreator.getServerVersion() > 12.2F) {
            Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(name));
            if (enchantment != null) return enchantment;
        }

        Enchantment enchantment = Enchantment.getByName(name);
        return enchantment == null ? Enchantment.VANISHING_CURSE : enchantment;
    }

}
