package com.mcupdater.autopackager.tile;

import com.mcupdater.autopackager.AutoPackager;
import com.mcupdater.autopackager.setup.Config;
import com.mcupdater.autopackager.setup.CraftingCache;
import com.mcupdater.mculib.capabilities.TileEntityPowered;
import com.mcupdater.mculib.helpers.InventoryHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.*;

import static com.mcupdater.autopackager.setup.Registration.PACKAGERBLOCK_TILE;

public class TilePackager extends TileEntityPowered {

    public enum Mode {
        HYBRID("autopackager.mode.hybrid"), SMALL("autopackager.mode.small"), LARGE("autopackager.mode.large"), HOLLOW("autopackager.mode.hollow"), UNPACKAGE("autopackager.mode.unpackage"), HYBRID2("autopackager.mode.hybrid2"), CROSS("autopackager.mode.cross"), STAIR("autopackager.mode.stair"), SLAB("autopackager.mode.slab"), WALL("autopackager.mode.wall");

        private String message;
        Mode(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * tickCounter increments every frame, every tickDelay frames it attempts to work.
     * We default to AutoPackager.delayCycleNormal but will wait for AutoPackager.delayCycleIdle instead if we ever fail
     * to pack something.
     */
    private int tickCounter = 0;
    private int tickDelay = Config.DELAY_NORMAL.get();
    private int idleCycles = 0;
    protected Mode mode;
    public IntReferenceHolder modeData = new IntReferenceHolder() {
        @Override
        public int get() {
            return TilePackager.this.getModeInternal();
        }

        @Override
        public void set(int mode) {
            TilePackager.this.setModeInternal(Mode.values()[mode]);
        }
    };

    public void setModeInternal(Mode newMode) {
        this.mode = newMode;
        this.markDirty();
    }

    private int getModeInternal() {
        return this.mode.ordinal();
    }

    public TilePackager() {
        super(PACKAGERBLOCK_TILE.get(), Math.max(Config.ENERGY_PER_CYCLE.get() * 100,100000), Integer.MAX_VALUE);
        mode = Mode.HYBRID;
    }

    public void changeMode() {
        mode = Mode.values()[(mode.ordinal() + 1) % Mode.values().length];
        this.markDirty();
    }

    @Override
    public void tick() {
        if (!this.world.isRemote) {
            if (++tickCounter >= tickDelay) {
                tickCounter = 0;
                if (!isDisabled() && energyStorage.getEnergyStored() > Config.ENERGY_PER_CYCLE.get()) {
                    if (Config.LUDICROUS.get()) {
                        boolean idle = true;
                        while (energyStorage.getEnergyStored() > Config.ENERGY_PER_CYCLE.get() && tryCraft()) {
                            idle = false;
                            idleCycles = 0;
                            if (!Config.UNBALANCED.get()) {
                                energyStorage.extractEnergy(Config.ENERGY_PER_CYCLE.get(), false);
                            }
                        }
                        if (idle) {
                            idleCycles++;
                            tickDelay = Config.DELAY_IDLE.get() * (Config.DEEP_SLEEP.get() ? Math.min(idleCycles, Config.MAX_DEEP_SLEEP.get()) : 1);
                        } else {
                            if (Config.UNBALANCED.get()) {
                                energyStorage.extractEnergy(Config.ENERGY_PER_CYCLE.get(), false);
                            }
                            tickDelay = Config.DELAY_NORMAL.get();
                        }
                    }
                    if (tryCraft()) {
                        energyStorage.extractEnergy(Config.ENERGY_PER_CYCLE.get(), false);
                        idleCycles = 0;
                        tickDelay = Config.DELAY_NORMAL.get();
                    } else {
                        idleCycles++;
                        tickDelay = Config.DELAY_IDLE.get() * (Config.DEEP_SLEEP.get() ? Math.min(idleCycles, Config.MAX_DEEP_SLEEP.get()) : 1);
                    }
                }
            }
        }
        super.func_73660_a();
    }

    private boolean tryCraft() {
        // Direction variables perform double-duty reflecting the face of the block in context
        // (i.e. the input and output sides of the AutoPackager as well as the output side of the input inventory and input side of the output inventory)
        Direction inputSide =  getInputSide();
        Direction outputSide = getOutputSide();
        BlockPos inputPos = pos.offset(inputSide);
        BlockPos outputPos = pos.offset(outputSide);
        TileEntity tileInput = this.getWorld().getTileEntity(inputPos);
        TileEntity tileOutput = this.getWorld().getTileEntity(outputPos);
        boolean inputValid = tileInput != null && (tileInput.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, outputSide).isPresent() || tileInput instanceof ISidedInventory || tileInput instanceof IInventory);
        boolean outputValid = tileOutput != null && (tileOutput.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, inputSide).isPresent() || tileOutput instanceof ISidedInventory || tileOutput instanceof IInventory);
        Map<String, SortedSet<Integer>> slotMap = new HashMap<String,SortedSet<Integer>>();
        if (inputValid && outputValid) {
            IItemHandler invInput = InventoryHelper.getWrapper(tileInput, outputSide);
            IItemHandler invOutput = InventoryHelper.getWrapper(tileOutput, inputSide);
            for (int slot = 0; slot < invInput.getSlots(); slot++) {
                if (!invInput.getStackInSlot(slot).equals(ItemStack.EMPTY)) {
                    if (invInput instanceof ISidedInventory && !((ISidedInventory)invInput).canExtractItem(slot, invInput.getStackInSlot(slot), outputSide)) {
                        continue;
                    }
                    if (slotMap.containsKey(invInput.getStackInSlot(slot).getTranslationKey())) {
                        slotMap.get(invInput.getStackInSlot(slot).getTranslationKey()).add(slot);
                    } else {
                        SortedSet<Integer> slotList = new TreeSet<Integer>();
                        slotList.add(slot);
                        slotMap.put(invInput.getStackInSlot(slot).getTranslationKey(), slotList);
                    }
                    boolean result;
                    switch (mode) {
                        case HYBRID:
                            result = (craftSmall(invInput, invOutput, slot) || craftLarge(invInput, invOutput, slot));
                            break;
                        case SMALL:
                            result = craftSmall(invInput, invOutput, slot);
                            break;
                        case LARGE:
                            result = craftLarge(invInput, invOutput, slot);
                            break;
                        case HOLLOW:
                            result = craftHollow(invInput, invOutput, slot);
                            break;
                        case HYBRID2:
                            result = (craftLarge(invInput, invOutput, slot) || craftSmall(invInput, invOutput, slot));
                            break;
                        case UNPACKAGE:
                            result = craftTiny(invInput, invOutput, slot);
                            break;
                        case CROSS:
                            result = craftCross(invInput, invOutput, slot);
                            break;
                        case STAIR:
                            result = craftStair(invInput, invOutput, slot);
                            break;
                        case SLAB:
                            result = craftSlab(invInput, invOutput, slot);
                            break;
                        case WALL:
                            result = craftWall(invInput, invOutput, slot);
                            break;
                        default:
                            result = false;
                    }
                    if (result) return true;
                }
            }
            for (Map.Entry<String,SortedSet<Integer>> entry : slotMap.entrySet()) {
                if (entry.getValue().size() > 1) {
                    SortedSet<Integer> slots = entry.getValue();
                    while (slots.size() > 1) {
                        if (invInput.getStackInSlot(slots.first()).equals(ItemStack.EMPTY) ||
                                !(invInput.getStackInSlot(slots.first()).getTranslationKey()).equals(entry.getKey()) ||
                                invInput.getStackInSlot(slots.first()).getCount() >= invInput.getStackInSlot(slots.first()).getMaxStackSize()) {
                            slots.remove(slots.first());
                            continue;
                        }
                        if (invInput.getStackInSlot(slots.last()).equals(ItemStack.EMPTY) ||
                                !(invInput.getStackInSlot(slots.last()).isItemEqual(invInput.getStackInSlot(slots.first()))) ||
                                !ItemStack.areItemStackTagsEqual(invInput.getStackInSlot(slots.first()), invInput.getStackInSlot(slots.last()))) {
                            slots.remove(slots.last());
                            continue;
                        }
                        if (invInput.getStackInSlot(slots.first()).getCount() + invInput.getStackInSlot(slots.last()).getCount() <= invInput.getStackInSlot(slots.first()).getMaxStackSize()) {
                            invInput.insertItem(slots.first(), invInput.extractItem(slots.last(),invInput.getStackInSlot(slots.last()).getCount(),false),false);
                        } else {
                            int spaceRemain = invInput.getStackInSlot(slots.first()).getMaxStackSize() - invInput.getStackInSlot(slots.first()).getCount();
                            invInput.insertItem(slots.first(), invInput.extractItem(slots.last(), spaceRemain, false), false);
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isDisabled() {
        for (Direction direction : Direction.values())
        {
            if (this.world.isSidePowered(pos.offset(direction), direction))
            {
                return true;
            }
        }

        if (this.world.isSidePowered(pos, Direction.DOWN))
        {
            return true;
        }
        else
        {
            BlockPos blockpos = pos.up();

            for (Direction direction : Direction.values())
            {
                if (direction != Direction.DOWN && this.world.isSidePowered(blockpos.offset(direction), direction))
                {
                    return true;
                }
            }

            return false;
        }
    }

    private Direction getInputSide() {
        if (this.world != null) {
            switch (this.world.getBlockState(this.pos).get(BlockStateProperties.FACING)) {
                case NORTH:
                    return Direction.EAST;
                case EAST:
                    return Direction.SOUTH;
                case SOUTH:
                    return Direction.WEST;
                case WEST:
                    return Direction.NORTH;
            }
        }
        return Direction.NORTH;
    }

    private Direction getOutputSide() {
        if (this.world != null) {
            switch (this.world.getBlockState(this.pos).get(BlockStateProperties.FACING)) {
                case NORTH:
                    return Direction.WEST;
                case EAST:
                    return Direction.NORTH;
                case SOUTH:
                    return Direction.EAST;
                case WEST:
                    return Direction.SOUTH;
            }
        }
        return Direction.SOUTH;
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.putInt("mode", this.getModeInternal());
        return compound;
    }

    @Override
    public void read(BlockState blockState, CompoundNBT compound) {
        super.read(blockState, compound);
        this.setModeInternal(Mode.values()[compound.getInt("mode")]);
    }

    private boolean craftTiny(IItemHandler invInput, IItemHandler invOutput, int slot) {
        ICraftingRecipe result = null;
        if (invInput.getStackInSlot(slot).getCount() >= 1) {
            ItemStack testStack = invInput.getStackInSlot(slot).copy();
            testStack.setCount(1);
            if (!CraftingCache.SINGLE.containsKey(testStack)) {
                CraftingInventory smallCraft = new CraftingInventory(new Container(ContainerType.CRAFTING, -1) {
                    @Override
                    public boolean canInteractWith(PlayerEntity entityPlayer) {
                        return false;
                    }
                }, 2, 2);
                smallCraft.setInventorySlotContents(0, testStack);
                Optional<ICraftingRecipe> recipe = world.getServer().getRecipeManager().getRecipe(IRecipeType.CRAFTING, smallCraft, this.getWorld());
                if (recipe.isPresent()) {
                    result = recipe.get();
                }
                CraftingCache.SINGLE.put(testStack, result);
            } else {
                result = CraftingCache.SINGLE.get(testStack);
            }
            if (result != null) {
                ItemStack recipeOutput = result.getRecipeOutput().copy();
                if (InventoryHelper.canStackFitInInventory(invOutput, recipeOutput)) {
                    invInput.extractItem(slot, 1, false);
                    InventoryHelper.insertItemStackIntoInventory(invOutput, recipeOutput);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean craftHollow(IItemHandler invInput, IItemHandler invOutput, int slot) {
        ICraftingRecipe result = null;
        if (invInput.getStackInSlot(slot).getCount() >= 8) {
            ItemStack testStack = invInput.getStackInSlot(slot).copy();
            testStack.setCount(1);
            if (!CraftingCache.HOLLOW.containsKey(testStack)) {
                CraftingInventory largeCraft = new CraftingInventory(new Container(ContainerType.CRAFTING, -1)
                {
                    @Override
                    public boolean canInteractWith(PlayerEntity entityPlayer) {
                        return false;
                    }
                }, 3, 3);
                for (int craftSlot = 0; craftSlot < 9; craftSlot++) {
                    largeCraft.setInventorySlotContents(craftSlot, craftSlot == 4 ? ItemStack.EMPTY : testStack);
                }
                Optional<ICraftingRecipe> recipe = world.getServer().getRecipeManager().getRecipe(IRecipeType.CRAFTING, largeCraft, this.getWorld());
                if (recipe.isPresent()) {
                    result = recipe.get();
                }
                CraftingCache.HOLLOW.put(testStack, result);
            } else {
                result = CraftingCache.HOLLOW.get(testStack);
            }
            if (result != null) {
                ItemStack recipeOutput = result.getRecipeOutput().copy();
                if (InventoryHelper.canStackFitInInventory(invOutput, recipeOutput)) {
                    invInput.extractItem(slot, 8, false);
                    InventoryHelper.insertItemStackIntoInventory(invOutput, recipeOutput);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean craftLarge(IItemHandler invInput, IItemHandler invOutput, int slot) {
        ICraftingRecipe result = null;
        if (invInput.getStackInSlot(slot).getCount() >= 9) {
            ItemStack testStack = invInput.getStackInSlot(slot).copy();
            testStack.setCount(1);
            if (!CraftingCache.LARGE.containsKey(testStack)) {
                CraftingInventory largeCraft = new CraftingInventory(new Container(ContainerType.CRAFTING, -1)
                {
                    @Override
                    public boolean canInteractWith(PlayerEntity entityPlayer) {
                        return false;
                    }
                }, 3, 3);
                for (int craftSlot = 0; craftSlot < 9; craftSlot++) {
                    largeCraft.setInventorySlotContents(craftSlot, testStack);
                }
                Optional<ICraftingRecipe> recipe = world.getServer().getRecipeManager().getRecipe(IRecipeType.CRAFTING, largeCraft, this.getWorld());
                if (recipe.isPresent()) {
                    result = recipe.get();
                }
                CraftingCache.LARGE.put(testStack, result);
            } else {
                result = CraftingCache.LARGE.get(testStack);
            }
            if (result != null) {
                ItemStack recipeOutput = result.getRecipeOutput().copy();
                if (InventoryHelper.canStackFitInInventory(invOutput, recipeOutput)) {
                    invInput.extractItem(slot, 9, false);
                    InventoryHelper.insertItemStackIntoInventory(invOutput, recipeOutput);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean craftSmall(IItemHandler invInput, IItemHandler invOutput, int slot) {
        ICraftingRecipe result = null;
        if (invInput.getStackInSlot(slot).getCount() >= 4) {
            ItemStack testStack = invInput.getStackInSlot(slot).copy();
            testStack.setCount(1);
            if (!CraftingCache.SMALL.containsKey(testStack)) {
                CraftingInventory smallCraft = new CraftingInventory(new Container(ContainerType.CRAFTING, -1)
                {
                    @Override
                    public boolean canInteractWith(PlayerEntity entityPlayer) {
                        return false;
                    }
                }, 2, 2);
                for (int craftSlot = 0; craftSlot < 4; craftSlot++) {
                    smallCraft.setInventorySlotContents(craftSlot, testStack);
                }
                Optional<ICraftingRecipe> recipe = world.getServer().getRecipeManager().getRecipe(IRecipeType.CRAFTING, smallCraft, this.getWorld());
                if (recipe.isPresent()) {
                    result = recipe.get();
                }
                CraftingCache.SMALL.put(testStack, result);
            } else {
                result = CraftingCache.SMALL.get(testStack);
            }
            if (result != null) {
                ItemStack recipeOutput = result.getRecipeOutput().copy();
                if (InventoryHelper.canStackFitInInventory(invOutput, recipeOutput)) {
                    invInput.extractItem(slot, 4, false);
                    InventoryHelper.insertItemStackIntoInventory(invOutput, recipeOutput);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean craftCross(IItemHandler invInput, IItemHandler invOutput, int slot) {
        ICraftingRecipe result = null;
        if (invInput.getStackInSlot(slot).getCount() >= 5) {
            ItemStack testStack = invInput.getStackInSlot(slot).copy();
            testStack.setCount(1);
            if (!CraftingCache.CROSS.containsKey(testStack)) {
                CraftingInventory largeCraft = new CraftingInventory(new Container(ContainerType.CRAFTING, -1)
                {
                    @Override
                    public boolean canInteractWith(PlayerEntity entityPlayer) {
                        return false;
                    }
                }, 3, 3);
                for (int craftSlot = 0; craftSlot < 9; craftSlot++) {
                    largeCraft.setInventorySlotContents(craftSlot, (craftSlot == 0 || craftSlot == 2 || craftSlot == 6 || craftSlot == 8) ? ItemStack.EMPTY : testStack);
                }
                Optional<ICraftingRecipe> recipe = world.getServer().getRecipeManager().getRecipe(IRecipeType.CRAFTING, largeCraft, this.getWorld());
                if (recipe.isPresent()) {
                    result = recipe.get();
                }
                CraftingCache.CROSS.put(testStack, result);
            } else {
                result = CraftingCache.CROSS.get(testStack);
            }
            if (result != null) {
                ItemStack recipeOutput = result.getRecipeOutput().copy();
                if (InventoryHelper.canStackFitInInventory(invOutput, recipeOutput)) {
                    invInput.extractItem(slot, 5, false);
                    InventoryHelper.insertItemStackIntoInventory(invOutput, recipeOutput);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean craftStair(IItemHandler invInput, IItemHandler invOutput, int slot) {
        ICraftingRecipe result = null;
        if (invInput.getStackInSlot(slot).getCount() >= 6) {
            ItemStack testStack = invInput.getStackInSlot(slot).copy();
            testStack.setCount(1);
            if (!CraftingCache.STAIR.containsKey(testStack)) {
                CraftingInventory largeCraft = new CraftingInventory(new Container(ContainerType.CRAFTING, -1)
                {
                    @Override
                    public boolean canInteractWith(PlayerEntity entityPlayer) {
                        return false;
                    }
                }, 3, 3);
                for (int craftSlot = 0; craftSlot < 9; craftSlot++) {
                    largeCraft.setInventorySlotContents(craftSlot, (craftSlot == 1 || craftSlot == 2 || craftSlot == 5) ? ItemStack.EMPTY : testStack);
                }
                Optional<ICraftingRecipe> recipe = world.getServer().getRecipeManager().getRecipe(IRecipeType.CRAFTING, largeCraft, this.getWorld());
                if (recipe.isPresent()) {
                    result = recipe.get();
                }
                CraftingCache.STAIR.put(testStack, result);
            } else {
                result = CraftingCache.STAIR.get(testStack);
            }
            if (result != null) {
                ItemStack recipeOutput = result.getRecipeOutput().copy();
                if (InventoryHelper.canStackFitInInventory(invOutput, recipeOutput)) {
                    invInput.extractItem(slot, 6, false);
                    InventoryHelper.insertItemStackIntoInventory(invOutput, recipeOutput);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean craftSlab(IItemHandler invInput, IItemHandler invOutput, int slot) {
        ICraftingRecipe result = null;
        if (invInput.getStackInSlot(slot).getCount() >= 3) {
            ItemStack testStack = invInput.getStackInSlot(slot).copy();
            testStack.setCount(1);
            if (!CraftingCache.SLAB.containsKey(testStack)) {
                CraftingInventory largeCraft = new CraftingInventory(new Container(ContainerType.CRAFTING, -1)
                {
                    @Override
                    public boolean canInteractWith(PlayerEntity entityPlayer) {
                        return false;
                    }
                }, 3, 3);
                for (int craftSlot = 0; craftSlot < 9; craftSlot++) {
                    largeCraft.setInventorySlotContents(craftSlot, craftSlot < 6 ? ItemStack.EMPTY : testStack);
                }
                Optional<ICraftingRecipe> recipe = world.getServer().getRecipeManager().getRecipe(IRecipeType.CRAFTING, largeCraft, this.getWorld());
                if (recipe.isPresent()) {
                    result = recipe.get();
                }
                CraftingCache.SLAB.put(testStack, result);
            } else {
                result = CraftingCache.SLAB.get(testStack);
            }
            if (result != null) {
                ItemStack recipeOutput = result.getRecipeOutput().copy();
                if (InventoryHelper.canStackFitInInventory(invOutput, recipeOutput)) {
                    invInput.extractItem(slot, 3, false);
                    InventoryHelper.insertItemStackIntoInventory(invOutput, recipeOutput);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean craftWall(IItemHandler invInput, IItemHandler invOutput, int slot) {
        ICraftingRecipe result = null;
        if (invInput.getStackInSlot(slot).getCount() >= 6) {
            ItemStack testStack = invInput.getStackInSlot(slot).copy();
            testStack.setCount(1);
            if (!CraftingCache.WALL.containsKey(testStack)) {
                CraftingInventory largeCraft = new CraftingInventory(new Container(ContainerType.CRAFTING, -1)
                {
                    @Override
                    public boolean canInteractWith(PlayerEntity entityPlayer) {
                        return false;
                    }
                }, 3, 3);
                for (int craftSlot = 0; craftSlot < 9; craftSlot++) {
                    largeCraft.setInventorySlotContents(craftSlot, craftSlot < 3 ? ItemStack.EMPTY : testStack);
                }
                Optional<ICraftingRecipe> recipe = world.getServer().getRecipeManager().getRecipe(IRecipeType.CRAFTING, largeCraft, this.getWorld());
                if (recipe.isPresent()) {
                    result = recipe.get();
                }
                CraftingCache.WALL.put(testStack, result);
            } else {
                result = CraftingCache.WALL.get(testStack);
            }
            if (result != null) {
                ItemStack recipeOutput = result.getRecipeOutput().copy();
                if (InventoryHelper.canStackFitInInventory(invOutput, recipeOutput)) {
                    invInput.extractItem(slot, 6, false);
                    InventoryHelper.insertItemStackIntoInventory(invOutput, recipeOutput);
                    return true;
                }
            }
        }
        return false;
    }

}
