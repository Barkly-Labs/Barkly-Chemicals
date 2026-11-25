package com.barklylabs.chemlab.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ChemLabConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.BooleanValue ENABLE_EXPERIENCE_EFFECTS;
    public static final ForgeConfigSpec.IntValue PROFILE_DURATION_TICKS;
    public static final ForgeConfigSpec.IntValue TOKEN_COOLDOWN_TICKS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_ADVANCEMENT_REACTIONS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_REACTION_SOUNDS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_PROFILE_PARTICLES;
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ForgeConfigSpec.BooleanValue CLIENT_SIMULATION_ENABLED;
    public static final ForgeConfigSpec.BooleanValue CLIENT_VISUAL_EFFECTS;
    public static final ForgeConfigSpec.BooleanValue CLIENT_CAMERA_EFFECTS;
    public static final ForgeConfigSpec.BooleanValue CLIENT_REDUCE_MOTION;
    public static final ForgeConfigSpec.BooleanValue CLIENT_VISUAL_TRAILS;
    public static final ForgeConfigSpec.BooleanValue CLIENT_CHROMATIC_FRINGE;
    public static final ForgeConfigSpec.BooleanValue CLIENT_AUDIO_EFFECTS;
    public static final ForgeConfigSpec.BooleanValue CLIENT_HUD_FEEDBACK;
    public static final ForgeConfigSpec.BooleanValue CLIENT_PREVENT_FLASHING;
    public static final ForgeConfigSpec.BooleanValue CLIENT_INSTANT_RESET;
    public static final ForgeConfigSpec.DoubleValue CLIENT_INTENSITY;
    public static final ForgeConfigSpec.DoubleValue CLIENT_TRANSITION_SCALE;
    public static final ForgeConfigSpec SPEC;

    static {
        BUILDER.push("fictional_experiences");
        ENABLE_EXPERIENCE_EFFECTS = BUILDER.comment("Enable the short, Minecraft-only effects on research artifacts.").define("enabled", true);
        PROFILE_DURATION_TICKS = BUILDER.comment("Base experience duration in ticks; clamped to 20..6000.").defineInRange("durationTicks", 600, 20, 6000);
        TOKEN_COOLDOWN_TICKS = BUILDER.comment("Reuse delay for an artifact, in ticks; clamped to 0..72000.").defineInRange("cooldownTicks", 1200, 0, 72000);
        BUILDER.pop();
        BUILDER.push("accessibility");
        ENABLE_ADVANCEMENT_REACTIONS = BUILDER.comment("Show one short, personalized chat reaction when a Barkly research challenge is earned.").define("advancementReactions", true);
        ENABLE_REACTION_SOUNDS = BUILDER.comment("Play a quiet chime with Barkly research challenge reactions.").define("reactionSounds", false);
        ENABLE_PROFILE_PARTICLES = BUILDER.comment("Show brief decorative Minecraft particles when using a profile artifact.").define("profileParticles", true);
        BUILDER.pop();
        SPEC = BUILDER.build();

        ForgeConfigSpec.Builder client = new ForgeConfigSpec.Builder();
        client.push("experience_simulation_accessibility");
        CLIENT_SIMULATION_ENABLED = client.comment("Master switch for the local fictional perception simulation.").define("enabled", true);
        CLIENT_VISUAL_EFFECTS = client.comment("Enable the low-intensity tint, geometric overlays, and visual layering.").define("visualEffects", true);
        CLIENT_CAMERA_EFFECTS = client.comment("Enable very small camera angle feedback. Off by default.").define("cameraEffects", false);
        CLIENT_REDUCE_MOTION = client.comment("Reduce animated overlays and disable all simulated camera motion.").define("reduceMotion", false);
        CLIENT_VISUAL_TRAILS = client.comment("Enable low-density edge trails and ghost layers.").define("visualTrails", true);
        CLIENT_CHROMATIC_FRINGE = client.comment("Enable restrained colored edge separation cues.").define("chromaticFringe", true);
        CLIENT_AUDIO_EFFECTS = client.comment("Enable quiet attenuation, slight pitch filtering, spatial bias, and occasional soft echoes. Music and UI sounds are excluded.").define("audioEffects", false);
        CLIENT_HUD_FEEDBACK = client.comment("Show small profile and recovery feedback away from essential controls.").define("hudFeedback", true);
        CLIENT_PREVENT_FLASHING = client.comment("Prevent rapid brightness/color changes; all profile animation is slow when enabled.").define("preventFlashing", true);
        CLIENT_INSTANT_RESET = client.comment("Allow the configurable Clear Experience key to immediately remove all local presentation effects.").define("instantReset", true);
        CLIENT_INTENSITY = client.comment("Maximum local visual/audio presentation intensity; 0 disables the presentation layer.").defineInRange("intensity", 0.8, 0.0, 1.0);
        CLIENT_TRANSITION_SCALE = client.comment("Scale onset and recovery transition length; higher values are slower.").defineInRange("transitionScale", 1.0, 0.25, 4.0);
        client.pop();
        CLIENT_SPEC = client.build();
    }
    private ChemLabConfig() { }
}
