package com.mcupdater.autopackager;

import com.mcupdater.autopackager.setup.ClientSetup;
import com.mcupdater.autopackager.setup.Config;
import com.mcupdater.autopackager.setup.ModSetup;
import com.mcupdater.autopackager.setup.Registration;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("autopackager")
public class AutoPackager
{
	public static final String MODID = "autopackager";
	public static final Logger LOGGER = LogManager.getLogger();

	public AutoPackager() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);
		Registration.init();

		FMLJavaModLoadingContext.get().getModEventBus().addListener(ModSetup::init);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::init);
	}
}
