package de.highcraft.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class HighCraftConfig {
    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec CLIENT_SPEC;

    // ---- Common ----
    public static final ModConfigSpec.BooleanValue ENABLE_ADDICTION;
    public static final ModConfigSpec.BooleanValue ENABLE_WITHDRAWAL;
    public static final ModConfigSpec.BooleanValue ENABLE_OVERDOSE;
    public static final ModConfigSpec.DoubleValue ADDICTION_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue EFFECT_DURATION_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue CROP_GROWTH_MULTIPLIER;
    public static final ModConfigSpec.IntValue MACHINE_ENERGY_PER_TICK;
    public static final ModConfigSpec.BooleanValue GRASS_DROPS_SEEDS;
    public static final ModConfigSpec.BooleanValue LAB_FUMES;
    public static final ModConfigSpec.BooleanValue LAB_ACCIDENTS;
    public static final ModConfigSpec.IntValue VILLAGE_LAB_WEIGHT;

    // ---- Client ----
    public static final ModConfigSpec.BooleanValue ENABLE_SHADERS;
    public static final ModConfigSpec.DoubleValue SHADER_INTENSITY;
    public static final ModConfigSpec.BooleanValue ENABLE_CAMERA_EFFECTS;
    public static final ModConfigSpec.BooleanValue ENABLE_HUD_OVERLAY;
    public static final ModConfigSpec.BooleanValue ENABLE_PARTICLES;
    public static final ModConfigSpec.BooleanValue SHADERS_WITH_SHADERPACK;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.comment("HighCraft gameplay settings").push("gameplay");
        ENABLE_ADDICTION = b.comment("Enable the addiction system").define("enableAddiction", true);
        ENABLE_WITHDRAWAL = b.comment("Enable withdrawal symptoms when addicted").define("enableWithdrawal", true);
        ENABLE_OVERDOSE = b.comment("Enable overdose (damage/death) when the intoxication level gets too high").define("enableOverdose", true);
        ADDICTION_MULTIPLIER = b.comment("Multiplier for addiction gain").defineInRange("addictionMultiplier", 1.0, 0.0, 10.0);
        EFFECT_DURATION_MULTIPLIER = b.comment("Multiplier for effect duration").defineInRange("effectDurationMultiplier", 1.0, 0.1, 10.0);
        CROP_GROWTH_MULTIPLIER = b.comment("Multiplier for crop growth").defineInRange("cropGrowthMultiplier", 1.0, 0.1, 10.0);
        MACHINE_ENERGY_PER_TICK = b.comment("FE used by machines per tick (instead of fuel)").defineInRange("machineEnergyPerTick", 20, 1, 10000);
        GRASS_DROPS_SEEDS = b.comment("Controlled by the loot modifier file; informational only").define("grassDropsSeeds", true);
        b.pop();
        b.comment("Drug labs (chemical reactor, crystallizer, pill press)").push("labs");
        LAB_FUMES = b.comment("An unventilated chemical reactor poisons nearby players and mobs without a gas mask (toxic fumes)").define("labFumes", true);
        LAB_ACCIDENTS = b.comment("An unventilated chemical reactor can have small accidents (explosion without block damage)").define("labAccidents", true);
        VILLAGE_LAB_WEIGHT = b.comment("Weight of drug labs in the village house pools (vanilla houses use 1-5; 0 = no village labs). Needs a world restart.")
                .defineInRange("villageLabWeight", 2, 0, 50);
        b.pop();
        COMMON_SPEC = b.build();

        ModConfigSpec.Builder c = new ModConfigSpec.Builder();
        c.comment("Visual effects (client)").push("visuals");
        ENABLE_SHADERS = c.comment("Screen shaders for drug effects (disable if you have problems with shader packs)").define("enableShaders", true);
        SHADER_INTENSITY = c.comment("Strength of the screen shaders").defineInRange("shaderIntensity", 1.0, 0.0, 2.0);
        ENABLE_CAMERA_EFFECTS = c.comment("Camera sway and FOV pulsing").define("enableCameraEffects", true);
        ENABLE_HUD_OVERLAY = c.comment("Coloured vignette in the HUD").define("enableHudOverlay", true);
        ENABLE_PARTICLES = c.comment("Hallucination and smoke particles").define("enableParticles", true);
        SHADERS_WITH_SHADERPACK = c.comment("Also apply the screen shaders while an Iris shader pack is active (applied after the shader pack)")
                .define("shadersWithShaderpack", true);
        c.pop();
        CLIENT_SPEC = c.build();
    }

    private HighCraftConfig() {}
}
