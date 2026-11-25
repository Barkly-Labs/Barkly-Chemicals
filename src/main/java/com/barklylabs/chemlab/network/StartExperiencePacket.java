package com.barklylabs.chemlab.network;

import com.barklylabs.chemlab.client.ClientExperienceState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record StartExperiencePacket(String profileId, int durationTicks) {
    public static void encode(StartExperiencePacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.profileId, 64);
        buffer.writeVarInt(packet.durationTicks);
    }

    public static StartExperiencePacket decode(FriendlyByteBuf buffer) {
        return new StartExperiencePacket(buffer.readUtf(64), buffer.readVarInt());
    }

    public static void handle(StartExperiencePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientExperienceState.start(packet.profileId(), packet.durationTicks())));
        context.setPacketHandled(true);
    }
}
