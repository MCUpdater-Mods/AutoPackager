package org.mcupdater.autopackager;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockPackager extends BlockContainer
{
	public static final PropertyDirection FACING;

	static {
		FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	}
	
	private Item itemBlock;

	protected BlockPackager() {
		super(Material.ROCK);
		setUnlocalizedName("packagerBlock");
		setRegistryName(AutoPackager.metadata.modId, "packagerBlock");
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
		setHardness(10F);
		setResistance(10F);
		setSoundType(SoundType.STONE);
		setCreativeTab(CreativeTabs.REDSTONE);

		itemBlock = this.generateItemBlock();
	}

	private Item generateItemBlock() {
		ItemBlock itemBlock = new ItemBlockPackager(this);
		return itemBlock;
	}

	public Item getItemBlock() {
		return this.itemBlock;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState blockState, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(world, pos, blockState, placer, stack);
		world.setBlockState(pos, blockState.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TilePackager) {
			((TilePackager) tile).setOrientation(world.getBlockState(pos).getValue(FACING));
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
	    return new TilePackager();
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		TilePackager tile = (TilePackager) world.getTileEntity(pos);

		if (heldItem == null) {
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
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	private class ItemBlockPackager extends ItemBlock {

		public ItemBlockPackager(Block block) {
			super(block);
			setUnlocalizedName("reconstructorBlock");
			setRegistryName(AutoPackager.metadata.modId, "packagerBlock");
		}
	}
}
