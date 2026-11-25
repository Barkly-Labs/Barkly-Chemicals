package com.barklylabs.chemlab.item;

import com.barklylabs.chemlab.config.ChemLabConfig;
import com.barklylabs.chemlab.network.ChemLabNetwork;
import com.barklylabs.chemlab.network.StartExperiencePacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class ExperienceTokenItem extends Item {
    private final String profileId;
    private final MobEffect effect;
    private final String messageKey;
    private final int durationScale;
    private final ParticleOptions particle;

    public ExperienceTokenItem(Properties properties, String profileId, MobEffect effect, String messageKey, int durationScale, ParticleOptions particle) {
        super(properties);
        this.profileId = profileId;
        this.effect = effect;
        this.messageKey = messageKey;
        this.durationScale = durationScale;
        this.particle = particle;
    }

    @Override public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, level, lines, flag);
        lines.add(Component.translatable("tooltip.barklychemlab.experience_token"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            if (ChemLabConfig.ENABLE_EXPERIENCE_EFFECTS.get()) {
                int duration = Math.max(20, Math.min(6000, ChemLabConfig.PROFILE_DURATION_TICKS.get() * durationScale / 10));
                player.addEffect(new MobEffectInstance(effect, duration, 0, false, false, true));
                player.getPersistentData().putInt("barklychemlab_experience_until", player.tickCount + duration + 1200);
                if (player instanceof ServerPlayer serverPlayer) {
                    ChemLabNetwork.sendToPlayer(serverPlayer, new StartExperiencePacket(profileId, duration));
                }
            }
            player.displayClientMessage(Component.translatable(messageKey), true);
            if (ChemLabConfig.ENABLE_PROFILE_PARTICLES.get() && level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(particle, player.getX(), player.getY() + 1.0D, player.getZ(), 18, 0.35D, 0.45D, 0.35D, 0.02D);
            }
            int cooldown = ChemLabConfig.TOKEN_COOLDOWN_TICKS.get();
            if (cooldown > 0) player.getCooldowns().addCooldown(this, cooldown);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
