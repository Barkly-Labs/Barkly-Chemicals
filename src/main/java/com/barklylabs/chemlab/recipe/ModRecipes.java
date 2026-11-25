package com.barklylabs.chemlab.recipe;

import com.barklylabs.chemlab.BarklyChemLab;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, BarklyChemLab.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, BarklyChemLab.MOD_ID);
    public static final RegistryObject<RecipeType<ResearchRecipe>> RESEARCH_TYPE = TYPES.register("research", () -> new RecipeType<>() { @Override public String toString() { return BarklyChemLab.MOD_ID + ":research"; } });
    public static final RegistryObject<RecipeSerializer<ResearchRecipe>> RESEARCH_SERIALIZER = SERIALIZERS.register("research", Serializer::new);

    public static final class Serializer implements RecipeSerializer<ResearchRecipe> {
        @Override public ResearchRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient first = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "first"));
            Ingredient second = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "second"));
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int work = GsonHelper.getAsInt(json, "work", 100);
            return new ResearchRecipe(id, first, second, output, work);
        }
        @Override public ResearchRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            Ingredient first = Ingredient.fromNetwork(buffer);
            Ingredient second = Ingredient.fromNetwork(buffer);
            ItemStack result = buffer.readItem();
            return new ResearchRecipe(id, first, second, result, buffer.readVarInt());
        }
        @Override public void toNetwork(FriendlyByteBuf buffer, ResearchRecipe recipe) {
            recipe.first().toNetwork(buffer);
            recipe.second().toNetwork(buffer);
            buffer.writeItem(recipe.result());
            buffer.writeVarInt(recipe.work());
        }
    }
    private ModRecipes() { }
}
