package org.broken.arrow.library.itemcreator;


import org.broken.arrow.library.color.TextTranslator;
import org.broken.arrow.library.itemcreator.utility.ConvertToItemStack;
import org.broken.arrow.library.itemcreator.utility.builders.ItemBuilder;
import org.broken.arrow.library.logging.Validate.ValidateExceptions;
import org.broken.arrow.library.nbt.RegisterNbtAPI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class ItemCreator {

    private static float serverVersion;
    private final NBTManger nbtManger;
    private ConvertToItemStack convertItems;
    private boolean haveTextTranslator = true;
    private boolean enableColorTranslation = true;

    private ItemCreator() {
        throw new ValidateExceptions("should not use empty constructor");
    }

    public ItemCreator(Plugin plugin) {
        this(plugin, false);
    }

    public ItemCreator(Plugin plugin, boolean turnOffLogger) {

        this.nbtManger = new NBTManger(plugin, turnOffLogger);
        setServerVersion(plugin);

        if (convertItems == null)
            convertItems = new ConvertToItemStack(serverVersion);

        try {
            TextTranslator.getInstance();
        } catch (NoClassDefFoundError ignore) {
            haveTextTranslator = false;
        }


    }

    /**
     * Starts the creation of a simple item.The item will not have a display name or lore.
     * Complete the creation by calling {@link CreateItemStack#makeItemStack()}.
     *
     * @param item The ItemStack you want to alter.
     * @return An instance of the CreateItemStack class.
     */
    public CreateItemStack of(ItemStack item) {
        return of(item, null);
    }

    /**
     * Starts the creation of a simple item.The item will not have a display name or lore.
     * Complete the creation by calling {@link CreateItemStack#makeItemStack()}.
     *
     * @param material The Material type.
     * @return An instance of the CreateItemStack class.
     */
    public CreateItemStack of(Material material) {
        return of(material, null);
    }

    /**
     * Starts the creation of a simple item.The item will not have a display name or lore.
     * Complete the creation by calling {@link CreateItemStack#makeItemStack()}.
     *
     * @param item The item name.
     * @return An instance of the CreateItemStack class.
     */
    public CreateItemStack of(String item) {
        return of(item, null);
    }

    /**
     * Starts the creation of an item with a display name and lore.
     * Complete the creation by calling {@link CreateItemStack#makeItemStack()}.
     *
     * @param item        The ItemStack you want to alter.
     * @param displayName The display name of the item.
     * @param lore        The lore of the item.
     * @return An instance of the CreateItemStack class.
     */
    public CreateItemStack of(ItemStack item, String displayName, String... lore) {
        return of(item, displayName, lore != null ? Arrays.asList(lore) : null);
    }

    /**
     * Starts the creation of an item with a display name and lore.
     * Complete the creation by calling {@link CreateItemStack#makeItemStack()}.
     *
     * @param material    The Material type.
     * @param displayName The display name of the item.
     * @param lore        The lore of the item.
     * @return An instance of the CreateItemStack class.
     */
    public CreateItemStack of(Material material, String displayName, String... lore) {
        return of(material, displayName, lore != null ? Arrays.asList(lore) : null);
    }

    /**
     * Starts the creation of an item with a display name and lore.
     * Complete the creation by calling {@link CreateItemStack#makeItemStack()}.
     *
     * @param itemName    The item name.
     * @param displayName The display name of the item.
     * @param lore        The lore of the item.
     * @return An instance of the CreateItemStack class.
     */
    public CreateItemStack of(String itemName, String displayName, String... lore) {
        return of(itemName, displayName, lore != null ? Arrays.asList(lore) : null);
    }

    /**
     * Starts the creation of an item with a display name and lore.
     * Complete the creation by calling {@link CreateItemStack#makeItemStack()}.
     *
     * @param item        The ItemStack you want to alter.
     * @param displayName The display name of the item.
     * @param lore        The lore of the item.
     * @return An instance of the CreateItemStack class.
     */
    public CreateItemStack of(ItemStack item, String displayName, List<String> lore) {
        return createStack(item, displayName, lore);
    }

    /**
     * Starts the creation of an item with a display name and lore.
     * Complete the creation by calling {@link CreateItemStack#makeItemStack()}.
     *
     * @param material    The material type of the stack.
     * @param displayName The display name of the item.
     * @param lore        The lore of the item.
     * @return An instance of the CreateItemStack class.
     */
    public CreateItemStack of(Material material, String displayName, List<String> lore) {
        return createStack(material, displayName, lore);
    }

    /**
     * Starts the creation of an item with a display name and lore.
     * Complete the creation by calling {@link CreateItemStack#makeItemStack()}.
     *
     * @param itemName    The item mame .
     * @param displayName The display name of the item.
     * @param lore        The lore of the item.
     * @return An instance of the CreateItemStack class.
     */
    public CreateItemStack of(String itemName, String displayName, List<String> lore) {
        return createStack(itemName, displayName, lore);
    }

    /**
     * Starts the creation of an item with a display name and lore.
     * Complete the creation by calling {@link CreateItemStack#makeItemStack()}.
     *
     * @param item        The name, Material, or ItemStack of the item.
     * @param displayName The display name of the item.
     * @param lore        The lore of the item.
     * @return An instance of the CreateItemStack class.
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
     * Starts the creation of an item using an existing or new ItemBuilder instance. You can set all the values you want
     * with the builder. Complete the creation by calling {@link CreateItemStack#makeItemStack()}.
     *
     * @param itemBuilder The ItemBuilder instance for creating the item.
     * @return An instance of the CreateItemStack class.
     */
    public CreateItemStack of(ItemBuilder itemBuilder) {
        return itemBuilder.build();
    }

    /**
     * Starts the creation of an item from an iterable of items. If you set name and/or lore
     * it will be shared over all items. Set it to null too keep the original text.
     * Complete the creation by calling {@link CreateItemStack#makeItemStackArray()}.
     *
     * @param itemArray   The iterable of items to convert to ItemStacks.
     * @param displayName The display name of the items.
     * @param lore        The lore of the items.
     * @param <T>         type if class on the item.
     * @return An instance of the CreateItemStack class.
     */
    public <T> CreateItemStack of(Iterable<T> itemArray, String displayName, List<String> lore) {
        ItemBuilder itemBuilder = new ItemBuilder(this, itemArray, displayName, lore);
        return itemBuilder.build();
    }

    /**
     * Checks whether color translation is enabled.
     *
     * @return {@code true} if color translation is enabled.
     */
    public boolean isEnableColorTranslation() {
        return enableColorTranslation;
    }

    /**
     * Sets whether color translation should be enabled. By default, color codes
     * in the string text will be translated automatically.
     * <p>
     * Set this to {@code false} to disable automatic color translation
     * and handle it manually instead.
     *
     * @param enableColorTranslation {@code true} to enable color translation.
     */
    public void setEnableColorTranslation(boolean enableColorTranslation) {
        this.enableColorTranslation = enableColorTranslation;
    }

    public static float getServerVersion() {
        return serverVersion;
    }

    private void setServerVersion(final Plugin plugin) {
        final String[] versionPieces = plugin.getServer().getBukkitVersion().split("\\.");
        final String firstNumber;
        String secondNumber;
        final String firstString = versionPieces[1];
        if (firstString.contains("-")) {
            firstNumber = firstString.substring(0, firstString.lastIndexOf("-"));

            secondNumber = firstString.substring(firstString.lastIndexOf("-") + 1);
            final int index = secondNumber.toUpperCase().indexOf("R");
            if (index >= 0)
                secondNumber = secondNumber.substring(index + 1);
        } else {
            final String secondString = versionPieces[2];
            firstNumber = firstString;
            secondNumber = secondString.substring(0, secondString.lastIndexOf("-"));
        }
        serverVersion = Float.parseFloat(firstNumber + "." + secondNumber);
    }

    @Nullable
    public RegisterNbtAPI getNbtApi() {
        return nbtManger.getNbtApi();
    }

    public ConvertToItemStack getConvertItems() {
        return convertItems;
    }

    public boolean isHaveTextTranslator() {
        return haveTextTranslator;
    }
}
