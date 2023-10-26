package org.broken.arrow.title.update.library.nms.modules;

import org.broken.arrow.title.update.library.nms.InventoryNMS;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class V_1_16_Inventory implements InventoryNMS {

	@Override
	public Class<?> getPacket() throws ClassNotFoundException {
		return Class.forName(retrieveNMSPackage("Packet"));
	}

	@Override
	public Field getPlayerConnection() throws ClassNotFoundException, NoSuchFieldException {
		return Class.forName(retrieveNMSPackage("EntityPlayer")).getField("playerConnection");
	}

	@Override
	public Class<?> getPlayerConnectionClass() throws ClassNotFoundException {
		return Class.forName(retrieveNMSPackage("PlayerConnection"));
	}

	@Override
	public Class<?> getContainersClass() throws ClassNotFoundException {
		return Class.forName(retrieveNMSPackage("Containers"));
	}

	@Override
	public Class<?> getContainerClass() throws ClassNotFoundException {
		return Class.forName(retrieveNMSPackage("Container"));
	}

	@Override
	public Class<?> getChatSerializer() throws ClassNotFoundException {
		return Class.forName(retrieveNMSPackage("IChatBaseComponent$ChatSerializer"));
	}

	@Override
	public Constructor<?> getPacketPlayOutOpenWindow() throws ClassNotFoundException, NoSuchMethodException {
		Class<?> iChatBaseComponent = Class.forName(retrieveNMSPackage("IChatBaseComponent"));
		return Class.forName(retrieveNMSPackage("PacketPlayOutOpenWindow")).getConstructor(int.class, this.getContainersClass(), iChatBaseComponent);
	}

	@Nonnull
	@Override
	public String getContainerField() {
		return "activeContainer";
	}

	@Nonnull
	@Override
	public String getWindowId() {
		return "windowId";
	}

	@Nonnull
	@Override
	public String getSendPacketName() {
		return "sendPacket";
	}

	@Nonnull
	@Override
	public String getUpdateInventoryMethodName() {
		return "updateInventory";
	}

	@Override
	public String getContainerFieldName(@Nonnull final Inventory currentlyOpenInventory) {
		InventoryType inventoryType = currentlyOpenInventory.getType();

		switch (inventoryType) {
			case CHEST:
				final int inventorySize = currentlyOpenInventory.getSize();
				switch (inventorySize) {
					case 9:
						return "GENERIC_9X1";
					case 18:
						return "GENERIC_9X2";
					case 27:
						return "GENERIC_9X3";
					case 36:
						return "GENERIC_9X4";
					case 45:
						return "GENERIC_9X5";
					default:
						if (inventorySize == 54)
							return "GENERIC_9X6";
						break;
				}
				break;
			case DISPENSER:
			case DROPPER:
				return "GENERIC_3X3";
		/*todo should this be implemented and can you update title? you find the field for InventoryEnderChest inside net.minecraft.world.entity.player.EntityHuman
			case ENDER_CHEST:
				break;*/
			case ANVIL:
				return "ANVIL";
			case HOPPER:
				return "HOPPER";
			case SHULKER_BOX:
				return "SHULKER_BOX";
        /*todo should this be implemented? class do you find in net.minecraft.world.level.block.entity.TileEntityBarrel.
           Check this nms code it use the field 'protected static final DataWatcherObject<NBTTagCompound> bR;':
              protected boolean a(EntityHuman entityhuman) {
                if (entityhuman.bR instanceof ContainerChest) {
                    IInventory iinventory = ((ContainerChest)entityhuman.bR).l();
                    return iinventory == TileEntityBarrel.this;
                } else {
                    return false;
                }
            }
			case BARREL:
				break;*/
		}
		return null;
	}
}
