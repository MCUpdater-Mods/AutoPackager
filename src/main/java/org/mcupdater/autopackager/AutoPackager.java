package org.mcupdater.autopackager;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.ShapedOreRecipe;

@Mod(modid = "autopackager", name="AutoPackager", version="1.5.2", acceptedMinecraftVersions="[1.7,1.8],", dependencies = "required-after:CoFHCore")
public class AutoPackager {
	public static Configuration config;
	public static BlockPackager packagerBlock;
	public static int energyPerCycle;
	public static int delayCycleNormal;
	public static int delayCycleIdle;
    public static boolean canSort = false;

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		config = new Configuration(evt.getSuggestedConfigurationFile());
		config.load();
		energyPerCycle = config.get("General", "RF_per_cycle", 1000).getInt(1000);
		delayCycleNormal = config.get("General", "cycle_delay_ticks",10).getInt(10);
		delayCycleIdle = config.get("General", "idle_delay_ticks",200).getInt(200);
		if (config.hasChanged()) {
			config.save();
		}

		packagerBlock = new BlockPackager();
		GameRegistry.registerBlock(packagerBlock, ItemBlockPackager.class, packagerBlock.getUnlocalizedName().replace("tile.",""));

		MinecraftForge.EVENT_BUS.register(this);
	}

	@EventHandler
	public void init(FMLInitializationEvent evt) {
		GameRegistry.registerTileEntity(TilePackager.class, "AutoPackager");
        GameRegistry.registerTileEntity(TileSortingPackager.class, "SortingAutoPackager");
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt) {
		loadRecipes();
        if (Loader.isModLoaded("RefinedRelocation")) {
            canSort = true;
        }
    }

	private void loadRecipes() {
		Item keyItem = GameRegistry.findItem("ThermalExpansion","powerCoilGold");
		if (keyItem == null) {
			keyItem = Items.redstone;
		}
		ShapedOreRecipe recipePackager = new ShapedOreRecipe(
			new ItemStack(packagerBlock, 1),
			"ipi",
			"ptp",
			"ici",
			'i', Items.iron_ingot,
			'p', Blocks.piston,
			't', Blocks.crafting_table,
			'c', keyItem
		);
		GameRegistry.addRecipe(recipePackager);
	}
}
