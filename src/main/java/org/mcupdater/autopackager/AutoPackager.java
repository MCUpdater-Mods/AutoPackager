package org.mcupdater.autopackager

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.network.NetworkMod;
import net.minecraftforge.common.Configuration;

@Mod(modid = "autopackager", name="AutoPackager", version="1.0", acceptedMinecraftVersions="[1.6],")
@NetworkMod(ClientSideRequired = true, serverSideRequired = true)
public class AutoPackager {
	public static Configuration config;
}
