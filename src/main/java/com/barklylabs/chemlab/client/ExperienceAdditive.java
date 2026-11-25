package com.barklylabs.chemlab.client;

import java.util.Map;

/** Fictional modifiers for presentation layers; these never modify player controls or server stats. */
public record ExperienceAdditive(String id, String label, float trail, float fringe, float warp, float muffle, float echo, float movement, float intensityMultiplier, int tint) {
    private static final Map<String, ExperienceAdditive> ALL = Map.of(
            "prism_salt", new ExperienceAdditive("prism_salt", "Prism Salt", .18f, .34f, .14f, 0, 0, 0, 1.10f, 0xD49EFF),
            "echo_dust", new ExperienceAdditive("echo_dust", "Echo Dust", .32f, .04f, .12f, .04f, .28f, 0, 1.04f, 0x83CFEA),
            "lumen_resin", new ExperienceAdditive("lumen_resin", "Lumen Resin", .04f, .08f, .08f, 0, 0, .04f, 1.08f, 0xF2D77A),
            "stillwater_essence", new ExperienceAdditive("stillwater_essence", "Stillwater Essence", 0, 0, -.05f, .12f, 0, -.10f, .78f, 0x72BBAF)
    );
    public static ExperienceAdditive byId(String id) { return ALL.get(id); }
    public static java.util.Collection<ExperienceAdditive> all() { return ALL.values(); }
}
