package com.barklylabs.chemlab.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

/** Immutable, validated presentation data loaded from an experience profile resource. */
public record ExperienceProfile(
        String id, String rationale, String reference,
        int color, float strength, float trail, float fringe, float warp,
        float cameraSway, float cameraRoll, float movement,
        float muffle, float echo, float pan, float pitch,
        float hud, String pattern,
        int onsetTicks, int peakTicks, int recoveryTicks,
        List<ExperienceVisualLayer> layers
) {
    public ExperienceProfile {
        layers = List.copyOf(layers);
    }

    public static ExperienceProfile fromJson(JsonObject json) {
        String id = json.get("id").getAsString();
        String pattern = json.has("pattern") ? json.get("pattern").getAsString() : "none";
        float strength = number(json, "strength", 0.35f);
        int color = parseColor(json.has("color") ? json.get("color").getAsString() : "#ffffff");
        List<ExperienceVisualLayer> layers = new ArrayList<>();
        if (json.has("layers") && json.get("layers").isJsonArray()) {
            JsonArray values = json.getAsJsonArray("layers");
            for (int i = 0; i < Math.min(16, values.size()); i++) {
                if (values.get(i).isJsonObject()) layers.add(ExperienceVisualLayer.fromJson(values.get(i).getAsJsonObject(), i, color));
            }
        }
        // Backward compatibility: older packs may still provide only the v1 pattern fields.
        if (layers.isEmpty()) {
            layers.add(new ExperienceVisualLayer("edge", "edge_tint", 10, strength, 1f, 1f, 0f, color, "visual"));
            if (!pattern.equals("none")) layers.add(new ExperienceVisualLayer("motif", "motif", 20, 0.55f, 0.8f, 1f, 0f, color, "hud"));
            if (number(json, "trail", 0) > 0) layers.add(new ExperienceVisualLayer("trail", "motion_trail", 30, number(json, "trail", 0), 0.35f, 1f, 0.6f, color, "trails"));
        }
        return new ExperienceProfile(
                id,
                json.has("rationale") ? json.get("rationale").getAsString() : "Fictional visual profile.",
                json.has("reference") ? json.get("reference").getAsString() : "",
                color, strength, number(json, "trail", 0), number(json, "fringe", 0), number(json, "warp", 0),
                number(json, "camera_sway", 0), number(json, "camera_roll", 0), number(json, "movement", 0),
                number(json, "muffle", 0), number(json, "echo", 0), number(json, "pan", 0), number(json, "pitch", 0),
                number(json, "hud", 0), pattern,
                integer(json, "onset_ticks", 80), integer(json, "peak_ticks", 240), integer(json, "recovery_ticks", 160),
                layers
        );
    }

    private static int parseColor(String value) {
        String digits = value.startsWith("#") ? value.substring(1) : value;
        if (digits.length() != 6) throw new IllegalArgumentException("Expected a six-digit RGB color");
        return Integer.parseInt(digits, 16) & 0xFFFFFF;
    }

    private static float number(JsonObject json, String key, float fallback) {
        return json.has(key) ? clamp(json.get(key).getAsFloat(), -1f, 1f) : fallback;
    }
    private static int integer(JsonObject json, String key, int fallback) {
        return json.has(key) ? Math.max(1, Math.min(12000, json.get(key).getAsInt())) : fallback;
    }
    private static float clamp(float value, float min, float max) { return Float.isFinite(value) ? Math.max(min, Math.min(max, value)) : min; }
}
