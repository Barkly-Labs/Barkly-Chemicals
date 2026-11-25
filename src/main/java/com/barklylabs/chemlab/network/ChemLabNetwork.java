package com.barklylabs.chemlab.network;

import com.barklylabs.chemlab.BarklyChemLab;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ChemLabNetwork {
    private static final String PROTOCOL = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(BarklyChemLab.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );
    private static boolean registered;

    public static synchronized void register() {
        if (registered) return;
        CHANNEL.registerMessage(0, StartExperiencePacket.class,
                StartExperiencePacket::encode,
                StartExperiencePacket::decode,
                StartExperiencePacket::handle);
        CHANNEL.registerMessage(1, ApplyAdditivePacket.class,
                ApplyAdditivePacket::encode,
                ApplyAdditivePacket::decode,
                ApplyAdditivePacket::handle);
        registered = true;
    }

    public static void sendToPlayer(ServerPlayer player, StartExperiencePacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToPlayer(ServerPlayer player, ApplyAdditivePacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    private ChemLabNetwork() { }
}
