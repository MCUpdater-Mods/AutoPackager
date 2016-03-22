package org.mcupdater.autopackager;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.mcupdater.autopackager.proxy.CommonProxy;

import java.util.HashMap;
import java.util.Map;

@Mod(useMetadata = true, modid = "autopackager")
public class AutoPackager {

	@SidedProxy(clientSide = "org.mcupdater.autopackager.proxy.ClientProxy", serverSide = "org.mcupdater.autopackager.proxy.CommonProxy")
	public static CommonProxy proxy;

	public static Configuration config;
	public static Block packagerBlock;
	public static int energyPerCycle;
	public static int delayCycleNormal;
	public static int delayCycleIdle;

	public static Map<ItemStack,ItemStack> large = new HashMap<ItemStack, ItemStack>();
	public static Map<ItemStack,ItemStack> small = new HashMap<ItemStack, ItemStack>();
	public static Map<ItemStack,ItemStack> hollow = new HashMap<ItemStack, ItemStack>();
	public static Map<ItemStack,ItemStack> single = new HashMap<ItemStack, ItemStack>();

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		config = new Configuration(evt.getSuggestedConfigurationFile());
		config.load();
		energyPerCycle = config.get("General", "RF_per_cycle", 1000).getInt(1000);
		delayCycleNormal = config.get("General", "cycle_delay_ticks",10).getInt(10);
		delayCycleIdle = config.get("General", "idle_delay_ticks",200).getInt(200);
		if (config.hasChanged()) {
			config.save();
		}
		packagerBlock=new BlockPackager();
		GameRegistry.registerBlock(packagerBlock,ItemBlockPackager.class,packagerBlock.getUnlocalizedName().replace("tile.",""));
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent evt) {
		GameRegistry.registerTileEntity(TilePackager.class, "AutoPackager");
		if (Loader.isModLoaded("Waila")) {
			FMLInterModComms.sendMessage("Waila", "register", "org.mcupdater.autopackager.compat.WailaRegistry.initWaila");
		}
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent evt) {
		loadRecipes();
		if (proxy.isClient()) {
			Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(Item.getItemFromBlock(packagerBlock), 0, new ModelResourceLocation("autopackager:packagerBlock", "inventory"));
		}
    }

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent evt) {
		evt.registerServerCommand(new ClearRecipeCacheCommand());
	}

	private void loadRecipes() {
		ItemStack keyItem = new ItemStack(GameRegistry.findItem("ThermalExpansion","powerCoilGold"),1);
		if (keyItem.getItem() == null) {
			keyItem = new ItemStack(Items.redstone);
		}
		System.out.println("Key item: " + keyItem.getDisplayName());
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
