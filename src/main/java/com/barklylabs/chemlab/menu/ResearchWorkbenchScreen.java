package com.barklylabs.chemlab.menu;

import com.barklylabs.chemlab.BarklyChemLab;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ResearchWorkbenchScreen extends AbstractContainerScreen<ResearchWorkbenchMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(BarklyChemLab.MOD_ID, "textures/gui/research_workbench.png");
    public ResearchWorkbenchScreen(ResearchWorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = imageHeight - 94;
    }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2, y = (height - imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
        int progress = menu.getProgressScaled();
        if (progress > 0) graphics.blit(TEXTURE, x + 80, y + 38, 0, 166, progress, 17, 256, 256);
    }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 7, 0xFFF2D9F0, false);
        graphics.drawString(font, Component.translatable("screen.barklychemlab.inputs"), 25, 23, 0xFFC7B8D4, false);
        graphics.drawString(font, Component.translatable("screen.barklychemlab.output"), 129, 23, 0xFFC7B8D4, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0xFFE2D7E8, false);
    }
}
