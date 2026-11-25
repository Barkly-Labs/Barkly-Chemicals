package com.barklylabs.chemlab.registry;

import com.barklylabs.chemlab.BarklyChemLab;
import com.barklylabs.chemlab.world.ResearchWorkbenchBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BarklyChemLab.MOD_ID);
    public static final DeferredRegister<Item> BLOCK_ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BarklyChemLab.MOD_ID);
    public static final RegistryObject<Block> RESEARCH_WORKBENCH = BLOCKS.register("research_workbench", ResearchWorkbenchBlock::new);
    static { BLOCK_ITEMS.register("research_workbench", () -> new BlockItem(RESEARCH_WORKBENCH.get(), new Item.Properties())); }
    private ModBlocks() { }
}
