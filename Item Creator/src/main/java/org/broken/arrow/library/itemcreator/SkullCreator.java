package org.broken.arrow.library.itemcreator;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.logging.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

/**
 * A library for the Bukkit API to create player skulls
 * from names, base64 strings, and texture URLs.
 * <p>
 * Does not use any NMS code, and should work across all versions.
 *
 * @author Dean B on 12/28/2016.
 */
public class SkullCreator {

    private static final Logging LOG = new Logging(SkullCreator.class);
    private static final String PLAYER_HEAD = "PLAYER_HEAD";
    private static final String BLOCK = "block";
    private static final String NAME = "name";
    public static final String PROFILE = "profile";
    public static final String TEXTURES = "textures";
    private static boolean legacy;
    private static boolean warningPosted = false;
    private static boolean doesHaveOwnerProfile = true;
    // some reflection stuff to be used when setting a skull's profile
    private static Field blockProfileField;
    private static Method metaSetProfileMethod;
    private static Field metaProfileField;

    // Check if it legacy version, means before 1.13.
    static {
        try {
            Class<?> skullMeta = Class.forName("org.bukkit.inventory.meta.SkullMeta");
            skullMeta.getMethod("setOwningPlayer", OfflinePlayer.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            legacy = true;
        }

    }

    /**
     * Private constructor to prevent instantiation.
     */
    private SkullCreator() {
    }

    /**
     * Creates a player skull, should work in both legacy and new Bukkit APIs.
     *
     * @return itemstack.
     */
    public static ItemStack createSkull() {
        try {
            return new ItemStack(Material.valueOf(PLAYER_HEAD));
        } catch (IllegalArgumentException e) {
            return new ItemStack(getMaterial("SKULL_ITEM"), 1, (byte) 3);
        }
    }

    /**
     * Creates a player skull item with the skin based on a player's name.
     *
     * @param name The Player's name.
     * @return The head of the Player.
     * @deprecated names don't make for good identifiers.
     */
    @Deprecated
    public static ItemStack itemFromName(String name) {
        return itemWithName(createSkull(), name);
    }

    /**
     * Creates a player skull item with the skin based on a player's UUID.
     *
     * @param id The Player's UUID.
     * @return The head of the Player.
     */
    public static ItemStack itemFromUuid(UUID id) {
        return itemWithUuid(createSkull(), id);
    }

    /**
     * Creates a player skull item with the skin at a Mojang URL.
     *
     * @param url The Mojang URL.
     * @return The head of the Player.
     */
    public static ItemStack itemFromUrl(String url) {
        return itemWithUrl(createSkull(), url);
    }

    /**
     * Creates a player skull item with the skin based on a base64 string.
     *
     * @param base64 The Mojang URL.
     * @return The head of the Player.
     */
    public static ItemStack itemFromBase64(String base64) {
        return itemWithBase64(createSkull(), base64);
    }

    /**
     * Modifies a skull to use the skin of the player with a given name.
     *
     * @param item The item to apply the name to. Must be a player skull.
     * @param name The Player's name.
     * @return The head of the Player.
     * @deprecated names don't make for good identifiers.
     */
    @Deprecated
    public static ItemStack itemWithName(ItemStack item, String name) {
        notNull(item, "item");
        notNull(name, NAME);

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwner(name);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Modifies a skull to use the skin of the player with a given UUID.
     *
     * @param item The item to apply the name to. Must be a player skull.
     * @param id   The Player's UUID.
     * @return The head of the Player.
     */
    public static ItemStack itemWithUuid(ItemStack item, UUID id) {
        notNull(item, "item");
        notNull(id, "id");

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        setOwningPlayer(meta, Bukkit.getOfflinePlayer(id));
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Modifies a skull to use the skin at the given Mojang URL.
     *
     * @param item The item to apply the skin to. Must be a player skull.
     * @param url  The URL of the Mojang skin.
     * @return The head associated with the URL.
     */
    public static ItemStack itemWithUrl(ItemStack item, String url) {
        notNull(item, "item");
        notNull(url, "url");

        return itemWithBase64(item, urlToBase64(url));
    }

    /**
     * Modifies a skull to use the skin based on the given base64 string.
     *
     * @param item   The ItemStack to put the base64 onto. Must be a player skull.
     * @param base64 The base64 string containing the texture.
     * @return The head with a custom texture.
     */
    public static ItemStack itemWithBase64(ItemStack item, String base64) {
        notNull(item, "item");
        notNull(base64, "base64");

        if (!(item.getItemMeta() instanceof SkullMeta)) {
            return null;
        }
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        mutateItemMeta(meta, base64);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Sets the block to a skull with the given name.
     *
     * @param block The block to set.
     * @param name  The player to set it to.
     * @deprecated names don't make for good identifiers.
     */
    @Deprecated
    public static void blockWithName(Block block, String name) {
        notNull(block, BLOCK);
        notNull(name, NAME);

        Skull state = (Skull) block.getState();
        state.setOwningPlayer(Bukkit.getOfflinePlayer(name));
        state.update(false, false);
    }

    /**
     * Sets the block to a skull with the given UUID.
     *
     * @param block The block to set.
     * @param id    The player to set it to.
     */
    public static void blockWithUuid(Block block, UUID id) {
        notNull(block, BLOCK);
        notNull(id, "id");

        setToSkull(block);
        Skull state = (Skull) block.getState();
        state.setOwningPlayer(Bukkit.getOfflinePlayer(id));
        state.update(false, false);
    }

    /**
     * Sets the block to a skull with the skin found at the provided mojang URL.
     *
     * @param block The block to set.
     * @param url   The mojang URL to set it to use.
     */
    public static void blockWithUrl(Block block, String url) {
        notNull(block, BLOCK);
        notNull(url, "url");

        blockWithBase64(block, urlToBase64(url));
    }

    /**
     * Sets the block to a skull with the skin for the base64 string.
     *
     * @param block  The block to set.
     * @param base64 The base64 to set it to use.
     */
    public static void blockWithBase64(Block block, String base64) {
        notNull(block, BLOCK);
        notNull(base64, "base64");

        setToSkull(block);
        Skull state = (Skull) block.getState();
        mutateBlockState(state, base64);
        state.update(false, false);
    }


    /**
     * Sets the skin URL on the given {@link SkullMeta}.
     *
     * <p>This method applies a skin to a skull item, supporting both modern
     * and legacy server implementations:</p>
     * <ul>
     *     <li>Modern API (PlayerProfile-based): uses {@link PlayerProfile} and {@code setOwnerProfile}.</li>
     *     <li>Legacy API (GameProfile reflection): creates a {@link GameProfile} and sets the
     *         Base64-encoded texture via the internal {@code profile} field.</li>
     * </ul>
     *
     * <p>If {@code url} is {@code null}, only the {@code uuid} will be applied,
     * resulting in a skull that displays the player's default skin.</p>
     *
     * @param meta the {@link SkullMeta} to modify
     * @param uuid the UUID to associate with the skull's profile
     * @param url  the skin URL to apply, or {@code null} to leave only the default player skin
     */
    public static void setSkullUrl(final SkullMeta meta, UUID uuid, @Nullable final String url) {
        checkIfHasOwnerMethod(meta);
        if (doesHaveOwnerProfile) {
            PlayerProfile profile = Bukkit.createPlayerProfile(uuid);
            if (url != null) {
                try {
                    profile.getTextures().setSkin(new URL(url));
                } catch (MalformedURLException e) {
                    LOG.log(() -> "Can't set back the url '" + url + "' for this skull.");
                }
            }
            meta.setOwnerProfile(profile);
            return;
        }
        GameProfile profile = new GameProfile(uuid, null);
        setSkin(url, profile);
        try {
            if (metaProfileField == null) {
                metaProfileField = meta.getClass().getDeclaredField(PROFILE);
                metaProfileField.setAccessible(true);
            }
            metaProfileField.set(meta, profile);
        } catch (NoSuchFieldException e) {
            LOG.log(e, () -> "Failed to access legacy SkullMeta profile field.");
        } catch (IllegalAccessException e) {
            LOG.log(e, () -> "Failed to set the legacy SkullMeta profile to field.");
        }
    }

    /**
     * Retrieves the skin URL from the given {@link SkullMeta}.
     * <p>
     * This method attempts to extract the URL of the custom head texture
     * regardless of whether the server is running a modern API
     * (using {@link PlayerProfile}) or an older legacy API
     * (via the internal {@code GameProfile}).
     * </p>
     *
     * @param meta the {@link SkullMeta} to extract the skin URL from
     * @return the skin URL in string form, or {@code null} if no URL is set
     *         or if the required methods/fields could not be accessed.
     */
    @Nullable
    public static String getSkullUrl(SkullMeta meta) {
        checkIfHasOwnerMethod(meta);
        if (doesHaveOwnerProfile) {
            try {
                final PlayerProfile ownerProfile = meta.getOwnerProfile();
                if (ownerProfile != null) {
                    final URL skin = ownerProfile.getTextures().getSkin();
                    if (skin != null)
                        return skin.toExternalForm();
                }
            } catch (NoClassDefFoundError | NoSuchMethodError ex2) {
                LOG.log(() -> "Could not invoke PlayerProfile and the methods (getTextures, getOwnerProfile, getSkin).");
            }
            return null;
        }
        try {
            if (metaProfileField == null) {
                metaProfileField = meta.getClass().getDeclaredField(PROFILE);
                metaProfileField.setAccessible(true);
            }
            GameProfile profile = (GameProfile) metaProfileField.get(meta);
            if (profile != null) {
                String url = getUrl(profile);
                if (url != null) return url;
            }
        } catch (NoSuchFieldException | IllegalAccessException ex2) {
            LOG.log(ex2, () -> "Failed to access legacy SkullMeta profile field.");
        }
        return null;
    }

    /**
     * Sets the block to a skull of type PLAYER_HEAD or legacy SKULL with player skull type.
     *
     * @param block The block to convert to a skull.
     */
    private static void setToSkull(Block block) {
        checkLegacy();

        try {
            block.setType(Material.valueOf(PLAYER_HEAD), false);
        } catch (IllegalArgumentException e) {
            block.setType(Material.valueOf("SKULL"), false);
            Skull state = (Skull) block.getState();
            state.setSkullType(SkullType.PLAYER);
            state.update(false, false);
        }
    }

    /**
     * Checks if an object is null and throws NullPointerException if so.
     *
     * @param o    The object to check.
     * @param name The name of the object to use in the exception message.
     * @throws NullPointerException if {@code o} is null.
     */
    private static void notNull(Object o, String name) {
        if (o == null) {
            throw new NullPointerException(name + " should not be null!");
        }
    }

    /**
     * Converts a Mojang skin URL to a base64 encoded string for skull textures.
     *
     * @param url The Mojang skin URL.
     * @return The base64 encoded texture string.
     * @throws Validate.ValidateExceptions if the URL syntax is invalid.
     */
    private static String urlToBase64(String url) {

        URI actualUrl;
        try {
            actualUrl = new URI(url);
        } catch (URISyntaxException e) {
            throw new Validate.ValidateExceptions(e, "Could not create the Base64 from the url");
        }
        String toEncode = "{\"textures\":{\"SKIN\":{\"url\":\"" + actualUrl + "\"}}}";
        return Base64.getEncoder().encodeToString(toEncode.getBytes());
    }

    /**
     * Creates a GameProfile with a random UUID based on the base64 string and sets
     * the textures property with the given base64 texture.
     *
     * @param b64 The base64 encoded texture string.
     * @return A GameProfile with the texture property.
     */
    private static GameProfile makeProfile(String b64) {
        // random uuid based on the b64 string
        UUID id = new UUID(
                b64.substring(b64.length() - 20).hashCode(),
                b64.substring(b64.length() - 10).hashCode()
        );
        GameProfile profile = new GameProfile(id, "aaaaa");
        profile.getProperties().put(TEXTURES, new Property(TEXTURES, b64));
        return profile;
    }

    /**
     * Mutates the block's Skull state to use the given base64 encoded skin texture.
     *
     * @param block The Skull block state to mutate.
     * @param b64   The base64 texture string.
     */
    private static void mutateBlockState(Skull block, String b64) {
        try {
            if (blockProfileField == null) {
                blockProfileField = block.getClass().getDeclaredField(PROFILE);
                blockProfileField.setAccessible(true);
            }
            blockProfileField.set(block, makeProfile(b64));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOG.log(e, () -> "Failed to change the skull block");
        }
    }

    /**
     * Mutates the SkullMeta of an item to use the given base64 encoded skin texture.
     *
     * @param meta The SkullMeta to mutate.
     * @param b64  The base64 texture string.
     */
    private static void mutateItemMeta(SkullMeta meta, String b64) {
        checkIfHasOwnerMethod(meta);
        if (doesHaveOwnerProfile) {
            try {
                meta.setOwnerProfile(makePlayerProfile(b64));
            } catch (MalformedURLException ex2) {
                LOG.log(ex2, () -> "Can't invoke the profile from the SkullMeta.");
            }
            return;
        }
        try {
            if (metaSetProfileMethod == null) {
                metaSetProfileMethod = meta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
                metaSetProfileMethod.setAccessible(true);
            }
            metaSetProfileMethod.invoke(meta, makeProfile(b64));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            // if in an older API where there is no setProfile method,
            // we set the profile field directly.
            try {
                if (metaProfileField == null) {
                    metaProfileField = meta.getClass().getDeclaredField(PROFILE);
                    metaProfileField.setAccessible(true);
                }
                metaProfileField.set(meta, makeProfile(b64));

            } catch (NoSuchFieldException | IllegalAccessException ex2) {
                LOG.log(ex2, () -> "Fail to get the profile");
            }
        }
    }

    private static String getUrl(final GameProfile profile) {
        for (Property property : profile.getProperties().get(TEXTURES)) {
            String value = property.getValue();
            try {
            // Decode Base64 -> JSON
            String json = new String(Base64.getDecoder().decode(value));
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

            return obj
                    .getAsJsonObject(TEXTURES)
                    .getAsJsonObject("SKIN")
                    .get("url")
                    .getAsString();
            } catch (IllegalArgumentException e) {
                LOG.log(e, () -> "Failed to decode skull texture property for profile: " + profile.getId());
            }
        }
        return null;
    }

    private static void setSkin(final String url, final GameProfile profile) {
        if (url == null) return;

        JsonObject skinJson = new JsonObject();
        JsonObject textures = new JsonObject();
        JsonObject skin = new JsonObject();
        skin.addProperty("url", url);
        textures.add("SKIN", skin);
        skinJson.add(TEXTURES, textures);
        String encoded = Base64.getEncoder().encodeToString(skinJson.toString().getBytes(StandardCharsets.UTF_8));
        profile.getProperties().put(TEXTURES, new Property(TEXTURES, encoded));

    }

    /**
     * Checks whether the SkullMeta class has the setOwnerProfile method
     * and updates the flag accordingly.
     *
     * @param meta The SkullMeta instance to check.
     */
    private static void checkIfHasOwnerMethod(SkullMeta meta) {
        if (metaSetProfileMethod == null) {
            try {
                metaSetProfileMethod = meta.getClass().getDeclaredMethod("setOwnerProfile", PlayerProfile.class);
            } catch (NoSuchMethodException | NoClassDefFoundError exception) {
                doesHaveOwnerProfile = false;
            }
        }
    }

    /**
     * Checks if running on legacy Bukkit API and logs a warning if using legacy API on modern server.
     */
    private static void checkLegacy() {
        try {
            // if both of these succeed, then we are running
            // in a legacy api, but on a modern (1.13+) server.
            Material.class.getDeclaredField(PLAYER_HEAD);
            Material.valueOf("SKULL");

            if (!warningPosted) {
                LOG.log(() -> "SKULLCREATOR API - Using the legacy bukkit API with 1.13+ bukkit versions is not supported!");
                warningPosted = true;
            }
        } catch (NoSuchFieldException | IllegalArgumentException ignored) {
            //We don't need to know a error is thrown. This only checks so you don't use wrong API version.
        }
    }

    /**
     * Retrieves a Material by name, logging a warning if not found.
     *
     * @param name The name of the Material.
     * @return The Material if found; null otherwise.
     */
    private static Material getMaterial(String name) {
        try {
            return Material.valueOf(name);
        } catch (Exception e) {
            LOG.log(() -> "Could not find this material: " + name);
        }
        return null;
    }

    /**
     * Sets the owning player of the SkullMeta using the appropriate method depending on Bukkit API version.
     *
     * @param meta   The SkullMeta to modify.
     * @param player The OfflinePlayer to set as owner.
     */
    private static void setOwningPlayer(SkullMeta meta, OfflinePlayer player) {
        try {
            if (legacy) {
                meta.setOwner(player.getName());
            } else {
                meta.setOwningPlayer(player);
            }
        } catch (Exception ignored) {
            //We ignore the thrown error.
        }
    }

    /**
     * Creates a PlayerProfile with textures set from the given base64 string.
     *
     * @param b64 The base64 encoded texture string.
     * @return The PlayerProfile with the skin set.
     * @throws MalformedURLException if the texture URL is malformed.
     */
    private static PlayerProfile makePlayerProfile(String b64) throws MalformedURLException {
        UUID id = new UUID(
                b64.substring(b64.length() - 20).hashCode(),
                b64.substring(b64.length() - 10).hashCode()
        );

        PlayerProfile profile = Bukkit.createPlayerProfile(id);
        PlayerTextures textures = profile.getTextures();
        URL urlFromBase64 = getUrlFromBase64(b64);
        if (urlFromBase64 == null)
            return profile;
        textures.setSkin(urlFromBase64);
        profile.setTextures(textures);
        return profile;
    }

    /**
     * Parses a URL from the given base64 encoded texture string.
     *
     * @param base64 The base64 encoded texture string.
     * @return The URL of the skin texture, or null if parsing failed.
     */
    private static URL getUrlFromBase64(String base64) throws MalformedURLException {
        try {
            String decoded = new String(Base64.getDecoder().decode(base64));
            return new URL(decoded.substring("{\"textures\":{\"SKIN\":{\"url\":\"".length(), decoded.length() - "\"}}}".length()));
        } catch (IllegalArgumentException exception) {
            LOG.log(() -> "Failed to parse the Base64 string, does you provide a valid base64 string? The input string: " + base64);
        }
        return null;
    }
}