package org.broken.arrow.library.itemcreator;


import org.broken.arrow.library.color.TextTranslator;
import org.broken.arrow.library.itemcreator.utility.ConvertToItemStack;
import org.broken.arrow.library.itemcreator.utility.ServerVersion;
import org.broken.arrow.library.itemcreator.utility.builders.ItemBuilder;
import org.broken.arrow.library.logging.Validate.ValidateExceptions;
import org.broken.arrow.library.nbt.RegisterNbtAPI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

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
     * @param plugin       the plugin instance
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
     * Returns the current server version as a float.
     *
     * @return the server version
     */
    public static float getServerVersion() {
        return serverVersion.getServerVersion();
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
     * Sets the server version based on the plugin's server version.
     *
     * @param plugin the plugin instance
     */
    private static void setServerVersion(Plugin plugin) {
        serverVersion = new ServerVersion(plugin);
    }

}
