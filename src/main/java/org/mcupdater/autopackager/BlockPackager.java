package org.mcupdater.autopackager;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import org.mcupdater.shared.Position;
import org.mcupdater.shared.Utils;

public class BlockPackager extends BlockContainer
{
	Icon textureFront;
	Icon textureSide;

	protected BlockPackager(int par1) {
		super(par1, Material.rock);
		setHardness(10F);
		setResistance(10F);
		setStepSound(soundStoneFootstep);
		setUnlocalizedName("packagerBlock");
		setCreativeTab(CreativeTabs.tabRedstone);
	}

	@Override
	public void onBlockPlacedBy(World world, int i, int j, int k, EntityLivingBase entityliving, ItemStack stack) {
		super.onBlockPlacedBy(world, i, j, k, entityliving, stack);
		ForgeDirection orientation = Utils.get2dOrientation(new Position(entityliving.posX, entityliving.posY, entityliving.posZ), new Position(i, j, k));

		world.setBlockMetadataWithNotify(i, j, k, orientation.getOpposite().ordinal(),1);
		System.out.println("Placed meta: " + orientation.getOpposite().ordinal());
		((TilePackager) world.getBlockTileEntity(i, j, k)).setOrientation(orientation);
	}

	@Override
	public Icon getIcon(int i, int j) {
		// If no metadata is set, then this is an icon.
		if (j == 0 && i == 3)
			return textureFront;

		if (i == j && i>1) // Front can't be top or bottom.
			return textureFront;

		return textureSide;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegistry)
	{
		textureFront = iconRegistry.registerIcon("autopackager:packager_front");
		textureSide = iconRegistry.registerIcon("autopackager:packager_side");
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TilePackager();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int hitSide, float hitX, float hitY, float hitZ) {
		super.onBlockActivated(world,x,y,z,player,hitSide,hitX,hitY,hitZ);
		if (thermalexpansion.util.Utils.isHoldingUsableWrench(player, x, y, z)) {
			if (!world.isRemote) {
				rotateBlock(world,x,y,z,ForgeDirection.UP);
			}
		}
		return true;
	}

	@Override
	public boolean rotateBlock(World world, int x, int y, int z, ForgeDirection axis) {
		int meta = world.getBlockMetadata(x,y,z) - 2; //convert to x/z axis
		meta = ++meta % 4 + 2;
		world.setBlockMetadataWithNotify(x,y,z,meta,1);
		((TilePackager) world.getBlockTileEntity(x, y, z)).setOrientation(ForgeDirection.getOrientation(meta));
		return true;
	}
}
