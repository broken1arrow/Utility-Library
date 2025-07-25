package org.broken.arrow.library.title.update;

import com.google.gson.JsonObject;
import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.logging.Validate;
import org.broken.arrow.library.title.update.nms.InventoryNMS;
import org.broken.arrow.library.title.update.utility.TitleUtility;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

public class ContainerUtility {
	private static final Logging logger = new Logging(ContainerUtility.class);
	private Class<?> packetClass;
	private Method handle;
	private Field playerConnection;
	private Class<?> packetConnectionClass;
	private Method chatComponentMethod;
	private Method chatComponentMethodString;
	private Class<?> containersClass;
	private Class<?> containerClass;
	private Constructor<?> packetConstructor;
	private final float serverVersion;
	private final InventoryNMS inventoryNMS;

	protected ContainerUtility(final InventoryNMS inventoryNMS, final float serverVersion) {
		this.serverVersion = serverVersion;
		this.inventoryNMS = inventoryNMS;
		loadClasses(serverVersion);
	}

	protected void updateInventory(@Nonnull final Player player, final TitleUtility titleUtility) throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException, InstantiationException {
		Validate.checkNotNull(player, "Player should not be null");
		Validate.checkBoolean(packetClass == null, "Could not updating the inventory title, because it could not invoke the nms classes");
		Object title = titleUtility.getTitle(this.serverVersion);
		Validate.checkNotNull(title, "Title should not be null");
		final Inventory inventory = player.getOpenInventory().getTopInventory();
		InventoryNMS nms = this.inventoryNMS;
		final int inventorySize = inventory.getSize();

		//final Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
		Object entityPlayer = handle.invoke(player);
		Class<?> entityPlayerClass = entityPlayer.getClass();
		// inside net.minecraft.world.entity.player and class EntityHuman do you have this field for Container class
		final Object activeContainer = entityPlayerClass.getField(nms.getContainerField()).get(entityPlayer);
		// inside net.minecraft.world.inventory and class Container do you have this field newer version it is currently "j"
		final Object windowId = activeContainer.getClass().getField(nms.getWindowId()).get(activeContainer);

		final Method chatSerialMethod = this.chatComponentMethod; //.getMethod("a", String.class); //before 1.14-> .getMethod(serverVersion >= 9.0 ? "b" : "a", String.class);
		final Object inventoryTitle;
		final Object packetInstance;
		final Object inventoryType;

		String fieldName = nms.getContainerFieldName(inventory);
		if (fieldName == null || fieldName.isEmpty()) {
			logger.log(Level.WARNING,()-> "Could not update title for this inventory: " + inventory);
			return;
		}

		if (serverVersion > 13) {
			if (title instanceof JsonObject || this.chatComponentMethodString == null)
				inventoryTitle = chatSerialMethod.invoke(null, title.toString());
			else
				inventoryTitle = this.chatComponentMethodString.invoke(null, title.toString());
			inventoryType = containersClass.getField(fieldName).get(null);
			packetInstance = packetConstructor.newInstance(windowId, inventoryType, inventoryTitle);
		} else {
			inventoryTitle = chatSerialMethod.invoke(null, title);
			packetInstance = packetConstructor.newInstance(windowId, fieldName, inventoryTitle, inventorySize);
		}

		final Object playerConnect = playerConnection.get(entityPlayer);
		// net.minecraft.server.network.PlayerConnection
		final Method packet = packetConnectionClass.getMethod(nms.getSendPacketName(), packetClass);
		packet.invoke(playerConnect, packetInstance);
		// inside net.minecraft.world.inventory.Container do you have method a(Container container)
		// This part make sure the inventory gets updated properly.
		entityPlayerClass.getMethod(nms.getUpdateInventoryMethodName(), containerClass).invoke(entityPlayer, activeContainer);

	}

	private void loadClasses(final float serverVersion) {
		try {
			InventoryNMS nms = this.inventoryNMS;
			this.packetClass = nms.getPacket();
			this.handle = nms.getPlayerHandle();
			this.playerConnection = nms.getPlayerConnection();
			this.packetConnectionClass = nms.getPlayerConnectionClass();
			this.containersClass = nms.getContainersClass();
			this.containerClass = nms.getContainerClass();
			Class<?> chatBaseComponent = nms.getChatSerializer();
			if (serverVersion < 17)
				this.chatComponentMethod = chatBaseComponent.getMethod(serverVersion >= 9.0 ? "b" : "a", String.class);
			else {
				this.chatComponentMethod = chatBaseComponent.getMethod("a", String.class);
				this.chatComponentMethodString = chatBaseComponent.getMethod("b", String.class);
			}
			this.packetConstructor = nms.getPacketPlayOutOpenWindow();

		} catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException exception) {
			logger.log(Level.WARNING,exception,()-> "An error occurred while updating the inventory title: ");
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
