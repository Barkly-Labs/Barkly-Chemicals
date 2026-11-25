package com.barklylabs.chemlab.compat.jei;

import com.barklylabs.chemlab.BarklyChemLab;
import com.barklylabs.chemlab.recipe.ModRecipes;
import com.barklylabs.chemlab.registry.ModBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.resources.ResourceLocation;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.barklylabs.chemlab.recipe.ResearchRecipe;
import java.io.Reader;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public final class ResearchJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = new ResourceLocation(BarklyChemLab.MOD_ID, "research_jei");
    @Override public ResourceLocation getPluginUid() { return UID; }
    @Override public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ResearchRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }
    @Override public void registerRecipes(IRecipeRegistration registration) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            registration.addRecipes(ResearchRecipeCategory.TYPE, recipesFromAssetCatalog());
            return;
        }
        registration.addRecipes(ResearchRecipeCategory.TYPE, new ArrayList<>(connection.getRecipeManager().getAllRecipesFor(ModRecipes.RESEARCH_TYPE.get())));
    }
    @Override public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModBlocks.RESEARCH_WORKBENCH.get(), ResearchRecipeCategory.TYPE);
    }

    private static List<ResearchRecipe> recipesFromAssetCatalog() {
        ResourceLocation catalogId = new ResourceLocation(BarklyChemLab.MOD_ID, "jei/research_recipes.json");
        var resource = Minecraft.getInstance().getResourceManager().getResource(catalogId);
        if (resource.isEmpty()) return List.of();
        try (Reader reader = resource.get().openAsReader()) {
            JsonArray entries = JsonParser.parseReader(reader).getAsJsonArray();
            List<ResearchRecipe> recipes = new ArrayList<>();
            for (var value : entries) {
                JsonObject json = value.getAsJsonObject();
                ResourceLocation id = new ResourceLocation(json.get("id").getAsString());
                recipes.add(ModRecipes.RESEARCH_SERIALIZER.get().fromJson(id, json));
            }
            return recipes;
        } catch (Exception ignored) {
            return List.of();
        }
    }
}
