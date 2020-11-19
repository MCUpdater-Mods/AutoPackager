package com.mcupdater.autopackager.setup;

import com.mcupdater.autopackager.AutoPackager;
import com.mcupdater.autopackager.network.PackagerChannel;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = AutoPackager.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModSetup {

    public static final ItemGroup ITEM_GROUP = new ItemGroup("autopackager") {

        @Override
        public ItemStack createIcon() {
            return new ItemStack(Registration.PACKAGERBLOCK.get());
        }
    };

    public static void init(final FMLCommonSetupEvent event) {
        PackagerChannel.init();
    }
}
