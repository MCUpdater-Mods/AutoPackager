package org.mcupdater.autopackager;

import cofh.api.energy.TileEnergyHandler;
import cofh.util.InventoryHelper;
import com.dynious.refinedrelocation.api.APIUtils;
import com.dynious.refinedrelocation.api.tileentity.ISortingMember;
import com.dynious.refinedrelocation.api.tileentity.handlers.ISortingMemberHandler;
import cpw.mods.fml.common.Optional;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import org.mcupdater.shared.Position;

@Optional.Interface(iface = "com.dynious.refinedrelocation.api.tileentity.ISortingMember", modid = "RefinedRelocation")
public class TilePackager extends TileEnergyHandler implements ISortingMember
{
	private Object sortingHandler;
	private ForgeDirection orientation;
	static int tickCounter = 0;

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (++tickCounter % 10 == 0) {
			if (storage.getEnergyStored() > 1000) {
				if (tryCraft()) {
					storage.extractEnergy(1000, false);
				}
			}
		}
	}

	private boolean tryCraft() {
		if (orientation == null) {
			return false;
		}
		Position inputPos = new Position(xCoord, yCoord, zCoord, orientation);
		Position outputPos = new Position(xCoord, yCoord, zCoord, orientation);
		inputPos.moveLeft(1.0);
		outputPos.moveRight(1.0);
		TileEntity tileInput = worldObj.getBlockTileEntity((int)inputPos.x, (int)inputPos.y, (int)inputPos.z);
		TileEntity tileOutput = worldObj.getBlockTileEntity((int)outputPos.x, (int)outputPos.y, (int)outputPos.z);
		if (tileInput instanceof IInventory && tileOutput instanceof IInventory) {
			IInventory invInput = (IInventory) tileInput;
			IInventory invOutput = (IInventory) tileOutput;
			for (int slot = 0; slot < invInput.getSizeInventory(); slot++) {
				if (invInput.getStackInSlot(slot) != null && invInput.getStackInSlot(slot).stackSize >= 4) {
					ItemStack testStack = invInput.getStackInSlot(slot).copy();
					testStack.stackSize = 1;
					InventoryCrafting smallCraft = new InventoryCrafting(new Container(){
						@Override
						public boolean canInteractWith(EntityPlayer entityPlayer) {
							return false;
						}
					}, 2, 2);
					for (int craftSlot = 0; craftSlot < 4; craftSlot++) {
						smallCraft.setInventorySlotContents(craftSlot, testStack);
					}
					ItemStack result = CraftingManager.getInstance().findMatchingRecipe(smallCraft, worldObj);
					if (result != null) {
						testStack = InventoryHelper.simulateInsertItemStackIntoInventory(invOutput, result, 1);
						if (testStack == null) {
							invInput.getStackInSlot(slot).splitStack(4);
							if (invInput.getStackInSlot(slot).stackSize == 0) {
								invInput.setInventorySlotContents(slot, null);
							}
							InventoryHelper.insertItemStackIntoInventory(invOutput, result, 1);
							return true;
						}
					}
				}
				if (invInput.getStackInSlot(slot) != null && invInput.getStackInSlot(slot).stackSize >= 9) {
					ItemStack testStack = invInput.getStackInSlot(slot).copy();
					testStack.stackSize = 1;
					InventoryCrafting largeCraft = new InventoryCrafting(new Container(){
						@Override
						public boolean canInteractWith(EntityPlayer entityPlayer) {
							return false;
						}
					}, 3, 3);
					for (int craftSlot = 0; craftSlot < 9; craftSlot++) {
						largeCraft.setInventorySlotContents(craftSlot, testStack);
					}
					ItemStack result = CraftingManager.getInstance().findMatchingRecipe(largeCraft, worldObj);
					if (result != null) {
						testStack = InventoryHelper.simulateInsertItemStackIntoInventory(invOutput, result, 1);
						if (testStack == null) {
							invInput.getStackInSlot(slot).splitStack(9);
							if (invInput.getStackInSlot(slot).stackSize == 0) {
								invInput.setInventorySlotContents(slot, null);
							}
							InventoryHelper.insertItemStackIntoInventory(invOutput, result, 1);
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	public void writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		tagCompound.setInteger("orientation",orientation.ordinal());
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		this.orientation = ForgeDirection.getOrientation(tagCompound.getInteger("orientation"));
	}

	public void setOrientation(ForgeDirection orientation) {
		this.orientation = orientation;
	}

	@Optional.Method(modid = "RefinedRelocation")
	@Override
	public ISortingMemberHandler getSortingHandler() {
		if (sortingHandler == null) {
			sortingHandler = APIUtils.createSortingMemberHandler(this);
		}
		return (ISortingMemberHandler) sortingHandler;
	}
}
