package org.broken.arrow.title.update.library.nms.modules.v_1_17;

import org.broken.arrow.title.update.library.nms.InventoryNMS;
import org.bukkit.inventory.Inventory;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class InventoryPacketSeventeen implements InventoryNMS {


	@Override
	public Class<?> getPacket() throws ClassNotFoundException {
		return Class.forName("net.minecraft.network.protocol.Packet");
	}

	@Override
	public Field getPlayerConnection() throws ClassNotFoundException, NoSuchFieldException {
		return Class.forName("net.minecraft.server.level.EntityPlayer").getField("b");
	}

	@Override
	public Class<?> getPlayerConnectionClass() throws ClassNotFoundException {
		return Class.forName("net.minecraft.server.network.PlayerConnection");
	}

	@Override
	public Class<?> getContainersClass() throws ClassNotFoundException {
		return Class.forName("net.minecraft.world.inventory.Containers");
	}

	@Override
	public Class<?> getContainerClass() throws ClassNotFoundException {
		return Class.forName("net.minecraft.world.inventory.Container");
	}

	@Override
	public Class<?> getChatSerializer() throws ClassNotFoundException {
		return Class.forName("net.minecraft.network.chat.IChatBaseComponent$ChatSerializer");
	}

	@Override
	public Constructor<?> getPacketPlayOutOpenWindow() throws ClassNotFoundException, NoSuchMethodException {
		Class<?> iChatBaseComponent = Class.forName("net.minecraft.network.chat.IChatBaseComponent");
		return Class.forName("net.minecraft.network.protocol.game.PacketPlayOutOpenWindow").getConstructor(int.class, this.getContainersClass(), iChatBaseComponent);
	}

	@Nonnull
	@Override
	public String getContainerField() {
		return "bV";
	}

	@Nonnull
	@Override
	public String getWindowId() {
		return "j";
	}

	@Nonnull
	@Override
	public String getSendPacketName() {
		return "sendPacket";
	}

	@Nonnull
	@Override
	public String getUpdateInventoryMethodName() {
		return "initMenu";
	}

	@Override
	public String getContainerFieldName(@Nonnull final Inventory currentlyOpenInventory) {
		switch (currentlyOpenInventory.getType()) {
			case CHEST:
				final int inventorySize = currentlyOpenInventory.getSize();
				switch (inventorySize) {
					case 9:
						return "a";
					case 18:
						return "b";
					case 27:
						return "c";
					case 36:
						return "d";
					case 45:
						return "e";
					case 54:
						return "f";
					default:
						return "c";
				}
			case DISPENSER:
			case DROPPER:
				return "g";
		/*todo should this be implemented and can you update title? you find the field for InventoryEnderChest inside net.minecraft.world.entity.player.EntityHuman
			case ENDER_CHEST:
				break;*/
			case ANVIL:
				return "h";
			case HOPPER:
				return "p";
			case SHULKER_BOX:
				return "t";
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
