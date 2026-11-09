package de.highcraft.block;

import de.highcraft.recipe.MachineRecipe;

/**
 * Describes slot layout and GUI geometry of all GUI machines.
 * Coordinates are GUI pixels (176x166 textures).
 */
public enum MachineType {
    GRINDER("grinder", MachineRecipe.Kind.GRINDING,
            new int[][]{{56, 17}}, new int[]{56, 53}, new int[][]{{116, 35}}, new int[]{79, 34}, new int[]{57, 37}),
    EXTRACTOR("extractor", MachineRecipe.Kind.EXTRACTING,
            new int[][]{{38, 17}, {62, 17}}, new int[]{50, 53}, new int[][]{{116, 35}}, new int[]{82, 34}, new int[]{51, 37}),
    LAB("lab_station", MachineRecipe.Kind.LAB,
            new int[][]{{30, 17}, {48, 17}, {66, 17}}, new int[]{48, 53}, new int[][]{{124, 35}}, new int[]{90, 34}, new int[]{49, 37}),
    PRESS("press", MachineRecipe.Kind.PRESSING,
            new int[][]{{56, 17}}, new int[]{56, 53}, new int[][]{{116, 35}}, new int[]{79, 34}, new int[]{57, 37}),
    /** Chemical reactor: 3 reagents + heat (fuel or FE). Needs ventilation (fume hood / exhaust fan). */
    REACTOR("chemical_reactor", MachineRecipe.Kind.REACTING,
            new int[][]{{30, 17}, {48, 17}, {66, 17}}, new int[]{48, 53}, new int[][]{{124, 35}}, new int[]{90, 34}, new int[]{49, 37}),
    /** Crystallizer: 1 batch + coolant slot (ice) instead of fuel; also runs cold-passively (ice next to it, cold biome). */
    CRYSTALLIZER("crystallizer", MachineRecipe.Kind.CRYSTALLIZING,
            new int[][]{{56, 17}}, new int[]{56, 53}, new int[][]{{116, 35}}, new int[]{79, 34}, new int[]{57, 37}),
    /** Pill press: powder + binder + dye -> pills. */
    PILL_PRESS("pill_press", MachineRecipe.Kind.PILL_PRESSING,
            new int[][]{{30, 17}, {48, 17}, {66, 17}}, new int[]{48, 53}, new int[][]{{124, 35}}, new int[]{90, 34}, new int[]{49, 37}),
    /** Grow tent: inputs = seeds, fertilizer, water bucket; no fuel; 4 outputs. */
    GROW_TENT("grow_tent", null,
            new int[][]{{44, 17}, {44, 35}, {44, 53}}, null, new int[][]{{98, 26}, {116, 26}, {98, 44}, {116, 44}}, new int[]{66, 35}, null);

    public final String id;
    public final MachineRecipe.Kind kind;
    public final int[][] inputPos;
    public final int[] fuelPos;
    public final int[][] outputPos;
    public final int[] arrowPos;
    public final int[] flamePos;

    MachineType(String id, MachineRecipe.Kind kind, int[][] inputPos, int[] fuelPos, int[][] outputPos, int[] arrowPos, int[] flamePos) {
        this.id = id;
        this.kind = kind;
        this.inputPos = inputPos;
        this.fuelPos = fuelPos;
        this.outputPos = outputPos;
        this.arrowPos = arrowPos;
        this.flamePos = flamePos;
    }

    public int inputCount() { return inputPos.length; }
    public boolean hasFuel() { return fuelPos != null; }
    public int fuelSlot() { return hasFuel() ? inputCount() : -1; }
    public int firstOutputSlot() { return inputCount() + (hasFuel() ? 1 : 0); }
    public int outputCount() { return outputPos.length; }
    public int slotCount() { return firstOutputSlot() + outputCount(); }
    public boolean isOutput(int slot) { return slot >= firstOutputSlot(); }

    /** Number of values synced via ContainerData. */
    public static final int DATA_COUNT = 8;
    public static final int DATA_PROGRESS = 0, DATA_MAX_PROGRESS = 1, DATA_BURN = 2, DATA_BURN_MAX = 3,
            DATA_ENERGY = 4, DATA_WATER = 5, DATA_LIGHT = 6, DATA_FERTILIZED = 7;
    /** Reactor: DATA_LIGHT = ventilation (0/1); crystallizer: DATA_LIGHT = cooling (0 warm, 1 cold, 2 deep cold, 3 coolant). */
    public static final int DATA_STATUS = DATA_LIGHT;

    public boolean usesCoolant() { return this == CRYSTALLIZER; }

    public static final int ENERGY_CAPACITY = 20000;
    public static final int WATER_CAPACITY = 4000;
}
