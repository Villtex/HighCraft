package de.highcraft.block.entity;

import de.highcraft.block.MachineBlock;
import de.highcraft.block.MachineType;
import de.highcraft.menu.MachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Shared base of all GUI machines: inventory, energy (FE), progress, ContainerData, saving.
 */
public abstract class AbstractMachineBlockEntity extends BlockEntity implements MenuProvider {
    protected final MachineType machine;
    protected final ItemStackHandler inventory;
    protected final MachineEnergyStorage energy;
    protected final AutomationItemHandler automation;

    protected int progress;
    protected int maxProgress = 1;
    protected int burnTime;
    protected int burnDuration;

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return getDataValue(index);
        }

        @Override
        public void set(int index, int value) {
            // The server is the source of truth
        }

        @Override
        public int getCount() {
            return MachineType.DATA_COUNT;
        }
    };

    protected AbstractMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, MachineType machine) {
        super(type, pos, state);
        this.machine = machine;
        this.inventory = new ItemStackHandler(machine.slotCount()) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                onInventoryChanged(slot);
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return isValidForSlot(slot, stack);
            }
        };
        // Energy is not part of the comparator signal -> no neighbour updates needed
        this.energy = new MachineEnergyStorage(MachineType.ENERGY_CAPACITY, 400, this::markUnsaved);
        this.automation = new AutomationItemHandler(inventory, this::canAutomationExtract);
    }

    /**
     * Marks the chunk for saving without {@link #setChanged()}'s neighbour updates. {@code setChanged()} also calls
     * {@code updateNeighbourForOutputSignal}, which NeoForge forwards as {@code onNeighborChange} to all six neighbours -
     * pipes and cables of other mods react to that. Use this for per-tick state (progress, energy, burn time);
     * inventory changes still go through {@code setChanged()} so comparators update.
     */
    protected void markUnsaved() {
        if (level != null) level.blockEntityChanged(worldPosition);
    }

    public MachineType machine() { return machine; }
    public ItemStackHandler inventory() { return inventory; }
    public IItemHandler automationHandler() { return automation; }
    public IEnergyStorage energyStorage() { return energy; }
    public IEnergyStorage energyStorageFor(net.minecraft.core.Direction side) { return energy; }

    protected int getDataValue(int index) {
        return switch (index) {
            case MachineType.DATA_PROGRESS -> progress;
            case MachineType.DATA_MAX_PROGRESS -> maxProgress;
            case MachineType.DATA_BURN -> burnTime;
            case MachineType.DATA_BURN_MAX -> burnDuration;
            case MachineType.DATA_ENERGY -> energy.getEnergyStored();
            default -> 0;
        };
    }

    protected abstract boolean isValidForSlot(int slot, ItemStack stack);

    protected boolean canAutomationExtract(int slot) {
        return machine.isOutput(slot);
    }

    protected void onInventoryChanged(int slot) {}

    public abstract void serverTick();

    protected void setLit(boolean lit) {
        if (level == null) return;
        BlockState state = getBlockState();
        if (state.hasProperty(MachineBlock.LIT) && state.getValue(MachineBlock.LIT) != lit) {
            level.setBlock(worldPosition, state.setValue(MachineBlock.LIT, lit), 3);
        }
    }

    /** True if the whole stack fits into the output slots. Allocation-free (called every tick). */
    protected boolean canFitOutput(ItemStack stack) {
        int remaining = stack.getCount();
        for (int s = machine.firstOutputSlot(); s < machine.slotCount() && remaining > 0; s++) {
            ItemStack existing = inventory.getStackInSlot(s);
            if (existing.isEmpty()) return true;
            if (ItemStack.isSameItemSameComponents(existing, stack)) {
                remaining -= Math.max(0, existing.getMaxStackSize() - existing.getCount());
            }
        }
        return remaining <= 0;
    }

    /**
     * Inserts a stack into the output slots (without slot validation).
     *
     * @return what did not fit (empty if everything was inserted) - callers must not drop it silently
     */
    protected ItemStack insertOutput(ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (int s = machine.firstOutputSlot(); s < machine.slotCount() && !remaining.isEmpty(); s++) {
            ItemStack existing = inventory.getStackInSlot(s);
            if (existing.isEmpty()) {
                inventory.setStackInSlot(s, remaining);
                return ItemStack.EMPTY;
            }
            if (ItemStack.isSameItemSameComponents(existing, remaining)) {
                int move = Math.min(existing.getMaxStackSize() - existing.getCount(), remaining.getCount());
                if (move > 0) {
                    // Via setStackInSlot (not grow in place) so onContentsChanged/comparators fire
                    inventory.setStackInSlot(s, existing.copyWithCount(existing.getCount() + move));
                    remaining.shrink(move);
                }
            }
        }
        return remaining;
    }

    /** Safety net for output that unexpectedly doesn't fit - spit it out instead of deleting it. */
    protected void dropOverflow(ItemStack stack) {
        if (level != null && !stack.isEmpty()) net.minecraft.world.level.block.Block.popResource(level, worldPosition.above(), stack);
    }

    public void dropContents(Level level, BlockPos pos) {
        for (int i = 0; i < inventory.getSlots(); i++) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), inventory.getStackInSlot(i));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.put("Energy", energy.serializeNBT(registries));
        tag.putInt("Progress", progress);
        tag.putInt("MaxProgress", maxProgress);
        tag.putInt("BurnTime", burnTime);
        tag.putInt("BurnDuration", burnDuration);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        if (tag.contains("Energy")) energy.deserializeNBT(registries, tag.get("Energy"));
        progress = tag.getInt("Progress");
        maxProgress = Math.max(1, tag.getInt("MaxProgress"));
        burnTime = tag.getInt("BurnTime");
        burnDuration = tag.getInt("BurnDuration");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.highcraft." + machine.id);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new MachineMenu(machine, id, playerInventory, inventory, data,
                ContainerLevelAccess.create(level, worldPosition));
    }
}
