package org.mcupdater.autopackager.helpers;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.items.wrapper.InvWrapper;

public class InventoryHelper
{
	public static boolean canStackFitInInventory(InvWrapper target, ItemStack toInsert) {
		if (target.getInv() == null || toInsert == null) {
			return false;
		}
		ItemStack remainingItems = toInsert.copy();
		FMLLog.info("check - Slots: " + target.getSlots());
		for (int slot = 0; slot < target.getSlots() && (remainingItems != null && remainingItems.stackSize > 0); slot++ ) {
			FMLLog.info("check - Slot: " + slot); // + " (" + target.getStackInSlot(slot).toString() + ")");
			remainingItems = target.insertItem(slot, remainingItems, true);
		}
		return (remainingItems == null || remainingItems.stackSize == 0);
	}

	public static ItemStack insertItemStackIntoInventory(InvWrapper target, ItemStack toInsert) {
		if (target.getInv() == null || toInsert == null) {
			return toInsert;
		}
		FMLLog.info("insert - Slots: " + target.getSlots());
		for (int slot = 0; slot < target.getSlots() && (toInsert != null && toInsert.stackSize > 0); slot++ ) {
			FMLLog.info("insert - Slot: " + slot); // + " (" + target.getStackInSlot(slot).toString() + ")");
			toInsert = target.insertItem(slot, toInsert, false);
		}
		return toInsert;
	}
}
