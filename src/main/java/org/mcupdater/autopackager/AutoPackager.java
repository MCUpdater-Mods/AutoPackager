package org.mcupdater.autopackager;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.ShapedOreRecipe;
import thermalexpansion.item.TEItems;

@Mod(modid = "autopackager", name="AutoPackager", version="1.0", acceptedMinecraftVersions="[1.6,1.7],", dependencies = "required-after:CoFHCore;required-after:ThermalExpansion")
@NetworkMod(clientSideRequired = true, serverSideRequired = true)
public class AutoPackager {
	public static Configuration config;
	public static BlockPackager packagerBlock;
	public static int energyPerCycle;
	public static int delayCycleNormal;
	public static int delayCycleIdle;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		config = new Configuration(evt.getSuggestedConfigurationFile());
		config.load();
		int packagerId = config.getBlock("packager.id",3001).getInt(3001);
		energyPerCycle = config.get("General", "RF_per_cycle", 1000).getInt(1000);
		delayCycleNormal = config.get("General", "cycle_delay_ticks",10).getInt(10);
		delayCycleIdle = config.get("General", "idle_delay_ticks",200).getInt(200);
		if (config.hasChanged()) {
			config.save();
		}

		packagerBlock = new BlockPackager(packagerId);
		GameRegistry.registerBlock(packagerBlock, ItemBlockPackager.class, packagerBlock.getUnlocalizedName().replace("tile.",""));
		LanguageRegistry.addName(new ItemStack(packagerBlock),"AutoPackager");

		MinecraftForge.EVENT_BUS.register(this);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent evt) {
		GameRegistry.registerTileEntity(TilePackager.class, "AutoPackager");
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent evt) {
		loadRecipes();
	}

	private void loadRecipes() {
		ShapedOreRecipe recipePackager = new ShapedOreRecipe(
			new ItemStack(packagerBlock, 1),
			"ipi",
			"ptp",
			"ici",
			'i', Item.ingotIron,
			'p', Block.pistonBase,
			't', Block.workbench,
			'c', TEItems.powerCoilGold
		);
		GameRegistry.addRecipe(recipePackager);
	}
}
