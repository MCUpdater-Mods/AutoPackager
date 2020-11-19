package com.mcupdater.autopackager.setup;

import com.mcupdater.autopackager.tile.ScreenPackager;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {
    public static void init(final FMLClientSetupEvent event) {
        ScreenManager.registerFactory(Registration.PACKAGERBLOCK_CONTAINER.get(), ScreenPackager::new);
    }
}
