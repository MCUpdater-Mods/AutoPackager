package org.mcupdater.autopackager;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import net.minecraftforge.common.Configuration;

@Mod(modid = "autopackager", name="AutoPackager", version="1.0", acceptedMinecraftVersions="[1.6],", dependencies = "required-after:CoFHCore")
@NetworkMod(clientSideRequired = true, serverSideRequired = true)
public class AutoPackager {
	public static Configuration config;
	public static BlockPackager packagerBlock;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		config = new Configuration(evt.getSuggestedConfigurationFile());
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent evt) {

	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent evt) {

	}

}
