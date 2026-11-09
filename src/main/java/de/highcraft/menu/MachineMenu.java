package de.highcraft.menu;

import de.highcraft.block.MachineType;
import de.highcraft.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/** One menu for all GUI machines; the layout comes from {@link MachineType}. */
public class MachineMenu extends AbstractContainerMenu {
    private final MachineType machine;
    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final int machineSlots;

    /** Client constructor. */
    public static MachineMenu client(MachineType machine, int id, Inventory inv, RegistryFriendlyByteBuf buf) {
        return new MachineMenu(machine, id, inv, new ItemStackHandler(machine.slotCount()),
                new SimpleContainerData(MachineType.DATA_COUNT), ContainerLevelAccess.NULL);
    }

    public MachineMenu(MachineType machine, int id, Inventory inv, IItemHandler handler, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.get(machine), id);
        this.machine = machine;
        this.data = data;
        this.access = access;
        this.machineSlots = machine.slotCount();

        for (int i = 0; i < machine.inputCount(); i++) {
            addSlot(new MachineSlot(handler, i, machine.inputPos[i][0], machine.inputPos[i][1]));
        }
        if (machine.hasFuel()) {
            addSlot(new MachineSlot(handler, machine.fuelSlot(), machine.fuelPos[0], machine.fuelPos[1]));
        }
        for (int i = 0; i < machine.outputCount(); i++) {
            addSlot(new MachineSlot(handler, machine.firstOutputSlot() + i, machine.outputPos[i][0], machine.outputPos[i][1]) {
                @Override
                public boolean mayPlace(ItemStack stack) { return false; }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, 8 + col * 18, 142));
        }
        addDataSlots(data);
    }

    /**
     * Shift-click merges into an existing stack in place ({@code moveItemStackTo} -> {@code grow} + {@code slot.setChanged()}),
     * but NeoForge's SlotItemHandler doesn't forward setChanged to the handler. Without this the machine never
     * sees the change - e.g. a press holding 1 resin (recipe needs 2) stayed idle after shift-clicking more resin in.
     */
    private static class MachineSlot extends SlotItemHandler {
        MachineSlot(IItemHandler handler, int index, int x, int y) {
            super(handler, index, x, y);
        }

        @Override
        public void setChanged() {
            if (getItemHandler() instanceof IItemHandlerModifiable modifiable) {
                modifiable.setStackInSlot(getContainerSlot(), getItem()); // same stack -> only fires onContentsChanged
            }
            super.setChanged();
        }
    }

    public MachineType machine() { return machine; }
    public int data(int index) { return data.get(index); }
    public int machineSlotCount() { return machineSlots; }

    public float progress() {
        int max = data.get(MachineType.DATA_MAX_PROGRESS);
        return max <= 0 ? 0 : Math.min(1f, data.get(MachineType.DATA_PROGRESS) / (float) max);
    }

    public float burn() {
        int max = data.get(MachineType.DATA_BURN_MAX);
        return max <= 0 ? 0 : data.get(MachineType.DATA_BURN) / (float) max;
    }

    public float energyFraction() {
        return data.get(MachineType.DATA_ENERGY) / (float) MachineType.ENERGY_CAPACITY;
    }

    public float waterFraction() {
        return data.get(MachineType.DATA_WATER) / (float) MachineType.WATER_CAPACITY;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        int playerStart = machineSlots;
        int playerEnd = slots.size();
        if (index < machineSlots) {
            if (!moveItemStackTo(stack, playerStart, playerEnd, true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, copy);
        } else {
            boolean moved = false;
            // Fuel goes into the fuel slot first, otherwise into matching inputs
            for (int i = 0; i < machine.firstOutputSlot() && !moved; i++) {
                Slot target = slots.get(i);
                if (target.mayPlace(stack)) moved = moveItemStackTo(stack, i, i + 1, false);
            }
            if (!moved) {
                if (index < playerEnd - 9) {
                    if (!moveItemStackTo(stack, playerEnd - 9, playerEnd, false)) return ItemStack.EMPTY;
                } else if (!moveItemStackTo(stack, playerStart, playerEnd - 9, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        Block block = ModMenus.blockFor(machine);
        return access == ContainerLevelAccess.NULL || stillValid(access, player, block);
    }
}
