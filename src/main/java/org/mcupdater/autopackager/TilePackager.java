package org.mcupdater.autopackager;

import cofh.api.energy.TileEnergyHandler;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import org.mcupdater.shared.Position;

public class TilePackager extends TileEnergyHandler
{
	@Override
	public void updateEntity() {
		super.updateEntity();
		if (storage.getEnergyStored() > 1000) {
			storage.extractEnergy(1000,false);
			if (!tryCraft()) {
				storage.receiveEnergy(1000,false);
			}
		}
	}

	private boolean tryCraft() {
		ForgeDirection orientation = ForgeDirection.getOrientation(this.blockMetadata);
		Position inputPos = new Position(xCoord, yCoord, zCoord, orientation);
		Position outputPos = new Position(xCoord, yCoord, zCoord, orientation);
		inputPos.moveLeft(1.0);
		outputPos.moveRight(1.0);
		TileEntity tileInput = worldObj.getBlockTileEntity((int)inputPos.x, (int)inputPos.y, (int)inputPos.z);
		if (tileInput instanceof IInventory) {
			IInventory invInput = (IInventory) tileInput;
			for (int slot = 0; slot < invInput.getSizeInventory(); slot++) {

			}
		}
		return false;
	}

}
