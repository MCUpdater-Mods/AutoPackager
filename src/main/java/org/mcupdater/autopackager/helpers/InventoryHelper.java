package org.mcupdater.autopackager.helpers;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.EmptyHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

public class InventoryHelper
{
	public static boolean canStackFitInInventory(IItemHandler target, ItemStack toInsert) {
		if (toInsert.isEmpty()) {
			return false;
		}
		ItemStack remainingItems = toInsert.copy();
		for (int slot = 0; slot < target.getSlots() && !remainingItems.isEmpty(); slot++ ) {
			remainingItems = target.insertItem(slot, remainingItems, true);
		}
		return (remainingItems.isEmpty());
	}

	public static ItemStack insertItemStackIntoInventory(IItemHandler target, ItemStack toInsert) {
		if (toInsert.isEmpty()) {
			return toInsert;
		}
		for (int slot = 0; slot < target.getSlots() && !toInsert.isEmpty(); slot++ ) {
			toInsert = target.insertItem(slot, toInsert, false);
		}
		return toInsert;
	}

	public static IItemHandler getWrapper(TileEntity tileEntity, EnumFacing side) {
		if (tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)) {
			return tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
		} else if (tileEntity instanceof ISidedInventory) {
			return new SidedInvWrapper((ISidedInventory) tileEntity, side);
		} else if (tileEntity instanceof IInventory) {
			return new InvWrapper((IInventory) tileEntity);
		}
		return EmptyHandler.INSTANCE;
	}
}
