package com.barklylabs.chemlab.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class ExperienceProfileCatalog extends SimplePreparableReloadListener<Map<String, ExperienceProfile>> {
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
    }

    public static ExperienceProfile get(String id) {
        return profiles.get(normalize(id));
    }

    public static int size() {
        return profiles.size();
    }

    private static String normalize(String id) {
        return id.startsWith("barklychemlab:") ? id.substring("barklychemlab:".length()) : id;
    }
}
