package com.mcupdater.autopackager.tile;

import com.mcupdater.autopackager.setup.Registration;
import com.mcupdater.mculib.capabilities.ContainerPowered;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class ContainerPackager extends ContainerPowered {

    private final TilePackager localTileEntity;
    private final PlayerEntity playerEntity;
    private final IItemHandler playerInventory;
    public final IntReferenceHolder modeData;

    public ContainerPackager(int windowId, World world, BlockPos pos, PlayerInventory playerInventory, PlayerEntity player, IntReferenceHolder modeData) {
        super(Registration.PACKAGERBLOCK_CONTAINER.get(), windowId);
        localTileEntity = world.getTileEntity(pos) instanceof TilePackager ? (TilePackager) world.getTileEntity(pos) : null;
        tileEntity = localTileEntity;
        this.playerEntity = player;
        this.playerInventory = new InvWrapper(playerInventory);
        this.modeData = modeData;

        layoutPlayerInventorySlots(8,84);
        trackPower();
        trackInt(this.modeData);
    }

    private void layoutPlayerInventorySlots(int leftCol, int topRow) {
        // Player inventory
        addSlotBox(playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);

        // Hotbar
        topRow += 58;
        addSlotRange(playerInventory, 0, leftCol, topRow, 9, 18);
    }

    private int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0 ; i < amount ; i++) {
            addSlot(new SlotItemHandler(handler, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    private int addSlotBox(IItemHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0 ; j < verAmount ; j++) {
            index = addSlotRange(handler, index, x, y, horAmount, dx);
            y += dy;
        }
        return index;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return isWithinUsableDistance(IWorldPosCallable.of(localTileEntity.getWorld(), localTileEntity.getPos()), playerEntity, Registration.PACKAGERBLOCK.get());
    }

    public TilePackager getTileEntity() {
        return this.localTileEntity;
    }
}
