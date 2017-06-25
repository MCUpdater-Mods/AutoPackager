package org.mcupdater.autopackager.helpers;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;

public class InventoryHelper
{
	public static boolean canStackFitInInventory(InvWrapper target, ItemStack toInsert) {
		if (target.getInv() == null || toInsert.isEmpty()) {
			return false;
		}
		ItemStack remainingItems = toInsert.copy();
		for (int slot = 0; slot < target.getSlots() && !remainingItems.isEmpty(); slot++ ) {
			remainingItems = target.insertItem(slot, remainingItems, true);
		}
		return (remainingItems.isEmpty());
	}

	public static ItemStack insertItemStackIntoInventory(InvWrapper target, ItemStack toInsert) {
		if (target.getInv() == null || toInsert.isEmpty()) {
			return toInsert;
		}
		for (int slot = 0; slot < target.getSlots() && !toInsert.isEmpty(); slot++ ) {
			toInsert = target.insertItem(slot, toInsert, false);
		}
		return toInsert;
	}
}
