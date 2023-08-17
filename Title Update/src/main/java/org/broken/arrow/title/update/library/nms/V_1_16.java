package org.broken.arrow.title.update.library.nms;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public class V_1_16 implements InventoryNMS {

	@Override
	public String containerField() {
		return "activeContainer";
	}
	
	@Override
	public String windowId() {
		return "windowId";
	}

	@Override
	public String sendPacket() {
		return "sendPacket";
	}

	@Override
	public String getUpdateInventoryMethodName() {
		return "updateInventory";
	}

	@Override
	public String containerFieldName(final Inventory currentlyOpenInventory) {

		InventoryType inventoryType = currentlyOpenInventory.getType();
		switch (inventoryType) {
			case CHEST:
				final int inventorySize = currentlyOpenInventory.getSize();
				switch (inventorySize) {
					case 9:
						return "1";
					case 18:
						return "2";
					case 36:
						return "3";
					case 45:
						return "4";
					case 55:
						return "5";
					default:
						return "6";
				}
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
