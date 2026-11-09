package de.highcraft.block.entity;

import net.neoforged.neoforge.energy.EnergyStorage;

/** Energy storage that only accepts energy and is consumed internally. */
public class MachineEnergyStorage extends EnergyStorage {
    private final Runnable onChange;

    public MachineEnergyStorage(int capacity, int maxReceive, Runnable onChange) {
        super(capacity, maxReceive, 0);
        this.onChange = onChange;
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        int r = super.receiveEnergy(toReceive, simulate);
        if (r > 0 && !simulate) onChange.run();
        return r;
    }

    /** Internal consumption (bypasses maxExtract). */
    public boolean consume(int amount) {
        if (energy < amount) return false;
        energy -= amount;
        onChange.run();
        return true;
    }

    public void setStored(int value) {
        this.energy = Math.max(0, Math.min(capacity, value));
    }
}
