package com.barklylabs.chemlab.client;

import com.barklylabs.chemlab.BarklyChemLab;
import com.barklylabs.chemlab.config.ChemLabConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = BarklyChemLab.MOD_ID, value = Dist.CLIENT)
public final class ExperiencePresentationEvents {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean renderHookSeen;
    private static final Set<String> RENDERED_PROFILES = new HashSet<>();
    @SubscribeEvent public static void camera(ViewportEvent.ComputeCameraAngles event) {
        if (!ChemLabConfig.CLIENT_CAMERA_EFFECTS.get() || ChemLabConfig.CLIENT_REDUCE_MOTION.get()) return;
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
            LOGGER.info("ChemCraft screen-space overlay hook is receiving Forge RenderGuiOverlayEvent.Post at HOTBAR stage");
        }
        if (!ChemLabConfig.CLIENT_VISUAL_EFFECTS.get()) return;
        draw(event.getGuiGraphics(), false);
    }

    @SubscribeEvent public static void menuOverlay(ScreenEvent.Render.Post event) {
        if (!ChemLabConfig.CLIENT_VISUAL_EFFECTS.get() || !ChemLabConfig.CLIENT_HUD_FEEDBACK.get()) return;
        draw(event.getGuiGraphics(), true);
    }

    private static void draw(GuiGraphics graphics, boolean menu) {
        var layers = ClientExperienceState.layers();
        if (layers.isEmpty()) return;
        for (ClientExperienceState.Layer layer : layers) {
            if (RENDERED_PROFILES.add(layer.profile().id())) LOGGER.info("Rendering screen-space profile '{}' at intensity {}", layer.profile().id(), layer.intensity());
        }
        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int rgb = ClientExperienceState.blendedColor();
        float intensity = ClientExperienceState.combined("strength", p -> p.strength());
        // Four nested translucent bands make the world-space frame perceptible at
        // normal GUI scale while remaining outside the center and hotbar controls.
        int[] bandWidths = {24, 16, 8, 3};
        float[] bandWeights = {0.16f, 0.22f, 0.28f, 0.34f};
        int maxBandAlpha = menu ? 58 : 118;
        for (int i = 0; i < bandWidths.length; i++) {
            int alpha = Math.max(0, Math.min(maxBandAlpha, Math.round(maxBandAlpha * intensity * bandWeights[i])));
            if (alpha == 0) continue;
            int band = bandWidths[i];
            int color = (alpha << 24) | rgb;
            graphics.fill(0, 0, width, band, color);
            graphics.fill(0, height - band, width, height, color);
            if (!menu) {
                graphics.fill(0, 0, band, height, color);
                graphics.fill(width - band, 0, width, height, color);
            }
        }
        if (!menu) {
            float fringe = ChemLabConfig.CLIENT_CHROMATIC_FRINGE.get() ? ClientExperienceState.combined("fringe", p -> p.fringe()) : 0;
            if (fringe > 0.04f) {
                int a = Math.min(72, Math.round(fringe * 92));
                graphics.fill(width / 2 - 27, height / 2 - 1, width / 2 - 22, height / 2 + 1, (a << 24) | 0x55DDF0);
                graphics.fill(width / 2 + 22, height / 2 - 1, width / 2 + 27, height / 2 + 1, (a << 24) | 0xEF78D8);
            }
            float warp = ClientExperienceState.combined("warp", p -> p.warp());
            if (warp > 0.10f && !ChemLabConfig.CLIENT_REDUCE_MOTION.get()) {
                long t = Minecraft.getInstance().level == null ? 0 : Minecraft.getInstance().level.getGameTime();
                int shift = (int)Math.round(Math.sin(t * 0.018) * Math.min(7, warp * 8));
                int wa = Math.min(48, Math.round(warp * 60));
                int wc = (wa << 24) | rgb;
                graphics.fill(width / 2 - 54 + shift, height / 2 - 31, width / 2 + 54 + shift, height / 2 - 30, wc);
                graphics.fill(width / 2 - 54 - shift, height / 2 + 30, width / 2 + 54 - shift, height / 2 + 31, wc);
            }
            for (ClientExperienceState.Layer layer : layers) drawPattern(graphics, layer.profile(), width, height, layer.intensity());
        }
        if (!menu && !ChemLabConfig.CLIENT_REDUCE_MOTION.get() && ChemLabConfig.CLIENT_VISUAL_TRAILS.get()) {
            float trail = ChemLabConfig.CLIENT_VISUAL_TRAILS.get() ? ClientExperienceState.combined("trail", p -> p.trail()) : 0;
            if (trail > 0.02f) {
                long tick = Minecraft.getInstance().level == null ? 0 : Minecraft.getInstance().level.getGameTime();
                int offset = (int)(Math.sin(tick * 0.035) * 3);
                int trailAlpha = Math.min(18, Math.round(trail * 18));
                int trailColor = (trailAlpha << 24) | rgb;
                graphics.fill(Math.max(0, width / 2 - 24 + offset), height / 2 - 1, width / 2 - 18 + offset, height / 2 + 1, trailColor);
                graphics.fill(Math.min(width, width / 2 + 18 - offset), height / 2 - 1, Math.min(width, width / 2 + 24 - offset), height / 2 + 1, trailColor);
            }
        }
        if (!menu && Minecraft.getInstance().player != null) {
            float movement = ClientExperienceState.combined("movement", p -> p.movement());
            if (movement > 0.03f && Minecraft.getInstance().player.getDeltaMovement().horizontalDistanceSqr() > 0.0025) {
                int ma = Math.min(64, Math.round(movement * 90));
                int mc = (ma << 24) | rgb;
                graphics.fill(8, height / 2 + 18, 8 + Math.min(48, Math.round(movement * 64)), height / 2 + 20, mc);
                graphics.fill(width - 8 - Math.min(48, Math.round(movement * 64)), height / 2 - 20, width - 8, height / 2 - 18, mc);
            }
        }
        if (ChemLabConfig.CLIENT_HUD_FEEDBACK.get()) {
            String key = "hud.barklychemlab.profile." + ClientExperienceState.primaryId();
            String msg = net.minecraft.network.chat.Component.translatable(key).getString() + " · " + net.minecraft.network.chat.Component.translatable("hud.barklychemlab.stage." + ClientExperienceState.primaryStage()).getString();
            if (msg.equals(key)) msg = ClientExperienceState.primaryId().replace('_', ' ');
            graphics.drawString(Minecraft.getInstance().font, msg, 6, 6, 0xB8FFFFFF, false);
        }
    }

    private static void drawPattern(GuiGraphics g, ExperienceProfile profile, int width, int height, float power) {
        int alpha = Math.max(0, Math.min(112, Math.round(112 * power * profile.hud())));
        if (alpha < 3) return;
        int c = (alpha << 24) | profile.color();
        int x = 8, y = 18;
        switch (profile.pattern()) {
            case "echo" -> { for (int i=0;i<3;i++) g.fill(x+i*5,y+i*3,x+i*5+3,y+i*3+2,c); }
            case "afterimage" -> { for (int i=0;i<4;i++) g.fill(x+i*3,y+1,x+i*3+1,y+11-i*2,c); }
            case "mirror" -> { g.fill(width-22,y,width-19,y+14,c); g.fill(width-18,y+3,width-13,y+11,c); g.fill(width-12,y+1,width-9,y+13,c); }
            case "facets", "prism" -> { g.fill(width-18,y,width-13,y+5,c); g.fill(width-15,y+5,width-10,y+10,c); g.fill(width-20,y+10,width-15,y+15,c); }
            case "leaf", "moth" -> { g.fill(x,y+5,x+2,y+10,c); g.fill(x+2,y+2,x+6,y+6,c); g.fill(x+4,y+7,x+8,y+11,c); }
            case "cloud" -> { for (int i=0;i<4;i++) g.fill(width-29+i*4,y+(i%2)*3,width-26+i*4,y+2+(i%2)*3,c); }
            case "tide" -> { g.fill(width-25,y+2,width-20,y+4,c); g.fill(width-20,y+5,width-15,y+7,c); g.fill(width-25,y+8,width-20,y+10,c); g.fill(width-20,y+11,width-15,y+13,c); }
            case "chime" -> { g.fill(x+3,y,x+5,y+2,c); g.fill(x+1,y+3,x+7,y+9,c); g.fill(x,y+10,x+8,y+12,c); }
            case "clock" -> { g.fill(x,y,x+12,y+2,c); g.fill(x,y,x+2,y+12,c); g.fill(x+10,y,x+12,y+12,c); g.fill(x+2,y+10,x+10,y+12,c); }
            case "spark" -> { g.fill(x+4,y,x+6,y+3,c); g.fill(x+2,y+3,x+8,y+5,c); g.fill(x+4,y+5,x+6,y+9,c); g.fill(x,y+10,x+2,y+12,c); }
            case "sun" -> { g.fill(width-17,y+2,width-15,y+12,c); g.fill(width-22,y+6,width-10,y+8,c); }
            case "sunburst" -> { g.fill(width-18,y+4,width-14,y+8,c); g.fill(width-24,y+5,width-20,y+7,c); g.fill(width-12,y+5,width-8,y+7,c); g.fill(width-17,y,width-15,y+3,c); g.fill(width-17,y+9,width-15,y+12,c); }
            case "ember" -> { g.fill(width-17,y+2,width-14,y+8,c); g.fill(width-16,y+8,width-15,y+13,c); g.fill(width-21,y+10,width-18,y+13,c); }
            case "warmth" -> { g.fill(width-22,y+10,width-10,y+12,c); g.fill(width-19,y+7,width-13,y+9,c); g.fill(width-17,y+4,width-15,y+6,c); }
            case "moon", "lantern" -> { g.fill(x,y+2,x+7,y+11,c); g.fill(x+3,y+1,x+8,y+9,(Math.max(0,alpha/2)<<24)|profile.color()); }
            case "orbit" -> { g.fill(x,y+5,x+12,y+7,c); g.fill(x+5,y+1,x+7,y+3,c); g.fill(x+8,y+10,x+10,y+12,c); }
            case "bloom" -> { g.fill(x+3,y+1,x+5,y+5,c); g.fill(x,y+4,x+4,y+7,c); g.fill(x+5,y+4,x+9,y+7,c); g.fill(x+3,y+7,x+6,y+11,c); g.fill(x+3,y+11,x+6,y+13,c); }
            case "archive" -> { g.fill(width-22,y,width-10,y+2,c); g.fill(width-20,y+4,width-12,y+5,c); g.fill(width-18,y+7,width-10,y+8,c); }
            default -> { g.fill(x,y,x+4,y+2,c); }
        }
    }

    private ExperiencePresentationEvents() { }
}
