package com.barklylabs.chemlab.compat.jei;

import com.barklylabs.chemlab.BarklyChemLab;
import com.barklylabs.chemlab.recipe.ResearchRecipe;
import com.barklylabs.chemlab.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class ResearchRecipeCategory implements IRecipeCategory<ResearchRecipe> {
    public static final RecipeType<ResearchRecipe> TYPE = RecipeType.create(BarklyChemLab.MOD_ID, "research", ResearchRecipe.class);
    private final IDrawableStatic background;
    private final IDrawable icon;
    public ResearchRecipeCategory(IGuiHelper helper) {
        background = helper.createBlankDrawable(124, 54);
        icon = helper.createDrawableItemStack(new ItemStack(ModBlocks.RESEARCH_WORKBENCH.get()));
    }
    @Override public RecipeType<ResearchRecipe> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return Component.translatable("jei.category.barklychemlab.research"); }
    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }
    @Override public int getWidth() { return 124; }
    @Override public int getHeight() { return 54; }
    @Override public void setRecipe(IRecipeLayoutBuilder builder, ResearchRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(0, 7).setStandardSlotBackground().addIngredients(recipe.first());
        builder.addInputSlot(25, 7).setStandardSlotBackground().addIngredients(recipe.second());
        builder.addOutputSlot(99, 7).setOutputSlotBackground().addItemStack(recipe.result());
    }
    @Override public void draw(ResearchRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        graphics.fill(0, 0, getWidth(), getHeight(), 0xFF281D37);
        graphics.fill(0, 0, getWidth(), 2, 0xFFE76BB5);
        graphics.fill(0, getHeight() - 2, getWidth(), getHeight(), 0xFF8855AF);
        graphics.fill(57, 13, 88, 15, 0xFFD36DB5);
        graphics.fill(80, 10, 88, 18, 0xFFD36DB5);
        graphics.fill(57, 16, 88, 18, 0xFFD36DB5);
        graphics.drawString(Minecraft.getInstance().font, Component.translatable("jei.category.barklychemlab.work", String.format("%.1fs", recipe.work() / 20.0F)), 4, 38, 0xFFE8D9EF, false);
    }
}
