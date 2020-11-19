package com.mcupdater.autopackager.network;

import com.mcupdater.autopackager.tile.TilePackager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ModePacket {
    private BlockPos blockPos;

    public ModePacket(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public static void toBytes(ModePacket msg, PacketBuffer packetBuffer) {
        packetBuffer.writeBlockPos(msg.blockPos);
    }

    public static ModePacket fromBytes(PacketBuffer packetBuffer) {
        return new ModePacket(packetBuffer.readBlockPos());
    }

    public static void handle(ModePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            World world = ctx.get().getSender().world;
            if (world.getTileEntity(msg.blockPos) instanceof TilePackager) {
                TilePackager tilePackager = (TilePackager) world.getTileEntity(msg.blockPos);
                tilePackager.changeMode();
            }
        });
    }
}
