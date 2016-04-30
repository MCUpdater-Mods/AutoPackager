package org.mcupdater.autopackager.helpers;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;

public class InventoryHelper
{
	public static boolean canStackFitInInventory(InvWrapper target, ItemStack toInsert) {
		if (target.getInv() == null || toInsert == null) {
			return false;
		}
		ItemStack remainingItems = toInsert.copy();
		for (int slot = 0; slot < target.getSlots() && (remainingItems != null && remainingItems.stackSize > 0); slot++ ) {
			remainingItems = target.insertItem(slot, remainingItems, true);
		}
		return (remainingItems == null || remainingItems.stackSize == 0);
	}

	public static ItemStack insertItemStackIntoInventory(InvWrapper target, ItemStack toInsert) {
		if (target.getInv() == null || toInsert == null) {
			return toInsert;
		}
		for (int slot = 0; slot < target.getSlots() && (toInsert != null && toInsert.stackSize > 0); slot++ ) {
			toInsert = target.insertItem(slot, toInsert, false);
		}
		return toInsert;
	}
}
