package com.barklylabs.chemlab.event;

import com.barklylabs.chemlab.BarklyChemLab;
import com.barklylabs.chemlab.config.ChemLabConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = BarklyChemLab.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AdvancementReactions {
    private static final Map<String, List<String>> REACTIONS = Map.of(
            "fieldwork", List.of("reaction.barklychemlab.fieldwork.1", "reaction.barklychemlab.fieldwork.2", "reaction.barklychemlab.fieldwork.3"),
            "calibration", List.of("reaction.barklychemlab.calibration.1", "reaction.barklychemlab.calibration.2", "reaction.barklychemlab.calibration.3"),
            "first_insight", List.of("reaction.barklychemlab.insight.1", "reaction.barklychemlab.insight.2", "reaction.barklychemlab.insight.3"),
            "experience_collection", List.of("reaction.barklychemlab.profiles.1", "reaction.barklychemlab.profiles.2", "reaction.barklychemlab.profiles.3"),
            "expanded_archive", List.of("reaction.barklychemlab.archive.1", "reaction.barklychemlab.archive.2", "reaction.barklychemlab.archive.3"),
            "dimension_survey", List.of("reaction.barklychemlab.dimension.1", "reaction.barklychemlab.dimension.2", "reaction.barklychemlab.dimension.3"),
            "outer_ledger", List.of("reaction.barklychemlab.outer.1", "reaction.barklychemlab.outer.2", "reaction.barklychemlab.outer.3")
    );

    @SubscribeEvent public static void onResearchChallenge(AdvancementEvent.AdvancementEarnEvent event) {
        if (!ChemLabConfig.ENABLE_ADVANCEMENT_REACTIONS.get() || !(event.getEntity() instanceof ServerPlayer player)) return;
        ResourceLocation id = event.getAdvancement().getId();
        if (!BarklyChemLab.MOD_ID.equals(id.getNamespace())) return;
        List<String> messages = REACTIONS.get(id.getPath().replace("research/", ""));
        if (messages == null || messages.isEmpty()) return;
        String key = messages.get(player.getRandom().nextInt(messages.size()));
        player.displayClientMessage(Component.translatable(key), false);
        if (ChemLabConfig.ENABLE_REACTION_SOUNDS.get()) {
            player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.45F, 1.1F);
        }
    }
    private AdvancementReactions() { }
}
