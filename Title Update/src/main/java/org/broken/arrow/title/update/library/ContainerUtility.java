package org.broken.arrow.title.update.library;

import org.broken.arrow.color.library.TextTranslator;
import org.broken.arrow.title.update.library.utility.TitleLogger;
import org.broken.arrow.title.update.library.utility.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

public class ContainerUtility {
	private Class<?> packetclass;
	private Method handle;
	private Field playerConnection;
	private Class<?> packetConnectionClass;
	private Class<?> chatCompenentSubClass;
	private Class<?> containersClass;
	private Class<?> containerClass;
	private Constructor<?> packetConstructor;
	private final float serverVersion;
	private final NmsData newNmsData;
	private final TitleLogger titleLogger;

	protected ContainerUtility(final NmsData newNmsData, final float serverVersion) {
		titleLogger = new TitleLogger(ContainerUtility.class);
		loadClasses(serverVersion);
		this.serverVersion = serverVersion;
		this.newNmsData = newNmsData;
	}

	protected void updateInventory(@Nonnull final Player player1, final String title) throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException, InstantiationException {
		Validate.checkNotNull(player1, "player should not be null");
		Validate.checkNotNull(title, "title should not be null");
		Validate.checkBoolean(packetclass == null, "Could not updating the inventory title, because it could not invoke the nms classes");

		final Inventory inventory = player1.getOpenInventory().getTopInventory();
		NmsData nmsData = this.newNmsData;
		final int inventorySize = inventory.getSize();
		final boolean isOlder = serverVersion < 17;
		final Object player = player1.getClass().getMethod("getHandle").invoke(player1);
		// inside net.minecraft.world.entity.player and class EntityHuman do you have this field
		final Object activeContainer = player.getClass().getField(nmsData.getContanerField()).get(player);
		// inside net.minecraft.world.inventory and class Container do you have this field newer version is it curretly "j"
		final Object windowId = activeContainer.getClass().getField(nmsData.getWindowId()).get(activeContainer);

		final Method declaredMethodChat;
		final Object inventoryTittle;
		final Object methods;

		String fieldName = nmsData.getContainerFieldnames(inventorySize);
		if (fieldName == null || fieldName.isEmpty()) {
			if (isOlder) fieldName = "GENERIC_9X3";
			else fieldName = "f";
		} else if (isOlder) {
			if (inventorySize % 9 == 0) fieldName = "GENERIC_9X" + fieldName;
		}
		if (serverVersion > 13) {

			declaredMethodChat = chatCompenentSubClass.getMethod("a", String.class);
			inventoryTittle = declaredMethodChat.invoke(null, TextTranslator.toComponent(title));
			final Object inventoryType = containersClass.getField(fieldName).get(null);

			methods = packetConstructor.newInstance(windowId, inventoryType, inventoryTittle);
		} else {

			declaredMethodChat = chatCompenentSubClass.getMethod(serverVersion >= 9.0 ? "b" : "a", String.class);
			inventoryTittle = declaredMethodChat.invoke(null, "'" + TextTranslator.toSpigotFormat(title) + "'");

			methods = packetConstructor.newInstance(windowId, "minecraft:" + inventory.getType().name().toLowerCase(), inventoryTittle, inventorySize);
		}

		final Object handles = handle.invoke(player1);
		final Object playerconect = playerConnection.get(handles);
		// net.minecraft.server.network.PlayerConnection
		final Method packet = packetConnectionClass.getMethod(nmsData.getSendPacket(), packetclass);

		packet.invoke(playerconect, methods);
		// inside net.minecraft.world.inventory.Container do you have method a(Container container)
		player.getClass().getMethod(nmsData.getUpdateInventory(), containerClass).invoke(player, activeContainer);

	}

	private static String versionCheckNms(final String clazzName) {
		return "net.minecraft.server." + Bukkit.getServer().getClass().toGenericString().split("\\.")[3] + "." + clazzName;
	}

	private static String versionCheckBukkit(final String clazzName) {
		return "org.bukkit.craftbukkit." + Bukkit.getServer().getClass().toGenericString().split("\\.")[3] + "." + clazzName;
	}

	private void loadClasses(final float serverVersion) {
		try {
			if (serverVersion < 17) packetclass = Class.forName(versionCheckNms("Packet"));
			else packetclass = Class.forName("net.minecraft.network.protocol.Packet");

			handle = Class.forName(versionCheckBukkit("entity.CraftPlayer")).getMethod("getHandle");
			if (serverVersion < 17)
				playerConnection = Class.forName(versionCheckNms("EntityPlayer")).getField("playerConnection");
			else {
				if (serverVersion >= 20)
					playerConnection = Class.forName("net.minecraft.server.level.EntityPlayer").getField("c");
				else playerConnection = Class.forName("net.minecraft.server.level.EntityPlayer").getField("b");
			}

			if (serverVersion < 17) packetConnectionClass = Class.forName(versionCheckNms("PlayerConnection"));
			else packetConnectionClass = Class.forName("net.minecraft.server.network.PlayerConnection");

			Class<?> chatBaseCompenent;
			if (serverVersion < 17) chatBaseCompenent = Class.forName(versionCheckNms("IChatBaseComponent"));
			else chatBaseCompenent = Class.forName("net.minecraft.network.chat.IChatBaseComponent");

			if (serverVersion > 13)
				if (serverVersion < 17)
					containersClass = Class.forName(versionCheckNms("Containers"));
				else
					containersClass = Class.forName("net.minecraft.world.inventory.Containers");

			else containersClass = String.class;

			if (serverVersion < 17) containerClass = Class.forName(versionCheckNms("Container"));
			else containerClass = Class.forName("net.minecraft.world.inventory.Container");

			if (serverVersion < 17)
				chatCompenentSubClass = Class.forName(versionCheckNms("IChatBaseComponent$ChatSerializer"));
			else
				chatCompenentSubClass = Class.forName("net.minecraft.network.chat.IChatBaseComponent$ChatSerializer");

			if (serverVersion > 13)
				if (serverVersion < 17)
					packetConstructor = Class.forName(versionCheckNms("PacketPlayOutOpenWindow")).getConstructor(int.class, containersClass, chatBaseCompenent);
				else
					packetConstructor = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutOpenWindow").getConstructor(int.class, containersClass, chatBaseCompenent);
			else
				packetConstructor = Class.forName(versionCheckNms("PacketPlayOutOpenWindow")).getConstructor(int.class, containersClass, chatBaseCompenent, int.class);

		} catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException exception) {
			this.titleLogger.sendLOG(exception, Level.WARNING, "An error occurred while updating the inventory title: ");
		}
	}
}
