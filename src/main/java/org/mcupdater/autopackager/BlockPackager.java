package org.mcupdater.autopackager;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.Collection;

public class BlockPackager extends BlockContainer
{
	public static final PropertyDirection FACING;

	static {
		FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	}

	protected BlockPackager() {
		super(Material.rock);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
		setHardness(10F);
		setResistance(10F);
		setStepSound(soundTypeStone);
		setUnlocalizedName("packagerBlock");
		setCreativeTab(CreativeTabs.tabRedstone);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState blockState, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(world, pos, blockState, placer, stack);
		world.setBlockState(pos, blockState.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TilePackager) {
			((TilePackager) tile).setOrientation(blockState.getValue(FACING));
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
	    return new TilePackager();
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
		TilePackager tile = (TilePackager) world.getTileEntity(pos);

		if (player.getCurrentEquippedItem() == null) {
			if (player.isSneaking()) {
				tile.cycleMode(player);
			} else {
				tile.checkMode(player);
			}
			return true;
		}

		return false;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.getFront(meta);
		if(enumfacing.getAxis() == EnumFacing.Axis.Y) {
			enumfacing = EnumFacing.NORTH;
		}

		return this.getDefaultState().withProperty(FACING, enumfacing);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getIndex();
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, FACING);
	}

	@Override
	public int getRenderType() {
		return 3;
	}
}
