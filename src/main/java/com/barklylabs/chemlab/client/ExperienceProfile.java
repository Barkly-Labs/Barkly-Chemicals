package com.barklylabs.chemlab.client;

import com.google.gson.JsonObject;

public record ExperienceProfile(
        String id, String rationale, String reference,
        int color, float strength, float trail, float fringe, float warp,
        float cameraSway, float cameraRoll, float movement,
        float muffle, float echo, float pan, float pitch,
        float hud, String pattern,
        int onsetTicks, int peakTicks, int recoveryTicks
) {
    public static ExperienceProfile fromJson(JsonObject json) {
        return new ExperienceProfile(
                json.get("id").getAsString(),
                json.has("rationale") ? json.get("rationale").getAsString() : "Fictional visual profile.",
                json.has("reference") ? json.get("reference").getAsString() : "",
                parseColor(json.has("color") ? json.get("color").getAsString() : "#ffffff"),
                number(json, "strength", 0.35f), number(json, "trail", 0),
                number(json, "fringe", 0), number(json, "warp", 0),
                number(json, "camera_sway", 0), number(json, "camera_roll", 0),
                number(json, "movement", 0), number(json, "muffle", 0),
                number(json, "echo", 0), number(json, "pan", 0),
                number(json, "pitch", 0), number(json, "hud", 0),
                json.has("pattern") ? json.get("pattern").getAsString() : "none",
                integer(json, "onset_ticks", 80), integer(json, "peak_ticks", 240),
                integer(json, "recovery_ticks", 160)
        );
    }

    private static int parseColor(String value) {
        String digits = value.startsWith("#") ? value.substring(1) : value;
        return Integer.parseInt(digits, 16) & 0xFFFFFF;
    }

    private static float number(JsonObject json, String key, float fallback) {
        return json.has(key) ? Math.max(-1.0f, Math.min(1.0f, json.get(key).getAsFloat())) : fallback;
    }

    private static int integer(JsonObject json, String key, int fallback) {
        return json.has(key) ? Math.max(1, Math.min(12000, json.get(key).getAsInt())) : fallback;
    }
}
