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
    }
    private ChemLabConfig() { }
}
