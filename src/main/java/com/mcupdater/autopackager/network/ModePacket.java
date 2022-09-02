package com.mcupdater.autopackager.network;

import com.mcupdater.autopackager.block.PackagerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ModePacket {
    private BlockPos blockPos;

    public ModePacket(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public static void toBytes(ModePacket msg, FriendlyByteBuf packetBuffer) {
        packetBuffer.writeBlockPos(msg.blockPos);
    }

    public static ModePacket fromBytes(FriendlyByteBuf packetBuffer) {
        return new ModePacket(packetBuffer.readBlockPos());
    }

    public static void handle(ModePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Level level = ctx.get().getSender().level;
            if (level.getBlockEntity(msg.blockPos) instanceof PackagerEntity) {
                PackagerEntity tilePackager = (PackagerEntity) level.getBlockEntity(msg.blockPos);
                tilePackager.changeMode();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
