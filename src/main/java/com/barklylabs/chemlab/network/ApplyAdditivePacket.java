package com.barklylabs.chemlab.network;

import com.barklylabs.chemlab.client.ClientExperienceState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record ApplyAdditivePacket(String additiveId) {
    public static void encode(ApplyAdditivePacket packet, FriendlyByteBuf buffer) { buffer.writeUtf(packet.additiveId(), 64); }
    public static ApplyAdditivePacket decode(FriendlyByteBuf buffer) { return new ApplyAdditivePacket(buffer.readUtf(64)); }
    public static void handle(ApplyAdditivePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientExperienceState.applyAdditive(packet.additiveId())));
        context.setPacketHandled(true);
    }
}
