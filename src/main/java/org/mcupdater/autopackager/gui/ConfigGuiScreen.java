package org.mcupdater.autopackager.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.mcupdater.autopackager.AutoPackager;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class ConfigGuiScreen extends GuiConfig {
	public ConfigGuiScreen(final GuiScreen parentScreen) {
		super(parentScreen, getConfigElements(), AutoPackager.metadata.modId, false, false, "AutoPackager Config");
	}

	private static List<IConfigElement> getConfigElements() {
		List<IConfigElement> configElements = new ArrayList<>();
		configElements.addAll(new ConfigElement(AutoPackager.config.getCategory("General")).getChildElements());
		configElements.addAll(new ConfigElement(AutoPackager.config.getCategory("Insanity")).getChildElements());
		return configElements;
	}
}
