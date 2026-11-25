package com.barklylabs.chemlab.menu;

import com.barklylabs.chemlab.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import com.barklylabs.chemlab.registry.ModBlocks;
import com.barklylabs.chemlab.world.ResearchWorkbenchBlockEntity;

public class ResearchWorkbenchMenu extends AbstractContainerMenu {
    private final Container input;
    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final BlockPos pos;

    public ResearchWorkbenchMenu(int id, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(id, playerInventory, new SimpleContainer(3), new SimpleContainerData(2), buffer.readBlockPos());
    }
    public ResearchWorkbenchMenu(int id, Inventory playerInventory, ResearchWorkbenchBlockEntity workbench, BlockPos pos) {
        this(id, playerInventory, workbench, workbench, pos);
    }
    public ResearchWorkbenchMenu(int id, Inventory playerInventory, Container input, ContainerData data, BlockPos pos) {
        super(ModMenus.RESEARCH.get(), id);
        checkContainerSize(input, 3);
        checkContainerDataCount(data, 2);
        this.input = input;
        this.data = data;
        this.pos = pos;
        this.access = ContainerLevelAccess.create(playerInventory.player.level(), pos);
        input.startOpen(playerInventory.player);
        addSlot(new Slot(input, 0, 28, 37));
        addSlot(new Slot(input, 1, 52, 37));
        addSlot(new Slot(input, 2, 132, 37) { @Override public boolean mayPlace(ItemStack stack) { return false; } });
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        addDataSlots(data);
    }
    @Override public boolean stillValid(Player player) {
        return access.evaluate((level, blockPos) -> level.getBlockState(blockPos).is(ModBlocks.RESEARCH_WORKBENCH.get()) && player.distanceToSqr(Vec3.atCenterOf(blockPos)) <= 64.0, true);
    }
    public int getProgressScaled() { int max = Math.max(1, data.get(1)); return Math.min(24, data.get(0) * 24 / max); }
    public BlockPos getBlockPos() { return pos; }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        ItemStack moved = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack current = slot.getItem(); moved = current.copy();
            if (index == 2) {
                if (!moveItemStackTo(current, 3, 39, true)) return ItemStack.EMPTY;
            } else if (index < 3) {
                if (!moveItemStackTo(current, 3, 39, false)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(current, 0, 2, false)) {
                if (index < 30) { if (!moveItemStackTo(current, 30, 39, false)) return ItemStack.EMPTY; }
                else if (!moveItemStackTo(current, 3, 30, false)) return ItemStack.EMPTY;
            }
            if (current.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
            if (current.getCount() == moved.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, current);
        }
        return moved;
    }
}
