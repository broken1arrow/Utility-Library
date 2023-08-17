package org.broken.arrow.title.update.library.nms;

import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Interface for initializing and retrieving classes and elements related to Minecraft's NMS and Bukkit server.
 */
public interface NMSInitializer {

	/**
	 * Retrieve the packet class.
	 *
	 * @return The packet class.
	 * @throws ClassNotFoundException If the packet class cannot be found.
	 */
	default Class<?> getPacket() throws ClassNotFoundException {
		return Class.forName(retrieveNMSPackage("Packet"));
	}

	/**
	 * Retrieve the method for obtaining the EntityPlayer instance from a CraftPlayer instance.
	 *
	 * @return The method for obtaining EntityPlayer.
	 * @throws ClassNotFoundException If the required class cannot be found.
	 * @throws NoSuchMethodException  If the required method cannot be found.
	 */
	default Method getPlayerHandle() throws ClassNotFoundException, NoSuchMethodException {
		return Class.forName(retrieveBukkitPackage("entity.CraftPlayer")).getMethod("getHandle");
	}

	/**
	 * Retrieve the field representing the PlayerConnection instance from an EntityPlayer.
	 *
	 * @return The field representing PlayerConnection.
	 * @throws ClassNotFoundException If the required class cannot be found.
	 * @throws NoSuchFieldException   If the required field cannot be found.
	 */
	default Field getPlayerConnection() throws ClassNotFoundException, NoSuchFieldException {
		return Class.forName(retrieveNMSPackage("EntityPlayer")).getField("playerConnection");
	}

	/**
	 * Retrieve the PlayerConnection class.
	 *
	 * @return the method that returns PlayerConnection instance.
	 */
	default Class<?> getPlayerConnectionClass() throws ClassNotFoundException {
		return Class.forName(retrieveNMSPackage("PlayerConnection"));
	}

	/**
	 * Retrieve the Containers class.
	 *
	 * @return The method that returns Containers instance.
	 * @throws ClassNotFoundException If the Containers class cannot be found.
	 */
	default Class<?> getContainersClass() throws ClassNotFoundException {
		return Class.forName(retrieveNMSPackage("Containers"));
	}

	/**
	 * Retrieve the Container class.
	 *
	 * @return The method that returns Container instance.
	 * @throws ClassNotFoundException If the Container class cannot be found.
	 */
	default Class<?> getContainerClass() throws ClassNotFoundException {
		return Class.forName(retrieveNMSPackage("Container"));
	}

	/**
	 * Retrieve the class representing the IChatBaseComponent.ChatSerializer class,
	 * which allows you to serialize chat messages.
	 *
	 * @return The class representing IChatBaseComponent.ChatSerializer.
	 * @throws ClassNotFoundException If the ChatSerializer class cannot be found.
	 */
	default Class<?> getChatSerializer() throws ClassNotFoundException {
		return Class.forName(retrieveNMSPackage("IChatBaseComponent$ChatSerializer"));
	}

	/**
	 * Retrieve the constructor for creating PacketPlayOutOpenWindow instances.
	 *
	 * @return The constructor for PacketPlayOutOpenWindow.
	 * @throws ClassNotFoundException If the required class cannot be found.
	 * @throws NoSuchMethodException  If the required constructor cannot be found.
	 */
	default Constructor<?> getPacketPlayOutOpenWindow() throws ClassNotFoundException, NoSuchMethodException {
		return Class.forName(retrieveNMSPackage("PacketPlayOutOpenWindow")).getConstructor(int.class, this.getContainerClass(), this.getChatSerializer());
	}

	/**
	 * Utility method for retrieving the fully qualified NMS package for the given class.
	 * <p>NOTE:</p>
	 * <p>
	 * This method are only useful for Minecraft 1.16.5 and below.
	 * </p>
	 *
	 * @param clazzName The name of the class for which the NMS package is retrieved.
	 * @return The fully qualified NMS package name.
	 */
	default String retrieveNMSPackage(final String clazzName) {
		return "net.minecraft.server." + Bukkit.getServer().getClass().toGenericString().split("\\.")[3] + "." + clazzName;
	}

	/**
	 * Utility method for retrieving the fully qualified Bukkit package for the given class.
	 *
	 * @param clazzName The name of the class for which the Bukkit package is retrieved.
	 * @return The fully qualified Bukkit package name.
	 */
	default String retrieveBukkitPackage(final String clazzName) {
		return "org.bukkit.craftbukkit." + Bukkit.getServer().getClass().toGenericString().split("\\.")[3] + "." + clazzName;
	}

}
