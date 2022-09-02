package com.mcupdater.autopackager.setup;

import com.mcupdater.autopackager.block.PackagerScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {
    public static void init(final FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(Registration.PACKAGERBLOCK.get(), RenderType.cutoutMipped());
        MenuScreens.register(Registration.PACKAGERBLOCK_MENU.get(), PackagerScreen::new);
    }

}
