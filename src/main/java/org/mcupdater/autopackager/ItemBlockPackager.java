package org.mcupdater.autopackager;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockPackager extends ItemBlock
{
	public ItemBlockPackager(int id) {
		super(id);
	}

	@Override
	public String getItemDisplayName(ItemStack itemstack) {
		return "AutoPackager";
	}
}
