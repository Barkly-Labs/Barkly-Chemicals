package com.barklylabs.chemlab.world;

import com.barklylabs.chemlab.recipe.ModRecipes;
import com.barklylabs.chemlab.recipe.ResearchRecipe;
import com.barklylabs.chemlab.registry.ModBlockEntities;
import com.barklylabs.chemlab.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class ResearchWorkbenchBlockEntity extends BlockEntity implements Container, ContainerData, MenuProvider {
    private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    private int progress;
    private int duration = 100;
    private String activeRecipeId = "";

    public ResearchWorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESEARCH_WORKBENCH.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ResearchWorkbenchBlockEntity be) {
        if (level.isClientSide) return;
        Optional<ResearchRecipe> recipe = level.getRecipeManager().getRecipeFor(ModRecipes.RESEARCH_TYPE.get(), be, level);
        if (recipe.isEmpty() || !be.hasOutputSpace(recipe.get())) {
            if (be.progress != 0 || !be.activeRecipeId.isEmpty()) { be.progress = 0; be.activeRecipeId = ""; be.setChanged(); }
            return;
        }
        ResearchRecipe active = recipe.get();
        String nextRecipeId = active.getId().toString();
        if (!nextRecipeId.equals(be.activeRecipeId)) { be.progress = 0; be.activeRecipeId = nextRecipeId; }
        be.duration = active.work();
        be.progress++;
        if (be.progress >= be.duration) {
            ItemStack result = active.getResultItem(level.registryAccess()).copy();
            ItemStack output = be.items.get(2);
            if (output.isEmpty()) be.items.set(2, result);
            else output.grow(result.getCount());
            be.items.get(0).shrink(1);
            be.items.get(1).shrink(1);
            be.progress = 0;
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        } else if ((be.progress & 3) == 0) {
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 2);
        }
    }

    private boolean hasOutputSpace(ResearchRecipe recipe) {
        ItemStack result = recipe.getResultItem(level.registryAccess());
        ItemStack output = items.get(2);
        return output.isEmpty() || (ItemStack.isSameItemSameTags(output, result) && output.getCount() + result.getCount() <= Math.min(output.getMaxStackSize(), result.getMaxStackSize()));
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putInt("Progress", progress);
        tag.putInt("Duration", duration);
        tag.putString("ActiveRecipe", activeRecipeId);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, items);
        progress = tag.getInt("Progress");
        duration = Math.max(20, tag.getInt("Duration"));
        activeRecipeId = tag.getString("ActiveRecipe");
    }
    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) { if (packet.getTag() != null) load(packet.getTag()); }
    @Override public int getContainerSize() { return items.size(); }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) { ItemStack result = ContainerHelper.removeItem(items, slot, amount); if (!result.isEmpty()) setChanged(); return result; }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }
    @Override public void setItem(int slot, ItemStack stack) { items.set(slot, stack); if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize()); setChanged(); }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return slot < 2; }
    @Override public void setChanged() { super.setChanged(); if (level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2); }
    @Override public boolean stillValid(Player player) { return Container.stillValidBlockEntity(this, player); }
    @Override public void clearContent() { for (int i = 0; i < items.size(); i++) items.set(i, ItemStack.EMPTY); setChanged(); }
    @Override public Component getDisplayName() { return Component.translatable("container.barklychemlab.research_workbench"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new com.barklylabs.chemlab.menu.ResearchWorkbenchMenu(id, inventory, this, worldPosition); }
    @Override public int get(int index) { return index == 0 ? progress : duration; }
    @Override public void set(int index, int value) { if (index == 0) progress = value; else duration = Math.max(20, value); }
    @Override public int getCount() { return 2; }
}
