package org.mcupdater.autopackager;

import cofh.api.block.IDismantleable;
import cofh.util.BlockHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import org.mcupdater.shared.Position;
import org.mcupdater.shared.Utils;

public class BlockPackager extends BlockContainer implements IDismantleable
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

		world.setBlockMetadataWithNotify(i, j, k, orientation.getOpposite().ordinal(), 1);
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
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer player, int side, float par7, float par8, float par9) {
		TilePackager tile = (TilePackager) world.getBlockTileEntity(i, j, k);

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
	public boolean rotateBlock(World world, int x, int y, int z, ForgeDirection axis) {
		int meta = world.getBlockMetadata(x,y,z);
		int newMeta = BlockHelper.getLeftSide(meta);
		world.setBlockMetadataWithNotify(x,y,z,newMeta,3);
		((TilePackager) world.getBlockTileEntity(x, y, z)).setOrientation(ForgeDirection.getOrientation(newMeta).getOpposite());
		return true;
	}

	@Override
	public ItemStack dismantleBlock(EntityPlayer entityPlayer, World world, int x, int y, int z, boolean placeInInventory) {
		ItemStack dropped = new ItemStack(AutoPackager.packagerBlock);
		world.setBlockToAir(x,y,z);

		//Spawn in world
		float multiplier = 0.3F;
		double deltaX = world.rand.nextFloat() * multiplier + (1.0F - multiplier) * 0.5D;
		double deltaY = world.rand.nextFloat() * multiplier + (1.0F - multiplier) * 0.5D;
		double deltaZ = world.rand.nextFloat() * multiplier + (1.0F - multiplier) * 0.5D;
		EntityItem spawnedItem = new EntityItem(world, x+deltaX, y+deltaY, z+ deltaZ, dropped);
		spawnedItem.delayBeforeCanPickup = 10;
		world.spawnEntityInWorld(spawnedItem);

		return dropped;
	}

	@Override
	public boolean canDismantle(EntityPlayer entityPlayer, World world, int i, int i2, int i3) {
		return true;
	}
}
