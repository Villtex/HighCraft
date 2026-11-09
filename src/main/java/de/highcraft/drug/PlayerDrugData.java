package de.highcraft.drug;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * Per-player state: addiction (0..100), tolerance (0..0.85), last use and intoxication level.
 */
public class PlayerDrugData implements INBTSerializable<CompoundTag> {
    public static final float MAX_TOLERANCE = 0.85f;

    private final float[] addiction = new float[DrugType.values().length];
    private final float[] tolerance = new float[DrugType.values().length];
    private final long[] lastUse = new long[DrugType.values().length];
    private final int[] doses = new int[DrugType.values().length];
    private float intoxication;
    private int pendingCrash;
    private int pendingComedown;
    private long lastMessageTick;

    public float addiction(DrugType t) { return addiction[t.ordinal()]; }
    public void setAddiction(DrugType t, float v) { addiction[t.ordinal()] = Mth.clamp(v, 0f, 100f); }

    public float tolerance(DrugType t) { return tolerance[t.ordinal()]; }
    public void setTolerance(DrugType t, float v) { tolerance[t.ordinal()] = Mth.clamp(v, 0f, MAX_TOLERANCE); }

    public long lastUse(DrugType t) { return lastUse[t.ordinal()]; }
    public void setLastUse(DrugType t, long tick) { lastUse[t.ordinal()] = tick; }

    public int doses(DrugType t) { return doses[t.ordinal()]; }
    public void addDose(DrugType t) { doses[t.ordinal()]++; }

    public float intoxication() { return intoxication; }
    public void setIntoxication(float v) { intoxication = Mth.clamp(v, 0f, 250f); }

    public int pendingCrash() { return pendingCrash; }
    public void setPendingCrash(int amplifierPlusOne) { pendingCrash = amplifierPlusOne; }

    public int pendingComedown() { return pendingComedown; }
    public void setPendingComedown(int amplifierPlusOne) { pendingComedown = Math.max(pendingComedown, amplifierPlusOne); }
    public void clearPendingComedown() { pendingComedown = 0; }

    public long lastMessageTick() { return lastMessageTick; }
    public void setLastMessageTick(long t) { lastMessageTick = t; }

    public void reset() {
        for (DrugType t : DrugType.values()) {
            addiction[t.ordinal()] = 0;
            tolerance[t.ordinal()] = 0;
            lastUse[t.ordinal()] = 0;
            doses[t.ordinal()] = 0;
        }
        intoxication = 0;
        pendingCrash = 0;
        pendingComedown = 0;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        for (DrugType t : DrugType.values()) {
            CompoundTag d = new CompoundTag();
            d.putFloat("addiction", addiction[t.ordinal()]);
            d.putFloat("tolerance", tolerance[t.ordinal()]);
            d.putLong("lastUse", lastUse[t.ordinal()]);
            d.putInt("doses", doses[t.ordinal()]);
            tag.put(t.id(), d);
        }
        tag.putFloat("intoxication", intoxication);
        tag.putInt("pendingCrash", pendingCrash);
        tag.putInt("pendingComedown", pendingComedown);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        for (DrugType t : DrugType.values()) {
            CompoundTag d = tag.getCompound(t.id());
            addiction[t.ordinal()] = d.getFloat("addiction");
            tolerance[t.ordinal()] = d.getFloat("tolerance");
            lastUse[t.ordinal()] = d.getLong("lastUse");
            doses[t.ordinal()] = d.getInt("doses");
        }
        intoxication = tag.getFloat("intoxication");
        pendingCrash = tag.getInt("pendingCrash");
        pendingComedown = tag.getInt("pendingComedown");
    }
}
