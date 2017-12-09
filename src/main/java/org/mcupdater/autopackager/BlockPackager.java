package org.mcupdater.autopackager;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
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
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.mcupdater.autopackager.compat.TOPInfoProvider;

public class BlockPackager extends BlockContainer implements TOPInfoProvider
{
	public static final PropertyDirection FACING;

	static {
		FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	}
	
	private Item itemBlock;

	public BlockPackager() {
		super(Material.ROCK);
		setUnlocalizedName("packagerblock");
		setRegistryName(AutoPackager.metadata.modId, "packagerblock");
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
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		TilePackager tile = (TilePackager) world.getTileEntity(pos);
		if (hand.equals(EnumHand.MAIN_HAND) && player.getHeldItemMainhand().equals(ItemStack.EMPTY)) {
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

	@Override
	public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
		TileEntity te = world.getTileEntity(data.getPos());
		if (te instanceof TilePackager) {
			TilePackager tile = (TilePackager) te;
			probeInfo.horizontal().text(new TextComponentTranslation("autopackager.mode.current").getUnformattedComponentText() + " " + new TextComponentTranslation(tile.getMode().getMessage()).getUnformattedComponentText());
		}
	}

	private class ItemBlockPackager extends ItemBlock {

		public ItemBlockPackager(Block block) {
			super(block);
			setUnlocalizedName("packagerblock");
			setRegistryName(AutoPackager.metadata.modId, "packagerblock");
		}
	}

	@SideOnly(Side.CLIENT)
	public void initModel() {
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
	}
}
