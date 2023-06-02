package org.broken.arrow.menu.library.NMS;

import org.broken.arrow.menu.library.utility.MenuLogger;
import org.broken.arrow.menu.library.utility.ServerVersion;
import org.broken.arrow.menu.library.utility.Validate;
import org.broken.lib.rbg.TextTranslator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class UpdateTittleContainers {

	private static Class<?> packetclass;
	private static Method handle;
	private static Field playerConnection;
	private static Class<?> packetConnectionClass;
	private static Class<?> chatBaseCompenent;
	private static Class<?> chatCompenentSubClass;
	private static Class<?> containersClass;
	private static Class<?> containerClass;
	private static Constructor<?> packetConstructor;
	private static NmsData nmsData;
	private static MenuLogger menuLogger;

	public static void update(final Player p, final String title) {
		NmsData newNmsData = getNmsData();
		if (menuLogger != null) {
			menuLogger.sendLOG(Level.WARNING, "There was an error the last time you tried to update the title. Send the stack trace to the delevoper.");
			return;
		}
		try {
			if (p != null) {
				final Map<Integer, String> inventorySizeNames;
				if (ServerVersion.atLeast(ServerVersion.v1_17)) {
					if (newNmsData == null) {
						inventorySizeNames = convertFieldNames(new FieldName(9, "a"),
								new FieldName(18, "b"), new FieldName(27, "c"), new FieldName(36, "d"),
								new FieldName(45, "e"), new FieldName(54, "f"), new FieldName(5, "p"));

						if (ServerVersion.atLeast(ServerVersion.v1_19)) {
							if (ServerVersion.atLeast(ServerVersion.v1_19_4))
								newNmsData = new NmsData("bP", "j",
										"a", "a", inventorySizeNames);
							else
								newNmsData = new NmsData("bU", "j",
										"a", "a", inventorySizeNames);

						} else if (ServerVersion.atLeast(ServerVersion.v1_18_0)) {
							newNmsData = new NmsData(ServerVersion.atLeast(ServerVersion.v1_18_2) ? "bV" : "bW", "j",
									"a", "a", inventorySizeNames);
						} else if (ServerVersion.equals(ServerVersion.v1_17)) {
							newNmsData = new NmsData("bV", "j", "sendPacket", "initMenu", inventorySizeNames);
						}
					}
				} else if (ServerVersion.olderThan(ServerVersion.v1_17)) {
					if (newNmsData == null) {
						inventorySizeNames = convertFieldNames(new FieldName(9, "1"),
								new FieldName(18, "2"), new FieldName(27, "3"), new FieldName(36, "4"),
								new FieldName(45, "5"), new FieldName(54, "6"), new FieldName(5, "HOPPER"));
						newNmsData = new NmsData("activeContainer", "windowId",
								"sendPacket", "updateInventory", inventorySizeNames);
					}
				}
				if (nmsData == null)
					nmsData = newNmsData;

				if (newNmsData != null) {
					loadNmsClasses();
					updateInventory(p, title);
				}

			}
		} catch (final NoSuchFieldException | ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | InstantiationException e) {
			menuLogger = new MenuLogger(UpdateTittleContainers.class);
			e.printStackTrace();
		}
	}


	private static void loadNmsClasses() throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException {
		if (ServerVersion.newerThan(ServerVersion.v1_16)) {
			loadNmsClasses1_17();
			return;
		}
		if (packetclass == null)
			packetclass = Class.forName(versionCheckNms("Packet"));
		if (handle == null)
			handle = Class.forName(versionCheckBukkit("entity.CraftPlayer")).getMethod("getHandle");
		if (playerConnection == null)
			playerConnection = Class.forName(versionCheckNms("EntityPlayer")).getField("playerConnection");
		if (packetConnectionClass == null)
			packetConnectionClass = Class.forName(versionCheckNms("PlayerConnection"));
		if (chatBaseCompenent == null)
			chatBaseCompenent = Class.forName(versionCheckNms("IChatBaseComponent"));
		if (containersClass == null)
			if (ServerVersion.newerThan(ServerVersion.v1_13))
				containersClass = Class.forName(versionCheckNms("Containers"));
			else
				containersClass = String.class;
		if (containerClass == null)
			containerClass = Class.forName(versionCheckNms("Container"));
		if (chatCompenentSubClass == null)
			chatCompenentSubClass = Class.forName(versionCheckNms("IChatBaseComponent$ChatSerializer"));
		if (packetConstructor == null)
			if (ServerVersion.newerThan(ServerVersion.v1_13))
				packetConstructor = Class.forName(versionCheckNms("PacketPlayOutOpenWindow")).getConstructor(int.class, containersClass, chatBaseCompenent);
			else
				packetConstructor = Class.forName(versionCheckNms("PacketPlayOutOpenWindow")).getConstructor(int.class, containersClass, chatBaseCompenent, int.class);
	}

	private static void loadNmsClasses1_17() throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException {

		if (packetclass == null)
			packetclass = Class.forName("net.minecraft.network.protocol.Packet");
		if (handle == null)
			handle = Class.forName(versionCheckBukkit("entity.CraftPlayer")).getMethod("getHandle");
		if (playerConnection == null)
			playerConnection = Class.forName("net.minecraft.server.level.EntityPlayer").getField("b");
		if (packetConnectionClass == null)
			packetConnectionClass = Class.forName("net.minecraft.server.network.PlayerConnection");
		if (chatBaseCompenent == null)
			chatBaseCompenent = Class.forName("net.minecraft.network.chat.IChatBaseComponent");
		if (containersClass == null)
			containersClass = Class.forName("net.minecraft.world.inventory.Containers");
		if (containerClass == null)
			containerClass = Class.forName("net.minecraft.world.inventory.Container");
		if (chatCompenentSubClass == null)
			chatCompenentSubClass = Class.forName("net.minecraft.network.chat.IChatBaseComponent$ChatSerializer");
		if (packetConstructor == null)
			packetConstructor = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutOpenWindow").getConstructor(int.class, containersClass, chatBaseCompenent);
	}

	private static void loadNmsClasses1_18() throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException {

		if (packetclass == null)
			packetclass = Class.forName("net.minecraft.network.protocol.Packet");
		if (handle == null)
			handle = Class.forName(versionCheckBukkit("entity.CraftPlayer")).getMethod("getHandle");
		if (playerConnection == null)
			playerConnection = Class.forName("net.minecraft.server.level.EntityPlayer").getField("b");
		if (packetConnectionClass == null)
			packetConnectionClass = Class.forName("net.minecraft.server.network.PlayerConnection");
		if (chatBaseCompenent == null)
			chatBaseCompenent = Class.forName("net.minecraft.network.chat.IChatBaseComponent");
		if (containersClass == null)
			containersClass = Class.forName("net.minecraft.world.inventory.Containers");
		if (containerClass == null)
			containerClass = Class.forName("net.minecraft.world.inventory.Container");
		if (chatCompenentSubClass == null)
			chatCompenentSubClass = Class.forName("net.minecraft.network.chat.IChatBaseComponent$ChatSerializer");
		if (packetConstructor == null)
			packetConstructor = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutOpenWindow").getConstructor(int.class, containersClass, chatBaseCompenent);
	}

	private static void updateInventory(@Nonnull final Player p, final String title) throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException, InstantiationException {
		Validate.checkNotNull(p, "player should not be null");
		Validate.checkNotNull(title, "title should not be null");
		final Inventory inventory = p.getOpenInventory().getTopInventory();
		final NmsData nmsData = getNmsData();
		final int inventorySize = inventory.getSize();
		final boolean isOlder = ServerVersion.olderThan(ServerVersion.v1_17);
		final Object player = p.getClass().getMethod("getHandle").invoke(p);
		// inside net.minecraft.world.entity.player and class EntityHuman do you have this field
		final Object activeContainer = player.getClass().getField(nmsData.getContanerField()).get(player);
		// inside net.minecraft.world.inventory and class Container do you have this field newer version is it curretly "j"
		final Object windowId = activeContainer.getClass().getField(nmsData.getWindowId()).get(activeContainer);

		final Method declaredMethodChat;
		final Object inventoryTittle;
		final Object methods;

		String fieldName = nmsData.getContainerFieldnames(inventorySize);
		if (fieldName == null || fieldName.isEmpty()) {
			if (isOlder)
				fieldName = "GENERIC_9X3";
			else
				fieldName = "f";
		} else if (isOlder) {
			if (inventorySize % 9 == 0)
				fieldName = "GENERIC_9X" + fieldName;
		}
		if (ServerVersion.newerThan(ServerVersion.v1_13)) {

			declaredMethodChat = chatCompenentSubClass.getMethod("a", String.class);
			inventoryTittle = declaredMethodChat.invoke(null, TextTranslator.toComponent(title));
			final Object inventoryType = containersClass.getField(fieldName).get(null);

			methods = packetConstructor.newInstance(windowId, inventoryType, inventoryTittle);
		} else {

			declaredMethodChat = chatCompenentSubClass.getMethod(ServerVersion.atLeast(ServerVersion.v1_9) ? "b" : "a", String.class);
			inventoryTittle = declaredMethodChat.invoke(null, "'" + TextTranslator.toSpigotFormat(title) + "'");

			methods = packetConstructor.newInstance(windowId, "minecraft:" + inventory.getType().name().toLowerCase(), inventoryTittle, inventorySize);
		}

		final Object handles = handle.invoke(p);
		final Object playerconect = playerConnection.get(handles);
		// net.minecraft.server.network.PlayerConnection
		final Method packet1 = packetConnectionClass.getMethod(nmsData.getSendPacket(), packetclass);

		packet1.invoke(playerconect, methods);
		// inside net.minecraft.world.inventory.Container do you have method a(Container container)
		player.getClass().getMethod(nmsData.getUpdateInventory(), containerClass).invoke(player, activeContainer);

	}

	private static String versionCheckNms(final String clazzName) {

		return "net.minecraft.server." + Bukkit.getServer().getClass().toGenericString().split("\\.")[3] + "." + clazzName;
	}

	private static String versionCheckBukkit(final String clazzName) {

		return "org.bukkit.craftbukkit." + Bukkit.getServer().getClass().toGenericString().split("\\.")[3] + "." + clazzName;
	}


	/**
	 * Use the method like this 9;a ("9" is inventory size and "a" is the field name).
	 * Is used to get the field for diffrent inventorys in nms class.
	 *
	 * @param fieldNames set the name to get right container inventory.
	 */
	private static Map<Integer, String> convertFieldNames(final FieldName... fieldNames) {
		final Map<Integer, String> inventoryFieldname = new HashMap<>();
		for (final FieldName fieldName : fieldNames) {
			inventoryFieldname.put(fieldName.getInventorySize(), fieldName.getFieldName());
		}
		return inventoryFieldname;
	}

	private static NmsData getNmsData() {
		return nmsData;
	}

	private static class NmsData {

		private final String contanerField;
		private final String windowId;
		private final String sendPacket;
		private final String updateInventory;
		private final Map<Integer, String> containerFieldnames;

		public NmsData(final String contanerField, final String windowId, final String sendPacket, final String updateInventory, final Map<Integer, String> containerFieldnames) {
			this.contanerField = contanerField;
			this.windowId = windowId;
			this.sendPacket = sendPacket;
			this.updateInventory = updateInventory;
			this.containerFieldnames = containerFieldnames;
		}

		/**
		 * inside net.minecraft.world.entity.player.EntityHuman do you have this field.
		 *
		 * @return field name.
		 */
		public String getContanerField() {
			return contanerField;
		}

		/**
		 * This is uesd to get current id of a inventory (is intriger)
		 * <p>
		 * The field in this class net.minecraft.world.entity.player.EntityHuman.
		 *
		 * @return the field name.
		 */
		public String getWindowId() {
			return windowId;
		}

		/**
		 * take the method from this class net.minecraft.server.network.PlayerConnection .
		 * This method a(Packet packet) older versions is it sendPacket(Packet packet).
		 *
		 * @return method name.
		 */
		public String getSendPacket() {
			return sendPacket;
		}

		/**
		 * take the field from this class net.minecraft.world.inventory.Container .
		 * This method a(Container container) older versions is it initMenu or updateInventory.
		 *
		 * @return method name.
		 */
		public String getUpdateInventory() {
			return updateInventory;
		}

		/**
		 * Get the name for the container type fieald name.
		 *
		 * @param inventorySize the size of the inventory.
		 * @return the right name for the field player currently open.
		 */
		public String getContainerFieldnames(final int inventorySize) {
			return containerFieldnames.get(inventorySize);
		}
	}

	private static class FieldName {
		private final int inventorySize;
		private final String fieldName;

		public FieldName(final int size, final String fieldName) {
			this.inventorySize = size;
			this.fieldName = fieldName;
		}

		/**
		 * Get the size of the inventory.
		 *
		 * @return inventory size.
		 */
		public int getInventorySize() {
			return inventorySize;
		}

		/**
		 * Get the field name for the current inventory.
		 *
		 * @return the name.
		 */
		public String getFieldName() {
			return fieldName;
		}
	}

}
