package org.mcupdater.autopackager;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModBlocks {

	@GameRegistry.ObjectHolder("autopackager:packagerblock")
	public static BlockPackager packager;

	@SideOnly(Side.CLIENT)
	public static void initModels() {
		packager.initModel();
	}
}