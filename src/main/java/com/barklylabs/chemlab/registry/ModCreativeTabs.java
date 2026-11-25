package com.barklylabs.chemlab.registry;

import com.barklylabs.chemlab.BarklyChemLab;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BarklyChemLab.MOD_ID);
    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("research", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.barklychemlab.research"))
            .icon(() -> new ItemStack(ModBlocks.RESEARCH_WORKBENCH.get()))
            .displayItems((parameters, output) -> {
                // Equipment and records
                output.accept(ModItems.FIELD_NOTEBOOK.get());
                output.accept(ModItems.SAMPLE_VIAL.get());
                output.accept(ModItems.CALIBRATION_LENS.get());
                // Exploration, botanical, and experimental materials
                output.accept(ModItems.PRISM_POLLEN.get());
                output.accept(ModItems.STAR_PETALS.get());
                output.accept(ModItems.LUNAR_GLASS.get());
                output.accept(ModItems.ECHO_SHARD.get());
                output.accept(ModItems.SKY_SILK.get());
                // Intermediate research materials and profile artifacts
                output.accept(ModItems.RESONANCE_CORE.get());
                output.accept(ModItems.INSIGHT_FRAGMENT.get());
                output.accept(ModItems.LUMEN_BLOOM.get());
                output.accept(ModItems.ECHO_PRISM.get());
                output.accept(ModItems.ORBIT_MARBLE.get());
                output.accept(ModItems.HUSH_CHIME.get());
                output.accept(ModItems.WARMTH_CRYSTAL.get());
                output.accept(ModItems.KRATOM_ARCHIVE.get());
                output.accept(ModItems.DXM_ECHO.get());
                output.accept(ModItems.KETAMINE_GEOMETRY.get());
                output.accept(ModItems.NITROUS_CLOUD.get());
                output.accept(ModItems.MESCALINE_SUN.get());
                output.accept(ModItems.TWOCB_PRISM.get());
                output.accept(ModItems.MEPHEDRONE_SPARK.get());
                output.accept(ModItems.GHB_TIDE.get());
                output.accept(ModItems.BENZODIAZEPINE_LANTERN.get());
                output.accept(ModItems.BARBITURATE_ARCHIVE.get());
                output.accept(ModItems.OXYCODONE_MOON.get());
                output.accept(ModItems.DIPHENHYDRAMINE_MOTH.get());
                output.accept(ModItems.ALCOHOL_CAMPFIRE.get());
                output.accept(ModItems.PCP_MIRROR.get());
                output.accept(ModItems.METHYLPHENIDATE_CLOCKWORK.get());
                output.accept(ModBlocks.RESEARCH_WORKBENCH.get());
            }).build());
    private ModCreativeTabs() { }
}
