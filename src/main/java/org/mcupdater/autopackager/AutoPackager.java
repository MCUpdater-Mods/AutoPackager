package org.mcupdater.autopackager;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.mcupdater.autopackager.gui.ConfigGuiScreen;
import org.mcupdater.autopackager.proxy.CommonProxy;

import java.util.HashMap;
import java.util.Map;

@Mod(useMetadata = true, modid = "autopackager", guiFactory = "org.mcupdater.autopackager.gui.GuiFactory")
public class AutoPackager {

	@SidedProxy(clientSide = "org.mcupdater.autopackager.proxy.ClientProxy", serverSide = "org.mcupdater.autopackager.proxy.CommonProxy")
	public static CommonProxy proxy;

	public static Configuration config;
	public static BlockPackager packagerBlock;
	public static int energyPerCycle;
	public static int delayCycleNormal;
	public static int delayCycleIdle;
	public static String keyItemString;
	public static boolean ludicrousMode;
	public static boolean unbalanced;

	public static Map<ItemStack,ItemStack> large = new HashMap<ItemStack, ItemStack>();
	public static Map<ItemStack,ItemStack> small = new HashMap<ItemStack, ItemStack>();
	public static Map<ItemStack,ItemStack> hollow = new HashMap<ItemStack, ItemStack>();
	public static Map<ItemStack,ItemStack> single = new HashMap<ItemStack, ItemStack>();
	public static ModMetadata metadata;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		metadata = evt.getModMetadata();
		config = new Configuration(evt.getSuggestedConfigurationFile());
		config.load();
		energyPerCycle = config.get("General", "energy_cost", 1000, "Forge Energy cost per operation").getInt(1000);
		delayCycleNormal = config.get("General", "cycle_delay",10, "Number of ticks between cycles when work is successful").getInt(10);
		delayCycleIdle = config.get("General", "idle_delay",200, "Number of ticks between cycles when no work has been done").getInt(200);
		keyItemString = config.get("General", "key_item", "minecraft:redstone", "Key item for recipe (bottom-center)").getString();
		ludicrousMode = config.get("Insanity", "turbocharged", false,"Do everything possible every cycle").getBoolean();
		unbalanced = config.get("Insanity", "turbo_cheap", false, "Energy cost applies only once per cycle").getBoolean();

		if (config.hasChanged()) {
			config.save();
		}
		packagerBlock=new BlockPackager();
		GameRegistry.register(packagerBlock);
		GameRegistry.register(packagerBlock.getItemBlock());
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
			proxy.doClientRegistrations();
		}
    }

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent evt) {
		evt.registerServerCommand(new ClearRecipeCacheCommand());
	}

	private void loadRecipes() {
		System.out.println("Key item: " + keyItemString);
		ShapedOreRecipe recipePackager = new ShapedOreRecipe(
			new ItemStack(packagerBlock, 1),
			"ipi",
			"ptp",
			"ici",
			'i', Items.IRON_INGOT,
			'p', Blocks.PISTON,
			't', Blocks.CRAFTING_TABLE,
			'c', keyItemString.contains(":") ? Item.REGISTRY.getObject(new ResourceLocation(keyItemString)) : keyItemString
		);
		GameRegistry.addRecipe(recipePackager);
	}

	@SubscribeEvent
	public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event)
	{
		config.save();
	}
}
