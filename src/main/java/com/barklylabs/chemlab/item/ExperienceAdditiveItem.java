package com.barklylabs.chemlab.item;

import com.barklylabs.chemlab.network.ApplyAdditivePacket;
import com.barklylabs.chemlab.network.ChemLabNetwork;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public final class ExperienceAdditiveItem extends Item {
    private final String additiveId;
    public ExperienceAdditiveItem(String id, Properties properties) { super(properties); this.additiveId = id; }
    @Override public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.barklychemlab.additive." + additiveId));
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            int activeUntil = player.getPersistentData().getInt("barklychemlab_experience_until");
            if (activeUntil > player.tickCount) {
                ChemLabNetwork.sendToPlayer(serverPlayer, new ApplyAdditivePacket(additiveId));
                if (!player.getAbilities().instabuild) stack.shrink(1);
                player.displayClientMessage(Component.translatable("message.barklychemlab.additive_applied", Component.translatable("item.barklychemlab." + additiveId)), true);
            } else {
                player.displayClientMessage(Component.translatable("message.barklychemlab.additive_requires_experience"), true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
