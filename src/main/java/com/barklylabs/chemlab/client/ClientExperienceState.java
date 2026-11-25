package com.barklylabs.chemlab.client;

import com.barklylabs.chemlab.config.ChemLabConfig;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Local-only, reversible perception layers. Gameplay and movement input are never altered here. */
public final class ClientExperienceState {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<Active> ACTIVE = new ArrayList<>();
    private static final List<ActiveAdditive> ADDITIVES = new ArrayList<>();
    private static long ticks;

    private ClientExperienceState() { }

    public static void start(String id, int durationTicks) {
        if (!ChemLabConfig.CLIENT_SIMULATION_ENABLED.get()) return;
        String key = id.startsWith("barklychemlab:") ? id.substring("barklychemlab:".length()) : id;
        ExperienceProfile profile = ExperienceProfileCatalog.get(key);
        if (profile == null) return;
        // Reusing one artifact refreshes its layer instead of creating a duplicate stack.
        ACTIVE.removeIf(active -> active.profile.id().equals(profile.id()));
        if (ACTIVE.size() >= 6) ACTIVE.remove(ACTIVE.stream().min(java.util.Comparator.comparingDouble(a -> a.profile.strength())).orElseThrow());
        ACTIVE.add(new Active(profile, Math.max(20, Math.min(12000, durationTicks)), ticks));
        LOGGER.info("Started client experience layer '{}' for {} ticks (profile strength {}, local intensity {}, catalog size {})", key, durationTicks, profile.strength(), ChemLabConfig.CLIENT_INTENSITY.get(), ExperienceProfileCatalog.size());
    }

    public static void clear() { ACTIVE.clear(); ADDITIVES.clear(); }

    public static void applyAdditive(String id) {
        if (!ChemLabConfig.CLIENT_SIMULATION_ENABLED.get() || ACTIVE.isEmpty()) return;
        ExperienceAdditive additive = ExperienceModifierCatalog.get(id);
        if (additive == null) {
            LOGGER.warn("Ignored unknown client modifier '{}'; loaded modifier count {}", id, ExperienceModifierCatalog.size());
            return;
        }
        ADDITIVES.removeIf(active -> active.id.equals(additive.id()));
        ADDITIVES.add(new ActiveAdditive(additive.id(), ticks + additive.durationTicks()));
        // Deterministic conflict resolution: retain the four lexicographically first distinct IDs.
        ADDITIVES.sort(java.util.Comparator.comparing(ActiveAdditive::id));
        while (ADDITIVES.size() > ChemLabConfig.CLIENT_MAX_MODIFIERS.get()) ADDITIVES.remove(ADDITIVES.size() - 1);
    }

    public static void tick() {
        ticks++;
        ADDITIVES.removeIf(additive -> ticks >= additive.expiresAt);
        Iterator<Active> iterator = ACTIVE.iterator();
        while (iterator.hasNext()) {
            Active active = iterator.next();
            if (active.age() >= active.duration + active.recoveryTicks()) iterator.remove();
        }
    }

    public static List<Layer> layers() {
        if (!ChemLabConfig.CLIENT_SIMULATION_ENABLED.get() || ChemLabConfig.CLIENT_INTENSITY.get() <= 0) return List.of();
        List<Layer> result = new ArrayList<>();
        for (Active active : ACTIVE) {
            float phase = active.phase();
            if (phase <= 0) continue;
            result.add(new Layer(active.profile, phase * ChemLabConfig.CLIENT_INTENSITY.get().floatValue()));
        }
        return result;
    }

    /** Diminishing-return union, so overlap never grows without bound. */
    public static float combined(String channel, java.util.function.ToDoubleFunction<ExperienceProfile> value) {
        double remainder = 1.0;
        for (Layer layer : layers()) {
            double contribution = Math.max(0, Math.min(0.88, value.applyAsDouble(layer.profile()) * layer.intensity()));
            remainder *= 1.0 - contribution;
        }
        double result = 1.0 - remainder;
        double multiplier = 1.0;
        double additive = 0;
        for (ActiveAdditive active : ADDITIVES) {
            ExperienceAdditive modifier = ExperienceModifierCatalog.get(active.id);
            if (modifier == null) continue;
            multiplier *= modifier.intensityMultiplier();
            additive += modifier.channel(channel);
        }
        return (float) Math.max(0, Math.min(0.88, result * Math.max(0.5, Math.min(1.5, multiplier)) + additive));
    }

    public static int blendedColor() {
        List<Layer> layers = layers();
        if (layers.isEmpty()) return 0xFFFFFF;
        double r=0,g=0,b=0,total=0;
        for (Layer l : layers) {
            float w = l.intensity() * Math.max(0.05f, l.profile().strength());
            r += ((l.profile().color() >> 16) & 255) * w;
            g += ((l.profile().color() >> 8) & 255) * w;
            b += (l.profile().color() & 255) * w;
            total += w;
        }
        for (ActiveAdditive active : ADDITIVES) {
            ExperienceAdditive a = ExperienceModifierCatalog.get(active.id);
            if (a == null) continue;
            float w = 0.18f;
            r += ((a.tint() >> 16) & 255) * w;
            g += ((a.tint() >> 8) & 255) * w;
            b += (a.tint() & 255) * w;
            total += w;
        }
        if (total == 0) return 0xFFFFFF;
        return ((int)(r/total)<<16) | ((int)(g/total)<<8) | (int)(b/total);
    }

    /**
     * Blends the palette for one full-screen stage. Active modifier tints contribute
     * only when that modifier changes the requested stage, keeping rendering data
     * independent from additive item classes.
     */
    public static int blendedStageColor(String channel) {
        double r = 0, g = 0, b = 0, total = 0;
        for (Layer active : layers()) {
            for (ExperienceVisualLayer layer : active.profile().layers()) {
                if (!layer.type().equals(channel)) continue;
                double weight = Math.max(0.001, active.intensity() * layer.intensity() * layer.opacity());
                r += ((layer.color() >> 16) & 255) * weight;
                g += ((layer.color() >> 8) & 255) * weight;
                b += (layer.color() & 255) * weight;
                total += weight;
            }
        }
        for (ActiveAdditive active : ADDITIVES) {
            ExperienceAdditive modifier = ExperienceModifierCatalog.get(active.id);
            if (modifier == null) continue;
            double weight = Math.abs(modifier.channel(channel));
            if (weight <= 0.001) continue;
            r += ((modifier.tint() >> 16) & 255) * weight;
            g += ((modifier.tint() >> 8) & 255) * weight;
            b += (modifier.tint() & 255) * weight;
            total += weight;
        }
        if (total == 0) return blendedColor();
        return ((int)(r / total) << 16) | ((int)(g / total) << 8) | (int)(b / total);
    }

    public static String primaryId() { return ACTIVE.isEmpty() ? "" : ACTIVE.get(ACTIVE.size()-1).profile.id(); }
    public static float primaryPhase() { return ACTIVE.isEmpty() ? 0 : ACTIVE.get(ACTIVE.size()-1).phase(); }
    public static String primaryStage() { return ACTIVE.isEmpty() ? "" : ACTIVE.get(ACTIVE.size()-1).stage(); }
    public static boolean active() { return !layers().isEmpty(); }

    private record Active(ExperienceProfile profile, int duration, long started) {
        int age() { return (int)Math.max(0, ticks - started); }
        int recoveryTicks() { return Math.max(1, Math.round(profile.recoveryTicks() * ChemLabConfig.CLIENT_TRANSITION_SCALE.get().floatValue())); }
        float phase() {
            int age = age();
            int onset = Math.max(1, Math.round(profile.onsetTicks() * ChemLabConfig.CLIENT_TRANSITION_SCALE.get().floatValue()));
            int peak = Math.max(onset + 1, Math.min(duration, profile.peakTicks()));
            if (age < onset) return smooth(age / (float)onset);
            if (age < peak) return 1;
            int declineStart = Math.max(peak, duration - recoveryTicks());
            if (age < declineStart) return 1;
            if (age < duration) return 0.55f * (1 - smooth((age - declineStart) / (float)Math.max(1, duration - declineStart)));
            int recovery = age - duration;
            if (recovery >= recoveryTicks()) return 0;
            return 0.55f * (1 - smooth(recovery / (float)recoveryTicks()));
        }
        String stage() { int age=age(); if (age < profile.onsetTicks()) return "onset"; if (age < duration - recoveryTicks()) return "peak"; return age < duration + recoveryTicks() ? "recovery" : ""; }
        private static float smooth(float x) { x=Math.max(0,Math.min(1,x)); return x*x*(3-2*x); }
    }

    public record Layer(ExperienceProfile profile, float intensity) { }
    private record ActiveAdditive(String id, long expiresAt) { }
}
