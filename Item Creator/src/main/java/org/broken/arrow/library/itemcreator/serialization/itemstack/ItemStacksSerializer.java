package org.broken.arrow.library.itemcreator.serialization.itemstack;

import com.google.gson.GsonBuilder;
import org.broken.arrow.library.itemcreator.meta.BottleEffectMeta;
import org.broken.arrow.library.itemcreator.meta.MapWrapperMeta;
import org.broken.arrow.library.itemcreator.serialization.typeadapter.BottleEffectMetaAdapter;
import org.broken.arrow.library.itemcreator.serialization.typeadapter.FireworkMetaAdapter;
import org.broken.arrow.library.itemcreator.serialization.typeadapter.MapMetaAdapter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for serializing and deserializing collections of {@link ItemStack}
 * objects to and from JSON format.
 * <p>
 * Maintains both the original {@link ItemStack} list and their serialized
 * representations ({@link SerializeItem}) for easy conversion.
 * </p>
 * <p>
 * Use {@link #toJson()} to export as JSON, {@link #fromJson(String)} to load from JSON,
 * or {@link #getItems()} to obtain the serializable objects directly.
 * </p>
 */
public class ItemStacksSerializer implements Iterable<ItemStack> {
    private final List<SerializeItem> items = new ArrayList<>();
    private final transient List<ItemStack> itemStacks = new ArrayList<>();

    /**
     * Adds a single ItemStack to the serializer.
     *
     * @param itemStack the ItemStack to add (not null)
     */
    public void add(@Nonnull final ItemStack itemStack) {
        final ItemStack stack = new ItemStack(itemStack);
        this.items.add(SerializeItem.fromItemStack(stack));
        this.itemStacks.add(this.createNewItemstack(stack));
    }

    /**
     * Adds multiple ItemStacks to the serializer.
     *
     * @param itemStacks the ItemStacks to add (ignored if null or empty)
     */
    public void addAll(@Nonnull final ItemStack... itemStacks) {
        if (itemStacks == null || itemStacks.length == 0)
            return;
        for (ItemStack itemStack : itemStacks) {
            final ItemStack stack = new ItemStack(itemStack);
            this.items.add(SerializeItem.fromItemStack(stack));
            this.itemStacks.add(this.createNewItemstack(stack));
        }
    }

    /**
     * Removes the given ItemStack from the serializer.
     *
     * @param itemStack the ItemStack to remove (not null)
     */
    public void remove(@Nonnull final ItemStack itemStack) {
        this.items.remove(SerializeItem.fromItemStack(itemStack));
        this.itemStacks.remove(itemStack);
    }

    /**
     * Removes all stored items from the serializer.
     */
    public void clear() {
        this.items.clear();
        this.itemStacks.clear();
    }

    /**
     * Returns a copy of the serialized item data list.
     *
     * @return a new List containing {@link SerializeItem} objects.
     */
    public List<SerializeItem> getItems() {
        return new ArrayList<>(items);
    }

    /**
     * Returns the list of stored ItemStacks.
     * <p>
     * The returned list is the internal list, so changes will affect this object.
     *
     * @return the ItemStack list.
     */
    public List<ItemStack> getItemStacks() {
        return itemStacks;
    }

    /**
     * Returns an iterator over the stored ItemStacks.
     *
     * @return an iterator for the ItemStacks
     */
    @Override
    @Nonnull
    public Iterator<ItemStack> iterator() {
        return itemStacks.iterator();
    }

    /**
     * Serializes the stored items to a pretty-printed JSON string.
     *
     * @return JSON representation of this serializer
     */
    public String toJson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(FireworkMeta.class, new FireworkMetaAdapter())
                .registerTypeAdapter(BottleEffectMeta.class, new BottleEffectMetaAdapter())
                .registerTypeAdapter(MapWrapperMeta.class,new MapMetaAdapter())
                .create()
                .toJson(this);
    }

    /**
     * Creates an ItemStacksSerializer from a JSON string.
     *
     * @param json the JSON data
     * @return the deserialized ItemStacksSerializer
     */
    public static ItemStacksSerializer fromJson(String json) {
        final ItemStacksSerializer serializer = new GsonBuilder()
                .registerTypeAdapter(FireworkMeta.class, new FireworkMetaAdapter())
                .registerTypeAdapter(BottleEffectMeta.class, new BottleEffectMetaAdapter())
                .registerTypeAdapter(MapWrapperMeta.class,new MapMetaAdapter())
                .create()
                .fromJson(json, ItemStacksSerializer.class);
        serializer.itemStacks.addAll(serializer.items.stream()
                .map(SerializeItem::toItemStack).collect(Collectors.toList()));
        return serializer;
    }

    private ItemStack createNewItemstack(final ItemStack itemStack) {
        return new ItemStack(itemStack);
    }
}
