package com.barklylabs.chemlab.client;

import com.barklylabs.chemlab.BarklyChemLab;
import com.barklylabs.chemlab.menu.ResearchWorkbenchScreen;
import com.barklylabs.chemlab.registry.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = BarklyChemLab.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModClientEvents {
    @SubscribeEvent public static void registerScreens(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(ModMenus.RESEARCH.get(), ResearchWorkbenchScreen::new));
    }
    private ModClientEvents() { }
}
