package com.barklylabs.chemlab.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class ExperienceProfileCatalog extends SimplePreparableReloadListener<Map<String, ExperienceProfile>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile Map<String, ExperienceProfile> profiles = Map.of();

    @Override
    protected Map<String, ExperienceProfile> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<String, ExperienceProfile> loaded = new HashMap<>();
        manager.listResources("experience_profiles", location -> location.getPath().endsWith(".json"))
                .forEach((location, resource) -> {
                    try (InputStreamReader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                        ExperienceProfile profile = ExperienceProfile.fromJson(json);
                        String key = normalize(profile.id());
                        loaded.put(key, profile);
                    } catch (Exception exception) {
                        System.err.println("[Barkly Chem Lab] Skipping invalid experience profile " + location + ": " + exception.getMessage());
                    }
                });
        return Map.copyOf(loaded);
    }

    @Override
    protected void apply(Map<String, ExperienceProfile> loaded, ResourceManager manager, ProfilerFiller profiler) {
        profiles = loaded;
        if (loaded.isEmpty()) LOGGER.error("ChemCraft experience profile reload found no profiles under assets/*/experience_profiles/*.json");
        else LOGGER.info("ChemCraft loaded {} client experience profiles", loaded.size());
    }

    public static ExperienceProfile get(String id) {
        String key = normalize(id);
        ExperienceProfile profile = profiles.get(key);
        if (profile != null) return profile;
        // Packet delivery can race the first resource reload, or a modpack can omit
        // this listener. Resolve the requested resource directly instead of silently
        // discarding the whole client-side effect.
        ResourceLocation location = new ResourceLocation("barklychemlab", "experience_profiles/" + key + ".json");
        try {
            var resource = Minecraft.getInstance().getResourceManager().getResource(location);
            if (resource.isPresent()) {
                try (InputStreamReader reader = new InputStreamReader(resource.get().open(), StandardCharsets.UTF_8)) {
                    ExperienceProfile loaded = ExperienceProfile.fromJson(JsonParser.parseReader(reader).getAsJsonObject());
                    Map<String, ExperienceProfile> updated = new HashMap<>(profiles);
                    updated.put(key, loaded);
                    profiles = Map.copyOf(updated);
                    LOGGER.warn("Loaded experience profile {} directly because it was absent from the reload catalog", key);
                    return loaded;
                }
            }
        } catch (Exception exception) {
            LOGGER.error("Could not load client experience profile {} from {}", key, location, exception);
        }
        LOGGER.error("No client experience profile exists for '{}'; client rendering layer will not start", key);
        return null;
    }

    public static int size() {
        return profiles.size();
    }

    private static String normalize(String id) {
        return id.startsWith("barklychemlab:") ? id.substring("barklychemlab:".length()) : id;
    }
}
