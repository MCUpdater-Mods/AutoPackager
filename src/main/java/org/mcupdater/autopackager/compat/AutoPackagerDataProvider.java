package org.mcupdater.autopackager.compat;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import org.mcupdater.autopackager.BlockPackager;
import org.mcupdater.autopackager.ModBlocks;
import org.mcupdater.autopackager.TilePackager;

import java.util.List;

/**
 * Created by sbarbour on 2/21/16.
 */

public class AutoPackagerDataProvider implements IWailaDataProvider {
	@Override
	public ItemStack getWailaStack(IWailaDataAccessor iWailaDataAccessor, IWailaConfigHandler iWailaConfigHandler) {
		if (iWailaDataAccessor.getBlock() instanceof BlockPackager) {
			return new ItemStack(ModBlocks.packager, 1, 0);
		}
		else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public List<String> getWailaBody(ItemStack itemStack, List<String> list, IWailaDataAccessor iWailaDataAccessor, IWailaConfigHandler iWailaConfigHandler) {
		TileEntity tile = iWailaDataAccessor.getTileEntity();
		if (tile != null && tile instanceof TilePackager) {
			TilePackager.Mode mode = TilePackager.Mode.values()[iWailaDataAccessor.getNBTData().getInteger("mode")];
			String key = new TextComponentTranslation("autopackager.mode.current").getUnformattedComponentText();
			String value = new TextComponentTranslation(mode.getMessage()).getUnformattedComponentText();
			list.add(key + " " + value);
		}
		return list;
	}

	@Override
	public NBTTagCompound getNBTData(EntityPlayerMP entityPlayerMP, TileEntity tileEntity, NBTTagCompound nbtTagCompound, World world, BlockPos pos) {
		if (tileEntity != null) {
			nbtTagCompound.setInteger("mode", ((TilePackager) tileEntity).getMode().ordinal());
		}
		return nbtTagCompound;
	}
}
