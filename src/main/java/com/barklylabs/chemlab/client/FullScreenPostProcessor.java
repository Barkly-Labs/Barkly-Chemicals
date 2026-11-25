package com.barklylabs.chemlab.client;

import com.barklylabs.chemlab.BarklyChemLab;
import com.barklylabs.chemlab.config.ChemLabConfig;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.slf4j.Logger;

/** Owns a private, reloadable post chain over the main 3D render target. */
public final class FullScreenPostProcessor extends SimplePreparableReloadListener<Boolean> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation CHAIN_ID = new ResourceLocation(BarklyChemLab.MOD_ID, "shaders/post/chemcraft_perception.json");
    private static final String STAGE_TARGET = "barklychem_stage_a";
    private static final java.util.List<StageSpec> STAGES = java.util.List.of(
            new StageSpec("post_vortex", "barklychem_post_vortex", true, false),
            new StageSpec("post_spectral", "barklychem_post_spectral", false, true));
    private static PostChain chain;
    private static PostPass vortexPass;
    private static PostPass spectralPass;
    private static boolean failedUntilReload;
    private static int width = -1;
    private static int height = -1;
    private static boolean firstRenderLogged;

    @Override protected Boolean prepare(ResourceManager manager, ProfilerFiller profiler) {
        if (manager.getResource(CHAIN_ID).isEmpty()) return false;
        for (StageSpec stage : STAGES) {
            if (manager.getResource(program(stage.programName() + ".json")).isEmpty()
                    || manager.getResource(program(stage.programName() + ".fsh")).isEmpty()) return false;
        }
        return manager.getResource(program("barklychem_post_vertex.vsh")).isPresent();
    }

    @Override protected void apply(Boolean resourcesPresent, ResourceManager manager, ProfilerFiller profiler) {
        closeChain();
        failedUntilReload = false;
        firstRenderLogged = false;
        if (!resourcesPresent) {
            failedUntilReload = true;
            LOGGER.error("ChemCraft full-screen post resources are missing; disabling the post chain until the next resource reload");
            return;
        }
        try {
            Minecraft mc = Minecraft.getInstance();
            var main = mc.getMainRenderTarget();
            chain = new PostChain(mc.getTextureManager(), manager, main, CHAIN_ID);
            var intermediate = chain.getTempTarget(STAGE_TARGET);
            // Each pass reads a complete frame. The second pass writes the composed frame back to main.
            vortexPass = chain.addPass(STAGES.get(0).programName(), main, intermediate);
            spectralPass = chain.addPass(STAGES.get(1).programName(), intermediate, main);
            width = main.width;
            height = main.height;
            LOGGER.info("ChemCraft full-screen post chain compiled with 2 ordered passes from {}", CHAIN_ID);
        } catch (Exception exception) {
            closeChain();
            failedUntilReload = true;
            LOGGER.error("ChemCraft could not initialize its full-screen post chain; GUI layers remain available", exception);
        }
    }

    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;
        if (!ChemLabConfig.CLIENT_SIMULATION_ENABLED.get() || !ChemLabConfig.CLIENT_VISUAL_EFFECTS.get()
                || !ChemLabConfig.CLIENT_POST_PROCESSING.get() || failedUntilReload || chain == null) return;
        float vortex = stageStrength(STAGES.get(0), ChemLabConfig.CLIENT_POST_VORTEX.get());
        float spectral = stageStrength(STAGES.get(1), ChemLabConfig.CLIENT_POST_SPECTRAL.get());
        if (vortex <= 0.002f && spectral <= 0.002f) return;
        Minecraft mc = Minecraft.getInstance();
        try {
            var main = mc.getMainRenderTarget();
            if (main.width != width || main.height != height) {
                chain.resize(main.width, main.height);
                width = main.width;
                height = main.height;
                LOGGER.info("Resized ChemCraft post targets to {}x{}", width, height);
            }
            float intensity = (float)ChemLabConfig.CLIENT_INTENSITY.get().doubleValue();
            float motion = ChemLabConfig.CLIENT_REDUCE_MOTION.get() ? 0f
                    : (ChemLabConfig.CLIENT_PREVENT_FLASHING.get() ? 0.30f : 1f);
            float phase = ClientExperienceState.primaryPhase();
            float elapsed = mc.level == null ? mc.gui.getGuiTicks() : mc.level.getGameTime() + event.getPartialTick();
            set(vortexPass, "Strength", clamp(vortex * intensity, 0f, 0.88f));
            set(vortexPass, "Time", elapsed / 20f);
            set(vortexPass, "Speed", stageParameter("post_vortex", true));
            set(vortexPass, "Scale", stageParameter("post_vortex", false));
            set(vortexPass, "Motion", motion);
            set(vortexPass, "Phase", phase);
            set(vortexPass, "Tint", colorFor("post_vortex"));
            set(spectralPass, "Strength", clamp(spectral * intensity, 0f, 0.88f));
            set(spectralPass, "Speed", stageParameter("post_spectral", true));
            set(spectralPass, "Scale", stageParameter("post_spectral", false));
            set(spectralPass, "Motion", motion);
            set(spectralPass, "Phase", phase);
            set(spectralPass, "Time", elapsed / 20f);
            set(spectralPass, "Tint", colorFor("post_spectral"));
            chain.process(event.getPartialTick());
            if (!firstRenderLogged) {
                firstRenderLogged = true;
                LOGGER.info("ChemCraft full-frame post chain processed the main render target (vortex {}, spectral {})", vortex, spectral);
            }
        } catch (Exception exception) {
            failedUntilReload = true;
            closeChain();
            LOGGER.error("ChemCraft full-screen post processing failed; disabled until resource reload", exception);
        }
    }

    public static void closeChain() {
        if (chain != null) {
            try { chain.close(); }
            catch (Exception exception) { LOGGER.warn("Error closing ChemCraft post chain", exception); }
        }
        chain = null;
        vortexPass = null;
        spectralPass = null;
        width = -1;
        height = -1;
    }

    private static float stageStrength(StageSpec stage, boolean enabled) {
        String type = stage.type();
        if (!enabled || (stage.requiresChromaticToggle() && !ChemLabConfig.CLIENT_CHROMATIC_FRINGE.get())) return 0;
        float result = ClientExperienceState.combined(type, profile -> profile.layers().stream()
                .filter(layer -> layer.type().equals(type))
                .mapToDouble(layer -> layer.intensity() * layer.opacity()).sum());
        if (stage.reducedByMotionSetting() && ChemLabConfig.CLIENT_REDUCE_MOTION.get()) result *= 0.35f;
        return result;
    }

    /** Weighted profile animation parameter; the limits keep resource-pack data bounded. */
    private static float stageParameter(String type, boolean speed) {
        double weighted = 0, total = 0;
        for (ClientExperienceState.Layer active : ClientExperienceState.layers()) {
            for (ExperienceVisualLayer layer : active.profile().layers()) {
                if (!layer.type().equals(type)) continue;
                double weight = active.intensity() * layer.intensity() * layer.opacity();
                weighted += (speed ? layer.speed() : layer.scale()) * weight;
                total += weight;
            }
        }
        float minimum = speed ? 0f : 0.25f;
        return (float)(total > 0 ? Math.max(minimum, Math.min(4f, weighted / total)) : 1f);
    }

    private static float[] colorFor(String type) {
        int color = ClientExperienceState.blendedStageColor(type);
        return new float[]{((color >> 16) & 255) / 255f, ((color >> 8) & 255) / 255f, (color & 255) / 255f};
    }

    private static void set(PostPass pass, String name, float value) {
        if (pass == null) return;
        Uniform uniform = pass.getEffect().getUniform(name);
        if (uniform != null) uniform.set(value);
    }
    private static void set(PostPass pass, String name, float[] value) {
        if (pass == null) return;
        Uniform uniform = pass.getEffect().getUniform(name);
        if (uniform != null && value.length >= 3) uniform.set(value[0], value[1], value[2]);
    }
    private static float clamp(float value, float min, float max) { return Math.max(min, Math.min(max, value)); }
    private static ResourceLocation program(String name) { return new ResourceLocation("minecraft", "shaders/program/" + name); }
    private record StageSpec(String type, String programName, boolean reducedByMotionSetting,
                             boolean requiresChromaticToggle) { }
}
