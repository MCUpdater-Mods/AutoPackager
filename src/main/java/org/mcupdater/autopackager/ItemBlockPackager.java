package org.mcupdater.autopackager;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockPackager extends ItemBlock
{
	public ItemBlockPackager(Block block) {
		super(block);
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		return "tile.autopackager";
	}
}
