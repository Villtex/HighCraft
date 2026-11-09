package de.highcraft.block.entity;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.function.IntPredicate;

/**
 * View for hoppers/pipes: insert only into valid input slots, extract only from allowed slots.
 */
public class AutomationItemHandler implements IItemHandler {
    private final ItemStackHandler inner;
    private final IntPredicate canExtract;

    public AutomationItemHandler(ItemStackHandler inner, IntPredicate canExtract) {
        this.inner = inner;
        this.canExtract = canExtract;
    }

    @Override public int getSlots() { return inner.getSlots(); }
    @Override public ItemStack getStackInSlot(int slot) { return inner.getStackInSlot(slot); }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        // ItemStackHandler#insertItem already checks isItemValid (= recipe scan) - don't run it twice per hopper insert
        return inner.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!canExtract.test(slot)) return ItemStack.EMPTY;
        return inner.extractItem(slot, amount, simulate);
    }

    @Override public int getSlotLimit(int slot) { return inner.getSlotLimit(slot); }
    @Override public boolean isItemValid(int slot, ItemStack stack) { return inner.isItemValid(slot, stack); }
}
