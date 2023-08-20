package org.broken.arrow.title.update.library;

import org.broken.arrow.color.library.TextTranslator;
import org.broken.arrow.title.update.library.nms.InventoryNMS;
import org.broken.arrow.title.update.library.utility.TitleLogger;
import org.broken.arrow.title.update.library.utility.Validate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

public class ContainerUtility {
	private Class<?> packetClass;
	private Method handle;
	private Field playerConnection;
	private Class<?> packetConnectionClass;
	private Method chatComponentMethod;
	private Class<?> containersClass;
	private Class<?> containerClass;
	private Constructor<?> packetConstructor;
	private final float serverVersion;
	private final InventoryNMS inventoryNMS;
	private final TitleLogger titleLogger;

	protected ContainerUtility(final InventoryNMS inventoryNMS, final float serverVersion) {
		titleLogger = new TitleLogger(ContainerUtility.class);
		this.serverVersion = serverVersion;
		this.inventoryNMS = inventoryNMS;
		loadClasses(serverVersion);
	}

	protected void updateInventory(@Nonnull final Player player, final String title) throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException, InstantiationException {
		Validate.checkNotNull(player, "player should not be null");
		Validate.checkNotNull(title, "title should not be null");
		Validate.checkBoolean(packetClass == null, "Could not updating the inventory title, because it could not invoke the nms classes");

		final Inventory inventory = player.getOpenInventory().getTopInventory();
		InventoryNMS inventoryNMS = this.inventoryNMS;
		final int inventorySize = inventory.getSize();
		final boolean isOlder = serverVersion < 17;
		//final Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
		Object entityPlayer = handle.invoke(player);
		Class<?> entityPlayerClass = entityPlayer.getClass();
		// inside net.minecraft.world.entity.player and class EntityHuman do you have this field
		final Object activeContainer = entityPlayerClass.getField(inventoryNMS.getContainerField()).get(entityPlayer);
		// inside net.minecraft.world.inventory and class Container do you have this field newer version it is currently "j"
		final Object windowId = activeContainer.getClass().getField(inventoryNMS.getWindowId()).get(activeContainer);

		final Method chatSerialMethod = this.chatComponentMethod; //.getMethod("a", String.class); //before 1.14-> .getMethod(serverVersion >= 9.0 ? "b" : "a", String.class);
		final Object inventoryTitle;
		final Object packetInstance;
		final Object inventoryType;

		String fieldName = inventoryNMS.getContainerFieldName(inventory);

		if (fieldName == null || fieldName.isEmpty()) {
			if (isOlder) fieldName = "GENERIC_9X3";
			else fieldName = "f";
		} else if (isOlder) {
			if (inventorySize % 9 == 0) fieldName = "GENERIC_9X" + fieldName;
		}
		if (serverVersion > 13) {
			inventoryTitle = chatSerialMethod.invoke(null, TextTranslator.toComponent(title));
			inventoryType = containersClass.getField(fieldName).get(null);
			packetInstance = packetConstructor.newInstance(windowId, inventoryType, inventoryTitle);
		} else {
			inventoryTitle = chatSerialMethod.invoke(null, "'" + TextTranslator.toSpigotFormat(title) + "'");
			inventoryType = inventoryNMS.getContainerFieldName(inventory);
			packetInstance = packetConstructor.newInstance(windowId, inventoryType, inventoryTitle, inventorySize);
		}
		//final Object handles = handle.invoke(player);
		final Object playerConnect = playerConnection.get(entityPlayer);
		// net.minecraft.server.network.PlayerConnection
		final Method packet = packetConnectionClass.getMethod(inventoryNMS.getSendPacketName(), packetClass);
		packet.invoke(playerConnect, packetInstance);
		// inside net.minecraft.world.inventory.Container do you have method a(Container container)
		// This part make sure the inventory gets updated properly.
		entityPlayerClass.getMethod(inventoryNMS.getUpdateInventoryMethodName(), containerClass).invoke(entityPlayer, activeContainer);

	}

	private void loadClasses(final float serverVersion) {
		try {
			InventoryNMS inventoryNMS = this.inventoryNMS;
			this.packetClass = inventoryNMS.getPacket();
			this.handle = inventoryNMS.getPlayerHandle();
			this.playerConnection = inventoryNMS.getPlayerConnection();
			this.packetConnectionClass = inventoryNMS.getPlayerConnectionClass();
			this.containersClass = inventoryNMS.getContainersClass();
			this.containerClass = inventoryNMS.getContainerClass();
			Class<?> chatBaseComponent = inventoryNMS.getChatSerializer();
			if (serverVersion < 17)
				this.chatComponentMethod = chatBaseComponent.getMethod(serverVersion >= 9.0 ? "b" : "a", String.class);
			else
				this.chatComponentMethod = chatBaseComponent.getMethod("a", String.class);
			this.packetConstructor = inventoryNMS.getPacketPlayOutOpenWindow();

		} catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException exception) {
			this.titleLogger.sendLOG(exception, Level.WARNING, "An error occurred while updating the inventory title: ");
		}
	}

	@Override
	public String toString() {
		return "ContainerUtility{" +
				"packetClass=" + packetClass +
				", handle=" + handle +
				", playerConnection=" + playerConnection +
				", packetConnectionClass=" + packetConnectionClass +
				", chatComponentMethod=" + chatComponentMethod +
				", containersClass=" + containersClass +
				", containerClass=" + containerClass +
				", packetConstructor=" + packetConstructor +
				", serverVersion=" + serverVersion +
				", inventoryNMS=" + inventoryNMS +
				'}';
	}
}
