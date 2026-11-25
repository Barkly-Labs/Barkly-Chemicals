package com.barklylabs.chemlab.client;

import com.google.gson.JsonObject;
import java.util.Map;
import java.util.TreeMap;

/** Fictional, bounded presentation modifier loaded from the experience_modifiers resource directory. */
public record ExperienceAdditive(String id, String label, Map<String, Float> channels,
                                float intensityMultiplier, int tint, int durationTicks) {
    public ExperienceAdditive {
        channels = Map.copyOf(channels);
        intensityMultiplier = clamp(intensityMultiplier, 0.5f, 1.5f);
        durationTicks = Math.max(20, Math.min(12000, durationTicks));
        tint &= 0xFFFFFF;
    }
    public float channel(String id) { return channels.getOrDefault(id, 0f); }

    public static ExperienceAdditive fromJson(JsonObject json) {
        String id = json.get("id").getAsString();
        String label = json.has("label") ? json.get("label").getAsString() : id.replace('_', ' ');
        TreeMap<String, Float> channels = new TreeMap<>();
        if (json.has("channels") && json.get("channels").isJsonObject()) {
            JsonObject values = json.getAsJsonObject("channels");
            for (String key : values.keySet()) {
                if (!key.matches("[a-z][a-z0-9_]{0,39}")) continue;
                channels.put(key, clamp(values.get(key).getAsFloat(), -0.5f, 0.5f));
            }
        }
        String color = json.has("tint") ? json.get("tint").getAsString().replace("#", "") : "FFFFFF";
        if (color.length() != 6) throw new IllegalArgumentException("Invalid modifier tint for " + id);
        return new ExperienceAdditive(id, label, channels,
                json.has("intensity_multiplier") ? json.get("intensity_multiplier").getAsFloat() : 1f,
                Integer.parseInt(color, 16), json.has("duration_ticks") ? json.get("duration_ticks").getAsInt() : 1200);
    }
    private static float clamp(float value, float min, float max) {
        return Float.isFinite(value) ? Math.max(min, Math.min(max, value)) : min;
    }
}
