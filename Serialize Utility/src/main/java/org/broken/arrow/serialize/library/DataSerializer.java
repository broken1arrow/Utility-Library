package org.broken.arrow.serialize.library;

import com.google.gson.Gson;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.broken.arrow.serialize.library.utility.converters.LocationSerializer;
import org.broken.arrow.serialize.library.utility.serialize.ConfigurationSerializable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class provides methods to serialize various data types into a format suitable for storage or database usage.
 * It supports serialization of objects such as colors, locations, UUIDs, enums, command senders, potions, components,
 * hover events, click events, iterable collections, maps, integers, doubles, floats, longs, shorts, strings,
 * booleans, items, memory sections, patterns, and objects implementing the ConfigurationSerializable interface.
 * The class also handles serialization of Bukkit-specific types and custom serializable objects.
 * Note: If an object does not have a known serialization method, a SerializeFailedException will be thrown.
 * @deprecated will be replaced with {@link SerializeUtility}
 */
@Deprecated
public final class DataSerializer {
	private static final float SERVER_VERSION;

	static {
		final String[] versionPieces = Bukkit.getServer().getBukkitVersion().split("\\.");
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
		SERVER_VERSION = Float.parseFloat(firstNumber + "." + secondNumber);
	}
	
	/**
	 * Serializes an object into a format suitable for storage or database usage.
	 *
	 * @param obj The object to serialize.
	 * @return The serialized representation of the object.
	 * @throws SerializeFailedException If the serialization fails due to an unsupported data type or serialization error.
	 */
	public static Object serialize(final Object obj) {
		if (obj == null) {
			return null;
		}

		if (obj instanceof ChatColor) {
			return serializeChatColor((ChatColor) obj);
		} else if (obj instanceof net.md_5.bungee.api.ChatColor) {
			return serializeBungeeChatColor((net.md_5.bungee.api.ChatColor) obj);
		} else if (obj instanceof Location) {
			return LocationSerializer.serializeLocYaw((Location) obj);
		} else if (obj instanceof UUID || obj instanceof Enum<?>) {
			return obj.toString();
		} else if (obj instanceof CommandSender) {
			return ((CommandSender) obj).getName();
		} else if (obj instanceof World) {
			return ((World) obj).getName();
		} else if (obj instanceof PotionEffect) {
			return ((PotionEffect) obj).serialize();
		} else if (obj instanceof java.awt.Color) {
			return "#" + ((java.awt.Color) obj).getRGB();
		} else if (obj instanceof BaseComponent) {
			return toJson((BaseComponent) obj);
		} else if (obj instanceof BaseComponent[]) {
			return toJson((BaseComponent[]) obj);
		} else if (obj instanceof HoverEvent) {
			return serializeHoverEvent((HoverEvent) obj);
		} else if (obj instanceof ClickEvent) {
			return serializeClickEvent((ClickEvent) obj);
		} else if (obj instanceof Iterable || obj.getClass().isArray()) {
			return serializeIterableOrArray(obj);
		} else if (obj instanceof Map) {
			return serializeMap(obj);
		} else if (obj instanceof Integer || obj instanceof Double || obj instanceof Float ||
				obj instanceof Long || obj instanceof Short || obj instanceof String ||
				obj instanceof Boolean || obj instanceof ItemStack || obj instanceof MemorySection ||
				obj instanceof Pattern) {
			return obj;
		} else if (obj instanceof org.bukkit.configuration.serialization.ConfigurationSerializable) {
			return ((org.bukkit.configuration.serialization.ConfigurationSerializable) obj).serialize();
		} else if (obj instanceof ConfigurationSerializable) {
			return ((ConfigurationSerializable) obj).serialize();
		}
		throw new SerializeFailedException("Does not know how to serialize " +
				obj.getClass().getSimpleName() + "! Does it extend ConfigSerializable? Data: " + obj);
	}

	private static Object serializeChatColor(ChatColor chatColor) {
		return chatColor.name();
	}

	private static Object serializeBungeeChatColor(net.md_5.bungee.api.ChatColor chatColor) {
		return SERVER_VERSION >= 16.0 ? chatColor.toString() : chatColor.name();
	}

	private static Object serializeHoverEvent(HoverEvent event) {
		Map<String, Object> serialize = new HashMap<>();
		serialize.put("Action", event.getAction().name());
		serialize.put("Value", Arrays.stream(event.getValue().clone()).map(BaseComponent::toString).collect(Collectors.toList()));
		return serialize;
	}

	private static Object serializeClickEvent(ClickEvent event) {
		Map<String, Object> serialize = new HashMap<>();
		serialize.put("Action", event.getAction().name());
		serialize.put("Value", event.getValue());
		return serialize;
	}

	private static Object serializeIterableOrArray(Object obj) {
		List<Object> serialized = new ArrayList<>();
		if (obj instanceof Iterable) {
			for (Object element : (Iterable<?>) obj) {
				serialized.add(serialize(element));
			}
		} else {
			for (Object element : (Object[]) obj) {
				serialized.add(serialize(element));
			}
		}
		return serialized;
	}

	private static Object serializeMap(Object obj) {
		Map<?, ?> oldMap = (Map<?, ?>) obj;
		Map<Object, Object> newMap = new LinkedHashMap<>();

		for (Map.Entry<?, ?> entry : oldMap.entrySet()) {
			newMap.put(serialize(entry.getKey()), serialize(entry.getValue()));
		}
		return newMap;
	}

	private static String toJson(final BaseComponent... comps) {
		if (SERVER_VERSION < 8.8)
			return "{}";
		String json;
		try {
			json = ComponentSerializer.toString(comps);
		} catch (final Throwable var3) {
			json = (new Gson()).toJson((new TextComponent(comps)).toLegacyText());
		}

		return json;
	}

	public static class SerializeFailedException extends RuntimeException {
		private static final long serialVersionUID = 2L;

		public SerializeFailedException(final String reason) {
			super(reason);
		}

	}
}
