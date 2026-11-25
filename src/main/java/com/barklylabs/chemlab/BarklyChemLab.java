package com.barklylabs.chemlab;

import com.barklylabs.chemlab.config.ChemLabConfig;
import com.barklylabs.chemlab.registry.ModBlocks;
import com.barklylabs.chemlab.registry.ModCreativeTabs;
import com.barklylabs.chemlab.registry.ModBlockEntities;
import com.barklylabs.chemlab.registry.ModMenus;
import com.barklylabs.chemlab.recipe.ModRecipes;
import com.barklylabs.chemlab.registry.ModItems;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BarklyChemLab.MOD_ID)
public final class BarklyChemLab {
    public static final String MOD_ID = "barklychemlab";

    public BarklyChemLab() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(bus);
        ModBlocks.BLOCKS.register(bus);
        ModBlocks.BLOCK_ITEMS.register(bus);
        ModCreativeTabs.TABS.register(bus);
        ModBlockEntities.TYPES.register(bus);
        ModMenus.TYPES.register(bus);
        ModRecipes.TYPES.register(bus);
        ModRecipes.SERIALIZERS.register(bus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ChemLabConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(this);
    }
}
