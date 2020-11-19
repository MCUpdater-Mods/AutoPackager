package com.mcupdater.autopackager.network;

import com.mcupdater.autopackager.AutoPackager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PackagerChannel {
    private static final String PROTOCOL_VERSION = "1";
    public static SimpleChannel INSTANCE;

    public static void init() {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(AutoPackager.MODID,"mode"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

        INSTANCE.registerMessage(0,
                ModePacket.class,
                ModePacket::toBytes,
                ModePacket::fromBytes,
                ModePacket::handle
        );
    }
}
