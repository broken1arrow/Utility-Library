package org.broken.arrow.library.itemcreator.serialization;

import com.google.gson.GsonBuilder;
import org.broken.arrow.library.itemcreator.meta.BottleEffectMeta;
import org.broken.arrow.library.itemcreator.serialization.typeadapter.BottleEffectMetaAdapter;
import org.broken.arrow.library.itemcreator.serialization.typeadapter.FireworkMetaAdapter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ItemStacksSerializer implements Iterable<ItemStack> {
    private final List<SerializeItem> items = new ArrayList<>();
    private transient final List<ItemStack> itemStacks = new ArrayList<>();

    public void add(@Nonnull final ItemStack itemStack) {
        this.items.add(SerializeItem.fromItemStack(itemStack));
        this.itemStacks.add(this.createNewItemstack(itemStack));
    }

    public void addAll(@Nonnull final ItemStack... itemStacks) {
        if (itemStacks == null || itemStacks.length == 0)
            return;
        for (ItemStack itemStack : itemStacks) {
            this.items.add(SerializeItem.fromItemStack(itemStack));
            this.itemStacks.add(this.createNewItemstack(itemStack));
        }
    }

    public void remove(@Nonnull final ItemStack itemStack) {
        this.items.remove(SerializeItem.fromItemStack(itemStack));
        this.itemStacks.remove(itemStack);
    }

    public void clear() {
        this.items.clear();
        this.itemStacks.clear();
    }

    public List<SerializeItem> getItems() {
        return new ArrayList<>(items);
    }

    public List<ItemStack> getItemStacks() {
        return itemStacks;
    }

    @Override
    @Nonnull
    public Iterator<ItemStack> iterator() {
        return itemStacks.iterator();
    }

    public String toJson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(FireworkMeta.class, new FireworkMetaAdapter())
                .registerTypeAdapter(BottleEffectMeta.class, new BottleEffectMetaAdapter())
                .create()
                .toJson(this);
    }

    public static ItemStacksSerializer fromJson(String json) {
        final ItemStacksSerializer serializer = new GsonBuilder()
                .registerTypeAdapter(FireworkMeta.class, new FireworkMetaAdapter())
                .registerTypeAdapter(BottleEffectMeta.class, new BottleEffectMetaAdapter())
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
