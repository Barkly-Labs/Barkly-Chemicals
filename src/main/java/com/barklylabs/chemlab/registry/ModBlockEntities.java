package com.barklylabs.chemlab.registry;

import com.barklylabs.chemlab.BarklyChemLab;
import com.barklylabs.chemlab.world.ResearchWorkbenchBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BarklyChemLab.MOD_ID);
    public static final RegistryObject<BlockEntityType<ResearchWorkbenchBlockEntity>> RESEARCH_WORKBENCH = TYPES.register("research_workbench", () -> BlockEntityType.Builder.of(ResearchWorkbenchBlockEntity::new, ModBlocks.RESEARCH_WORKBENCH.get()).build(null));
    private ModBlockEntities() { }
}
