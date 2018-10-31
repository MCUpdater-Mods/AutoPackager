package org.mcupdater.autopackager;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Logger;
import org.mcupdater.autopackager.compat.TOPCompatibility;
import org.mcupdater.autopackager.proxy.CommonProxy;

import java.util.HashMap;
import java.util.Map;

@Mod(useMetadata = true, modid = "autopackager", guiFactory = "org.mcupdater.autopackager.gui.GuiFactory")
public class AutoPackager {

	@SidedProxy(clientSide = "org.mcupdater.autopackager.proxy.ClientProxy", serverSide = "org.mcupdater.autopackager.proxy.CommonProxy")
	public static CommonProxy proxy;

	public static Configuration config;
	public static int energyPerCycle;
	public static int delayCycleNormal;
	public static int delayCycleIdle;
	public static boolean ludicrousMode;
	public static boolean unbalanced;

	public static Map<ItemStack,IRecipe> large = new HashMap<>();
	public static Map<ItemStack,IRecipe> small = new HashMap<>();
	public static Map<ItemStack,IRecipe> hollow = new HashMap<>();
	public static Map<ItemStack,IRecipe> single = new HashMap<>();
	public static Map<ItemStack,IRecipe> cross = new HashMap<>();
	public static Map<ItemStack,IRecipe> stair = new HashMap<>();
	public static Map<ItemStack,IRecipe> slab = new HashMap<>();
	public static Map<ItemStack,IRecipe> wall = new HashMap<>();
	public static ModMetadata metadata;
	public static Logger logger;
	public static boolean deepSleepEnabled;
	public static int deepSleepCount;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		metadata = evt.getModMetadata();
		logger = evt.getModLog();
		config = new Configuration(evt.getSuggestedConfigurationFile());
		config.load();
		energyPerCycle = config.get("General", "energy_cost", 10, "Forge Energy cost per operation").setLanguageKey("autopackager.config.energy_cost").getInt(10);
		delayCycleNormal = config.get("General", "cycle_delay",10, "Number of ticks between cycles when work is successful").setLanguageKey("autopackager.config.cycle_delay").getInt(10);
		delayCycleIdle = config.get("General", "idle_delay",200, "Number of ticks between cycles when no work has been done").setLanguageKey("autopackager.config.idle_delay").getInt(200);
		deepSleepEnabled = config.get("General", "deepSleep", false, "Should the AutoPackager scale up the idle delay on successive idle cycles").getBoolean(false);
		deepSleepCount = config.get("General", "maxDeepCycles", 20, "Maximum multiplier for deep sleep").getInt(20);
		ludicrousMode = config.get("Insanity", "turbocharged", false,"Do everything possible every cycle").setLanguageKey("autopackager.config.turbocharged").getBoolean();
		unbalanced = config.get("Insanity", "turbo_cheap", false, "Energy cost applies only once per cycle").setLanguageKey("autopackager.config.turbo_cheap").getBoolean();

		if (config.hasChanged()) {
			config.save();
		}

		MinecraftForge.EVENT_BUS.register(this);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent evt) {
		GameRegistry.registerTileEntity(TilePackager.class, "AutoPackager");
		if (Loader.isModLoaded("theoneprobe")) {
			TOPCompatibility.register();
		}
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent evt) {

    }

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent evt) {
		evt.registerServerCommand(new ClearRecipeCacheCommand());
	}

	@SubscribeEvent
	public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event)
	{
		config.save();
	}
}
