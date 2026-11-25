package com.barklylabs.chemlab.client;

import com.barklylabs.chemlab.BarklyChemLab;
import com.barklylabs.chemlab.config.ChemLabConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = BarklyChemLab.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ExperienceClientEvents {
    public static final KeyMapping CLEAR_EXPERIENCE = new KeyMapping("key.barklychemlab.clear_experience", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F8, "key.categories.barklychemlab");

    @SubscribeEvent public static void registerKeys(RegisterKeyMappingsEvent event) { event.register(CLEAR_EXPERIENCE); }
    @SubscribeEvent public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new ExperienceProfileCatalog());
        event.registerReloadListener(new ExperienceModifierCatalog());
        event.registerReloadListener(new FullScreenPostProcessor());
    }

    @Mod.EventBusSubscriber(modid = BarklyChemLab.MOD_ID, value = Dist.CLIENT)
    public static final class ClientEvents {
        @SubscribeEvent public static void clientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            ClientExperienceState.tick();
            while (CLEAR_EXPERIENCE.consumeClick()) if (ChemLabConfig.CLIENT_INSTANT_RESET.get()) ClientExperienceState.clear();
        }
        @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
            ClientExperienceState.clear();
        }
    }

    private ExperienceClientEvents() { }
}
