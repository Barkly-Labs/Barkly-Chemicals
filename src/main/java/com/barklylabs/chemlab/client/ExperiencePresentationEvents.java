package com.barklylabs.chemlab.client;

import com.barklylabs.chemlab.BarklyChemLab;
import com.barklylabs.chemlab.config.ChemLabConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod.EventBusSubscriber(modid = BarklyChemLab.MOD_ID, value = Dist.CLIENT)
public final class ExperiencePresentationEvents {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean renderHookSeen;

    @SubscribeEvent public static void camera(ViewportEvent.ComputeCameraAngles event) {
        if (!ChemLabConfig.CLIENT_SIMULATION_ENABLED.get() || !ChemLabConfig.CLIENT_CAMERA_EFFECTS.get() || ChemLabConfig.CLIENT_REDUCE_MOTION.get()) return;
        Minecraft mc = Minecraft.getInstance();
        float phase = (float)(mc.level == null ? 0 : mc.level.getGameTime() * 0.035);
        float sway = ClientExperienceState.combined("camera_sway", p -> p.cameraSway());
        float roll = ClientExperienceState.combined("camera_roll", p -> p.cameraRoll());
        event.setYaw(event.getYaw() + (float)Math.sin(phase) * sway * 0.65f);
        event.setRoll(event.getRoll() + (float)Math.sin(phase * 0.73f) * roll * 0.45f);
    }

    @SubscribeEvent public static void overlay(RenderGuiOverlayEvent.Post event) {
        if (!event.getOverlay().equals(VanillaGuiOverlay.HOTBAR.type())) return;
        if (!renderHookSeen) {
            renderHookSeen = true;
            LOGGER.info("ChemCraft modular screen-space renderer is receiving RenderGuiOverlayEvent.Post at HOTBAR stage");
        }
        if (!ChemLabConfig.CLIENT_SIMULATION_ENABLED.get() || !ChemLabConfig.CLIENT_VISUAL_EFFECTS.get()) return;
        draw(event.getGuiGraphics(), false);
    }

    @SubscribeEvent public static void menuOverlay(ScreenEvent.Render.Post event) {
        if (!ChemLabConfig.CLIENT_SIMULATION_ENABLED.get() || !ChemLabConfig.CLIENT_VISUAL_EFFECTS.get()) return;
        draw(event.getGuiGraphics(), true);
    }

    private static void draw(net.minecraft.client.gui.GuiGraphics graphics, boolean menu) {
        var active = ClientExperienceState.layers();
        if (active.isEmpty()) return;
        ExperienceLayerRenderers.render(graphics, menu, active);
        if (ChemLabConfig.CLIENT_HUD_FEEDBACK.get()) {
            String key = "hud.barklychemlab.profile." + ClientExperienceState.primaryId();
            String msg = net.minecraft.network.chat.Component.translatable(key).getString() + " · "
                    + net.minecraft.network.chat.Component.translatable("hud.barklychemlab.stage." + ClientExperienceState.primaryStage()).getString();
            if (msg.equals(key)) msg = ClientExperienceState.primaryId().replace('_', ' ');
            graphics.drawString(Minecraft.getInstance().font, msg, 6, 6, 0xB8FFFFFF, false);
        }
    }

    private ExperiencePresentationEvents() { }
}
