package com.barklylabs.chemlab.client;

import com.barklylabs.chemlab.BarklyChemLab;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

/** Resource-reloadable catalog for bounded additive modifiers; data never controls rendering code. */
public final class ExperienceModifierCatalog extends SimplePreparableReloadListener<Map<String, ExperienceAdditive>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile Map<String, ExperienceAdditive> modifiers = Map.of();

    @Override protected Map<String, ExperienceAdditive> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<String, ExperienceAdditive> result = new TreeMap<>();
        manager.listResources("experience_modifiers", location -> location.getPath().endsWith(".json")
                        && location.getNamespace().equals(BarklyChemLab.MOD_ID))
                .forEach((location, resource) -> {
                    try (InputStreamReader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                        ExperienceAdditive modifier = ExperienceAdditive.fromJson(JsonParser.parseReader(reader).getAsJsonObject());
                        String key = normalize(modifier.id());
                        if (!location.getPath().equals("experience_modifiers/" + key + ".json"))
                            throw new IllegalArgumentException("Modifier id must match resource path");
                        result.put(key, modifier);
                    } catch (Exception exception) {
                        LOGGER.error("Skipping invalid ChemCraft experience modifier {}: {}", location, exception.getMessage());
                    }
                });
        return Map.copyOf(result);
    }

    @Override protected void apply(Map<String, ExperienceAdditive> loaded, ResourceManager manager, ProfilerFiller profiler) {
        modifiers = loaded;
        LOGGER.info("Loaded {} ChemCraft experience modifiers", loaded.size());
    }
    public static ExperienceAdditive get(String id) { return modifiers.get(normalize(id)); }
    public static int size() { return modifiers.size(); }
    private static String normalize(String id) { return id.startsWith(BarklyChemLab.MOD_ID + ":") ? id.substring(BarklyChemLab.MOD_ID.length() + 1) : id; }
}
