package com.mcupdater.autopackager.block;

import com.mcupdater.autopackager.setup.Registration;
import com.mcupdater.mculib.block.IConfigurableMenu;
import com.mcupdater.mculib.capabilities.PowerTrackingMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.util.Map;

public class PackagerMenu extends PowerTrackingMenu implements IConfigurableMenu {

    private final PackagerEntity localBlockEntity;
    private final Player player;
    private final IItemHandler playerInventory;
    public final DataSlot modeData;
    private final Map<Direction, Component> adjacentNames;

    public PackagerMenu(int windowId, Level level, BlockPos pos, Inventory inventory, Player player, DataSlot modeData, Map<Direction, Component> adjacentNames) {
        super(Registration.PACKAGERBLOCK_MENU.get(), windowId);
        this.adjacentNames = adjacentNames;
        localBlockEntity = level.getBlockEntity(pos) instanceof PackagerEntity ? (PackagerEntity) level.getBlockEntity(pos) : null;
        this.tileEntity = localBlockEntity;
        this.player = player;
        this.playerInventory = new InvWrapper(inventory);
        this.modeData = modeData;

        layoutPlayerInventorySlots(8,84);
        trackPower();
        addDataSlot(this.modeData);
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
    public boolean stillValid(Player playerIn) {
        return stillValid(ContainerLevelAccess.create(localBlockEntity.getLevel(), localBlockEntity.getBlockPos()), player, Registration.PACKAGERBLOCK.get());
    }

    @Override
    public PackagerEntity getBlockEntity() {
        return this.localBlockEntity;
    }

    @Override
    public Component getSideName(Direction direction) {
        return this.adjacentNames.get(direction);
    }
}
