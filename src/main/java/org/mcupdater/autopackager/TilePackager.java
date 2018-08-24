package org.mcupdater.autopackager;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.mcupdater.autopackager.helpers.InventoryHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class TilePackager extends TileEntity implements ITickable
{
	public enum Mode {
		HYBRID("autopackager.mode.hybrid"), SMALL("autopackager.mode.small"), LARGE("autopackager.mode.large"), HOLLOW("autopackager.mode.hollow"), UNPACKAGE("autopackager.mode.unpackage"), HYBRID2("autopackager.mode.hybrid2");

		private String message;
		Mode(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}


	}

	private EnergyStorage storage = new EnergyStorage(AutoPackager.energyPerCycle * 100);

	private EnumFacing orientation = EnumFacing.DOWN;

	/**
	 * tickCounter increments every frame, every tickDelay frames it attempts to work.
	 * We default to AutoPackager.delayCycleNormal but will wait for AutoPackager.delayCycleIdle instead if we ever fail
	 * to pack something.
	 */
	private int tickCounter = 0;
	private int tickDelay = AutoPackager.delayCycleNormal;
	protected Mode mode;

	public TilePackager() {
		super();
		mode = Mode.HYBRID;
	}

	@Override
	public void update() {
		if (++tickCounter >= tickDelay) {
			tickCounter = 0;
			if (storage.getEnergyStored() > AutoPackager.energyPerCycle) {
				if (AutoPackager.ludicrousMode) {
					boolean idle = true;
					while (storage.getEnergyStored() > AutoPackager.energyPerCycle && tryCraft()) {
						idle = false;
						if (!AutoPackager.unbalanced) {
							storage.extractEnergy(AutoPackager.energyPerCycle, false);
						}
					}
					if (idle) {
						tickDelay = AutoPackager.delayCycleIdle;
					} else {
						if (AutoPackager.unbalanced) {
							storage.extractEnergy(AutoPackager.energyPerCycle, false);
						}
						tickDelay = AutoPackager.delayCycleNormal;
					}
				}
				if (tryCraft()) {
					storage.extractEnergy(AutoPackager.energyPerCycle, false);
					tickDelay = AutoPackager.delayCycleNormal;
				} else {
					tickDelay = AutoPackager.delayCycleIdle;
				}
			}
		}
	}

	private boolean tryCraft() {
		if (orientation == null) {
			return false;
		}
		// EnumFacing variables perform double-duty reflecting the face of the block in context
		// (i.e. the input and output sides of the AutoPackager as well as the output side of the input inventory and input side of the output inventory)
		EnumFacing inputSide = getInputSide();
		EnumFacing outputSide = getOutputSide();
		BlockPos inputPos = pos.offset(inputSide);
		BlockPos outputPos = pos.offset(outputSide);
		TileEntity tileInput = this.getWorld().getTileEntity(inputPos);
		TileEntity tileOutput = this.getWorld().getTileEntity(outputPos);
		boolean inputValid = tileInput != null && (tileInput.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, outputSide ) || tileInput instanceof ISidedInventory || tileInput instanceof IInventory);
		boolean outputValid = tileOutput != null && (tileOutput.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, inputSide ) || tileOutput instanceof ISidedInventory || tileOutput instanceof IInventory);
        Map<String,SortedSet<Integer>> slotMap = new HashMap<String,SortedSet<Integer>>();
		if (inputValid && outputValid) {
			IItemHandler invInput = InventoryHelper.getWrapper(tileInput, outputSide);
			IItemHandler invOutput = InventoryHelper.getWrapper(tileOutput, inputSide);
			for (int slot = 0; slot < invInput.getSlots(); slot++) {
                if (!invInput.getStackInSlot(slot).equals(ItemStack.EMPTY)) {
	                if (invInput instanceof ISidedInventory && !((ISidedInventory)invInput).canExtractItem(slot, invInput.getStackInSlot(slot), outputSide)) {
		                continue;
	                }
                    if (slotMap.containsKey(invInput.getStackInSlot(slot).getUnlocalizedName() + ":" + invInput.getStackInSlot(slot).getItemDamage())) {
                        slotMap.get(invInput.getStackInSlot(slot).getUnlocalizedName() + ":" + invInput.getStackInSlot(slot).getItemDamage()).add(slot);
                    } else {
                        SortedSet<Integer> slotList = new TreeSet<Integer>();
                        slotList.add(slot);
                        slotMap.put(invInput.getStackInSlot(slot).getUnlocalizedName() + ":" + invInput.getStackInSlot(slot).getItemDamage(), slotList);
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
		                         !(invInput.getStackInSlot(slots.first()).getUnlocalizedName() + ":" + invInput.getStackInSlot(slots.first()).getItemDamage()).equals(entry.getKey()) ||
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

	private boolean craftTiny(IItemHandler invInput, IItemHandler invOutput, int slot) {
		IRecipe result;
		if (invInput.getStackInSlot(slot).getCount() >= 1) {
			ItemStack testStack = invInput.getStackInSlot(slot).copy();
			testStack.setCount(1);
			if (!AutoPackager.single.containsKey(testStack)) {
				InventoryCrafting smallCraft = new InventoryCrafting(new Container() {
					@Override
					public boolean canInteractWith(EntityPlayer entityPlayer) {
						return false;
					}
				}, 2, 2);
				smallCraft.setInventorySlotContents(0, testStack);
				result = CraftingManager.findMatchingRecipe(smallCraft, this.getWorld());
				AutoPackager.single.put(testStack, result);
			} else {
				result = AutoPackager.single.get(testStack);
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
		IRecipe result;
		if (invInput.getStackInSlot(slot).getCount() >= 8) {
			ItemStack testStack = invInput.getStackInSlot(slot).copy();
			testStack.setCount(1);
			if (!AutoPackager.hollow.containsKey(testStack)) {
				InventoryCrafting largeCraft = new InventoryCrafting(new Container()
				{
					@Override
					public boolean canInteractWith(EntityPlayer entityPlayer) {
						return false;
					}
				}, 3, 3);
				for (int craftSlot = 0; craftSlot < 9; craftSlot++) {
					largeCraft.setInventorySlotContents(craftSlot, craftSlot == 4 ? ItemStack.EMPTY : testStack);
				}
				result = CraftingManager.findMatchingRecipe(largeCraft, this.getWorld());
				AutoPackager.hollow.put(testStack, result);
			} else {
				result = AutoPackager.hollow.get(testStack);
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
		IRecipe result;
		if (invInput.getStackInSlot(slot).getCount() >= 9) {
	        ItemStack testStack = invInput.getStackInSlot(slot).copy();
	        testStack.setCount(1);
		    if (!AutoPackager.large.containsKey(testStack)) {
			    InventoryCrafting largeCraft = new InventoryCrafting(new Container()
			    {
				    @Override
				    public boolean canInteractWith(EntityPlayer entityPlayer) {
					    return false;
				    }
			    }, 3, 3);
			    for (int craftSlot = 0; craftSlot < 9; craftSlot++) {
				    largeCraft.setInventorySlotContents(craftSlot, testStack);
			    }
			    result = CraftingManager.findMatchingRecipe(largeCraft, this.getWorld());
			    AutoPackager.large.put(testStack, result);
		    } else {
			    result = AutoPackager.large.get(testStack);
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
		IRecipe result;
		if (invInput.getStackInSlot(slot).getCount() >= 4) {
			ItemStack testStack = invInput.getStackInSlot(slot).copy();
			testStack.setCount(1);
			if (!AutoPackager.small.containsKey(testStack)) {
				InventoryCrafting smallCraft = new InventoryCrafting(new Container()
				{
					@Override
					public boolean canInteractWith(EntityPlayer entityPlayer) {
						return false;
					}
				}, 2, 2);
				for (int craftSlot = 0; craftSlot < 4; craftSlot++) {
					smallCraft.setInventorySlotContents(craftSlot, testStack);
				}
				result = CraftingManager.findMatchingRecipe(smallCraft, this.getWorld());
				AutoPackager.small.put(testStack, result);
			} else {
				result = AutoPackager.small.get(testStack);
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

	private EnumFacing getInputSide() {
		switch (this.orientation) {
			case NORTH:
				return EnumFacing.EAST;
			case EAST:
				return EnumFacing.SOUTH;
			case SOUTH:
				return EnumFacing.WEST;
			case WEST:
				return EnumFacing.NORTH;
		}
		return EnumFacing.NORTH;
	}

	private EnumFacing getOutputSide() {
		switch (this.orientation) {
			case NORTH:
				return EnumFacing.WEST;
			case EAST:
				return EnumFacing.NORTH;
			case SOUTH:
				return EnumFacing.EAST;
			case WEST:
				return EnumFacing.SOUTH;
		}
		return EnumFacing.SOUTH;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		tagCompound.setInteger("mode", mode.ordinal());
		tagCompound.setString("orientation", orientation.getName());
		tagCompound.setInteger("energy", storage.getEnergyStored());
		return tagCompound;
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		System.out.println("readFromNBT");
		this.mode = Mode.values()[tagCompound.getInteger("mode")];
		this.orientation = EnumFacing.byName(tagCompound.getString("orientation"));
		if (tagCompound.hasKey("energy")) {
			storage = new EnergyStorage(AutoPackager.energyPerCycle * 100,AutoPackager.energyPerCycle * 100,AutoPackager.energyPerCycle * 100,tagCompound.getInteger("energy"));
			//storage.receiveEnergy(tagCompound.getInteger("energy"),false);
		}
	}

	public void setOrientation(EnumFacing orientation) {
		this.orientation = orientation;
	}

	public void cycleMode(EntityPlayer player) {
		mode = Mode.values()[(mode.ordinal() + 1) % Mode.values().length];
		if (!this.getWorld().isRemote) {
			player.sendMessage(new TextComponentTranslation(new TextComponentTranslation("autopackager.mode.current").getUnformattedComponentText() + " " + new TextComponentTranslation(mode.getMessage()).getUnformattedComponentText()));
		}
		this.markDirty();
	}

	public void checkMode(EntityPlayer player) {
		if (!this.getWorld().isRemote) {
			player.sendMessage(new TextComponentTranslation(new TextComponentTranslation("autopackager.mode.current").getUnformattedComponentText() + " " + new TextComponentTranslation(mode.getMessage()).getUnformattedComponentText()));
		}
	}

	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return CapabilityEnergy.ENERGY.cast(storage);
		}

		return super.getCapability(capability, facing);
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
		return capability == CapabilityEnergy.ENERGY || super.hasCapability(capability, facing);
	}

	public Mode getMode() {
		return this.mode;
	}
}
