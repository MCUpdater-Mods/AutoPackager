package org.mcupdater.autopackager;

import cofh.api.energy.TileEnergyHandler;
import com.dynious.refinedrelocation.api.APIUtils;
import com.dynious.refinedrelocation.api.tileentity.ISortingMember;
import com.dynious.refinedrelocation.api.tileentity.handlers.ISortingMemberHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

@Optional.Interface(iface = "com.dynious.refinedrelocation.api.tileentity.ISortingMember", modid = "RefinedRelocation")
public class TilePackager extends TileEnergyHandler implements ITickable, ISortingMember
{
	protected enum Mode {
		HYBRID("autopackager.mode.hybrid"), SMALL("autopackager.mode.small"), LARGE("autopackager.mode.large"), HOLLOW("autopackager.mode.hollow"), UNPACKAGE("autopackager.mode.unpackage");

		private String message;
		Mode(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}
	}

	private boolean isFirstTick = true;
	private Object sortingHandler;

	private EnumFacing orientation;

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
		if (isFirstTick && Loader.isModLoaded("RefinedRelocation")) {
			new Runnable() {
				@Override
				public void run() {
					getHandler().onTileAdded();
				}
			}.run();
			isFirstTick = false;
		}
		if (++tickCounter >= tickDelay) {
			tickCounter = 0;
			if (storage.getEnergyStored() > AutoPackager.energyPerCycle) {
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
/*
		if (orientation == null) {
			return false;
		}
		Position inputPos = new Position(this.getPos(), orientation);
		Position outputPos = new Position(this.getPos(), orientation);
		inputPos.moveLeft(1.0);
		outputPos.moveRight(1.0);
		TileEntity tileInput = worldObj.getTileEntity((int)inputPos.x, (int)inputPos.y, (int)inputPos.z);
		TileEntity tileOutput = worldObj.getTileEntity((int)outputPos.x, (int)outputPos.y, (int)outputPos.z);
        Map<String,SortedSet<Integer>> slotMap = new HashMap<String,SortedSet<Integer>>();
		if (tileInput instanceof IInventory && tileOutput instanceof IInventory) {
			IInventory invInput = (IInventory) tileInput;
			IInventory invOutput = (IInventory) tileOutput;
			for (int slot = 0; slot < invInput.getSizeInventory(); slot++) {
                if (invInput.getStackInSlot(slot) != null) {
	                if (invInput instanceof ISidedInventory && !((ISidedInventory)invInput).canExtractItem(slot, invInput.getStackInSlot(slot), ForgeDirection.DOWN.ordinal())) {
		                continue;
	                }
                    if (slotMap.containsKey(invInput.getStackInSlot(slot).getUnlocalizedName() + ":" + invInput.getStackInSlot(slot).getItemDamage())) {
                        slotMap.get(invInput.getStackInSlot(slot).getUnlocalizedName() + ":" + invInput.getStackInSlot(slot).getItemDamage()).add(slot);
                    } else {
                        SortedSet<Integer> slotList = new TreeSet<Integer>();
                        slotList.add(slot);
                        slotMap.put(invInput.getStackInSlot(slot).getUnlocalizedName() + ":" + invInput.getStackInSlot(slot).getItemDamage(), slotList);
                    }
	                ItemStack result;
                    if ((mode == Mode.HYBRID || mode == Mode.SMALL) && invInput.getStackInSlot(slot).stackSize >= 4) {
	                    ItemStack testStack = invInput.getStackInSlot(slot).copy();
	                    testStack.stackSize = 1;
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
		                    result = CraftingManager.getInstance().findMatchingRecipe(smallCraft, worldObj);
		                    AutoPackager.small.put(testStack, result);
	                    } else {
		                    result = AutoPackager.small.get(testStack);
	                    }
                        if (result != null) {
                            testStack = InventoryHelper.simulateInsertItemStackIntoInventory(invOutput, result, 1);
                            if (testStack == null) {
                                invInput.decrStackSize(slot, 4);
                                InventoryHelper.insertItemStackIntoInventory(invOutput, result, 1);
                                return true;
                            }
                        }
                    }
                    if ((mode == Mode.HYBRID || mode == Mode.LARGE) && invInput.getStackInSlot(slot).stackSize >= 9) {
                        ItemStack testStack = invInput.getStackInSlot(slot).copy();
                        testStack.stackSize = 1;
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
		                    result = CraftingManager.getInstance().findMatchingRecipe(largeCraft, worldObj);
		                    AutoPackager.large.put(testStack, result);
	                    } else {
		                    result = AutoPackager.large.get(testStack);
	                    }
                        if (result != null) {
                            testStack = InventoryHelper.simulateInsertItemStackIntoInventory(invOutput, result, 1);
                            if (testStack == null) {
                                invInput.decrStackSize(slot, 9);
                                InventoryHelper.insertItemStackIntoInventory(invOutput, result, 1);
                                return true;
                            }
                        }
                    }
	                if (mode == Mode.HOLLOW && invInput.getStackInSlot(slot).stackSize >= 8) {
		                ItemStack testStack = invInput.getStackInSlot(slot).copy();
		                testStack.stackSize = 1;
		                if (!AutoPackager.hollow.containsKey(testStack)) {
			                InventoryCrafting largeCraft = new InventoryCrafting(new Container()
			                {
				                @Override
				                public boolean canInteractWith(EntityPlayer entityPlayer) {
					                return false;
				                }
			                }, 3, 3);
			                for (int craftSlot = 0; craftSlot < 9; craftSlot++) {
				                largeCraft.setInventorySlotContents(craftSlot, craftSlot == 4 ? null : testStack);
			                }
			                result = CraftingManager.getInstance().findMatchingRecipe(largeCraft, worldObj);
			                AutoPackager.hollow.put(testStack, result);
		                } else {
			                result = AutoPackager.hollow.get(testStack);
		                }
		                if (result != null) {
			                testStack = InventoryHelper.simulateInsertItemStackIntoInventory(invOutput, result, 1);
			                if (testStack == null) {
				                invInput.decrStackSize(slot, 8);
				                InventoryHelper.insertItemStackIntoInventory(invOutput, result, 1);
				                return true;
			                }
		                }
	                }
	                if (mode == Mode.UNPACKAGE && invInput.getStackInSlot(slot).stackSize >= 1) {
		                ItemStack testStack = invInput.getStackInSlot(slot).copy();
		                testStack.stackSize = 1;
		                if (!AutoPackager.single.containsKey(testStack)) {
			                InventoryCrafting smallCraft = new InventoryCrafting(new Container()
			                {
				                @Override
				                public boolean canInteractWith(EntityPlayer entityPlayer) {
					                return false;
				                }
			                }, 2, 2);
			                smallCraft.setInventorySlotContents(0, testStack);
			                result = CraftingManager.getInstance().findMatchingRecipe(smallCraft, worldObj);
			                AutoPackager.single.put(testStack, result);
		                } else {
			                result = AutoPackager.single.get(testStack);
		                }
		                if (result != null) {
			                testStack = InventoryHelper.simulateInsertItemStackIntoInventory(invOutput, result, 1);
			                if (testStack == null) {
				                invInput.decrStackSize(slot, 1);
				                InventoryHelper.insertItemStackIntoInventory(invOutput, result, 1);
				                return true;
			                }
		                }
	                }
                }
			}
            for (Map.Entry<String,SortedSet<Integer>> entry : slotMap.entrySet()) {
                 if (entry.getValue().size() > 1) {
                     SortedSet<Integer> slots = entry.getValue();
                     while (slots.size() > 1) {
                         if (invInput.getStackInSlot(slots.first()) == null || !(invInput.getStackInSlot(slots.first()).getUnlocalizedName() + ":" + invInput.getStackInSlot(slots.first()).getItemDamage()).equals(entry.getKey()) || invInput.getStackInSlot(slots.first()).stackSize >= invInput.getStackInSlot(slots.first()).getMaxStackSize()) {
                             slots.remove(slots.first());
                             continue;
                         }
                         if (invInput.getStackInSlot(slots.last()) == null || !(invInput.getStackInSlot(slots.last()).isItemEqual(invInput.getStackInSlot(slots.first()))) || !ItemStack.areItemStackTagsEqual(invInput.getStackInSlot(slots.first()), invInput.getStackInSlot(slots.last()))) {
                             slots.remove(slots.last());
                             continue;
                         }
                         if (invInput.getStackInSlot(slots.first()).stackSize + invInput.getStackInSlot(slots.last()).stackSize <= invInput.getStackInSlot(slots.first()).getMaxStackSize()) {
                             invInput.getStackInSlot(slots.first()).stackSize += invInput.getStackInSlot(slots.last()).stackSize;
                             invInput.setInventorySlotContents(slots.last(), null);
                         } else {
                             int spaceRemain = invInput.getStackInSlot(slots.first()).getMaxStackSize() - invInput.getStackInSlot(slots.first()).stackSize;
                             invInput.getStackInSlot(slots.first()).stackSize += spaceRemain;
                             invInput.decrStackSize(slots.last(), spaceRemain);
                         }
                     }
                 }
            }
		}
		*/
		return false;
	}

	@Override
	public void writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		tagCompound.setInteger("mode", mode.ordinal());
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		this.mode = Mode.values()[tagCompound.getInteger("mode")];
	}

	public void setOrientation(EnumFacing orientation) {
		this.orientation = orientation;
	}

	public void cycleMode(EntityPlayer player) {
		mode = Mode.values()[(mode.ordinal()+1) % Mode.values().length];
		if (!worldObj.isRemote) {
			player.addChatMessage(new ChatComponentTranslation(StatCollector.translateToLocal("autopackager.mode.current") + " " + StatCollector.translateToLocal(mode.getMessage())));
		}
	}

	public void checkMode(EntityPlayer player) {
		if (!worldObj.isRemote) {
			player.addChatMessage(new ChatComponentTranslation(StatCollector.translateToLocal("autopackager.mode.current") + " " + StatCollector.translateToLocal(mode.getMessage())));
		}
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		return new S35PacketUpdateTileEntity(this.getPos(), getBlockMetadata(), nbt);
	}

	@Override
	public void onDataPacket(NetworkManager netman, S35PacketUpdateTileEntity packet) {
		readFromNBT(packet.getNbtCompound());
		if (worldObj.isRemote) {
			this.worldObj.markBlockForUpdate(this.getPos());
		}
	}

	@Optional.Method(modid = "RefinedRelocation")
	@Override
	public ISortingMemberHandler getHandler() {
		if (sortingHandler == null) {
			sortingHandler = APIUtils.createSortingMemberHandler(this);
		}
		return (ISortingMemberHandler) sortingHandler;
	}

	@SideOnly(Side.CLIENT)
	@SuppressWarnings("unchecked")
	public void addWailaInformation(List information) {
		information.add(StatCollector.translateToLocal("autopackager.mode.current") + " " + StatCollector.translateToLocal(mode.getMessage()));
	}
}
