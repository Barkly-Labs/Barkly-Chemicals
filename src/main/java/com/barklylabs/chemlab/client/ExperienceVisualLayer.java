package com.barklylabs.chemlab.client;

import com.google.gson.JsonObject;

/** Declarative render layer. The type selects a registered client renderer; data stays code-free. */
public record ExperienceVisualLayer(String id, String type, int order, float intensity, float opacity,
                                    float speed, float scale, int color, String accessibility) {
    private static final int MAX_ORDER = 10_000;

    public static ExperienceVisualLayer fromJson(JsonObject json, int index, int profileColor) {
        String id = text(json, "id", "layer_" + index);
        String type = text(json, "type", "motif");
        return new ExperienceVisualLayer(id, type, integer(json, "order", index * 10),
                number(json, "intensity", 0.5f, 0f, 1f), number(json, "opacity", 0.5f, 0f, 1f),
                number(json, "speed", 1f, 0f, 4f), number(json, "scale", 1f, 0.25f, 4f),
                json.has("color") ? parseColor(json.get("color").getAsString()) : profileColor,
                text(json, "accessibility", "visual"));
    }

    private static String text(JsonObject json, String key, String fallback) {
        return json.has(key) ? json.get(key).getAsString() : fallback;
    }
    private static int integer(JsonObject json, String key, int fallback) {
        return json.has(key) ? Math.max(-MAX_ORDER, Math.min(MAX_ORDER, json.get(key).getAsInt())) : fallback;
    }
    private static float number(JsonObject json, String key, float fallback, float min, float max) {
        if (!json.has(key)) return fallback;
        float value = json.get(key).getAsFloat();
        return Float.isFinite(value) ? Math.max(min, Math.min(max, value)) : fallback;
    }
    private static int parseColor(String value) {
        String digits = value.startsWith("#") ? value.substring(1) : value;
        if (digits.length() != 6) throw new IllegalArgumentException("Expected a six-digit layer RGB color");
        return Integer.parseInt(digits, 16) & 0xFFFFFF;
    }
}
