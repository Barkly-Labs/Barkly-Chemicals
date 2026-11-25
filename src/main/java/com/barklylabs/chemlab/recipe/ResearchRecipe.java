package com.barklylabs.chemlab.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public final class ResearchRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final Ingredient first;
    private final Ingredient second;
    private final ItemStack result;
    private final int work;

    public ResearchRecipe(ResourceLocation id, Ingredient first, Ingredient second, ItemStack result, int work) {
        this.id = id;
        this.first = first;
        this.second = second;
        this.result = result.copy();
        this.work = Math.max(20, Math.min(1200, work));
    }
    public Ingredient first() { return first; }
    public Ingredient second() { return second; }
    public int work() { return work; }
    public ItemStack result() { return result.copy(); }
    @Override public boolean matches(Container container, Level level) {
        if (container.getContainerSize() < 2) return false;
        ItemStack a = container.getItem(0), b = container.getItem(1);
        return (!a.isEmpty() && !b.isEmpty() && first.test(a) && second.test(b)) || (!a.isEmpty() && !b.isEmpty() && first.test(b) && second.test(a));
    }
    @Override public ItemStack assemble(Container container, RegistryAccess registryAccess) { return result.copy(); }
    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= 2; }
    @Override public ItemStack getResultItem(RegistryAccess registryAccess) { return result.copy(); }
    @Override public ResourceLocation getId() { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.RESEARCH_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return ModRecipes.RESEARCH_TYPE.get(); }
    @Override public NonNullList<Ingredient> getIngredients() { return NonNullList.of(Ingredient.EMPTY, first, second); }
}
