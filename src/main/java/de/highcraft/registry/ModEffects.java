package de.highcraft.registry;

import de.highcraft.HighCraft;
import de.highcraft.effect.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, HighCraft.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> STONED = EFFECTS.register("stoned", () -> new StonedEffect()
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, HighCraft.id("effect.stoned"), -0.07, Operation.ADD_MULTIPLIED_TOTAL));

    public static final DeferredHolder<MobEffect, MobEffect> MUNCHIES = EFFECTS.register("munchies", MunchiesEffect::new);

    public static final DeferredHolder<MobEffect, MobEffect> RUSH = EFFECTS.register("rush", () -> new DrugEffect(MobEffectCategory.BENEFICIAL, 0xF2F2F2)
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, HighCraft.id("effect.rush"), 0.12, Operation.ADD_MULTIPLIED_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_SPEED, HighCraft.id("effect.rush"), 0.10, Operation.ADD_MULTIPLIED_TOTAL)
            .addAttributeModifier(Attributes.BLOCK_BREAK_SPEED, HighCraft.id("effect.rush"), 0.20, Operation.ADD_MULTIPLIED_TOTAL)
            .addAttributeModifier(Attributes.JUMP_STRENGTH, HighCraft.id("effect.rush"), 0.05, Operation.ADD_MULTIPLIED_TOTAL));

    public static final DeferredHolder<MobEffect, MobEffect> CRASH = EFFECTS.register("crash", () -> new DrugEffect(MobEffectCategory.HARMFUL, 0x4A4A55)
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, HighCraft.id("effect.crash"), -0.10, Operation.ADD_MULTIPLIED_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_DAMAGE, HighCraft.id("effect.crash"), -0.15, Operation.ADD_MULTIPLIED_TOTAL)
            .addAttributeModifier(Attributes.BLOCK_BREAK_SPEED, HighCraft.id("effect.crash"), -0.20, Operation.ADD_MULTIPLIED_TOTAL));

    public static final DeferredHolder<MobEffect, MobEffect> SEDATED = EFFECTS.register("sedated", () -> new SedatedEffect()
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, HighCraft.id("effect.sedated"), -0.10, Operation.ADD_MULTIPLIED_TOTAL)
            .addAttributeModifier(Attributes.ARMOR, HighCraft.id("effect.sedated"), 2.0, Operation.ADD_VALUE)
            .addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, HighCraft.id("effect.sedated"), 0.15, Operation.ADD_VALUE));

    public static final DeferredHolder<MobEffect, MobEffect> TRIP = EFFECTS.register("trip", () -> new DrugEffect(MobEffectCategory.NEUTRAL, 0xC050F0)
            .addAttributeModifier(Attributes.LUCK, HighCraft.id("effect.trip"), 1.0, Operation.ADD_VALUE));

    /** LSD: kaleidoscopic, very intense trip. */
    public static final DeferredHolder<MobEffect, MobEffect> ACID = EFFECTS.register("acid", () -> new DrugEffect(MobEffectCategory.NEUTRAL, 0x40E0D0)
            .addAttributeModifier(Attributes.LUCK, HighCraft.id("effect.acid"), 2.0, Operation.ADD_VALUE)
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, HighCraft.id("effect.acid"), 0.04, Operation.ADD_MULTIPLIED_TOTAL));

    public static final DeferredHolder<MobEffect, MobEffect> NICOTINE_BUZZ = EFFECTS.register("nicotine_buzz", () -> new DrugEffect(MobEffectCategory.BENEFICIAL, 0xC89B5A)
            .addAttributeModifier(Attributes.ATTACK_SPEED, HighCraft.id("effect.nicotine"), 0.05, Operation.ADD_MULTIPLIED_TOTAL)
            .addAttributeModifier(Attributes.BLOCK_BREAK_SPEED, HighCraft.id("effect.nicotine"), 0.08, Operation.ADD_MULTIPLIED_TOTAL));

    public static final DeferredHolder<MobEffect, MobEffect> WITHDRAWAL = EFFECTS.register("withdrawal", () -> new WithdrawalEffect()
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, HighCraft.id("effect.withdrawal"), -0.06, Operation.ADD_MULTIPLIED_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_DAMAGE, HighCraft.id("effect.withdrawal"), -0.10, Operation.ADD_MULTIPLIED_TOTAL)
            .addAttributeModifier(Attributes.BLOCK_BREAK_SPEED, HighCraft.id("effect.withdrawal"), -0.12, Operation.ADD_MULTIPLIED_TOTAL));

    public static final DeferredHolder<MobEffect, MobEffect> OVERDOSE = EFFECTS.register("overdose", OverdoseEffect::new);

    /** Meth: extremely wired - fast, tireless, no sleep. Followed by a comedown. */
    public static final DeferredHolder<MobEffect, MobEffect> METH_HIGH = EFFECTS.register("meth_high", () -> new MethHighEffect()
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, HighCraft.id("effect.meth_high"), 0.16, Operation.ADD_MULTIPLIED_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_SPEED, HighCraft.id("effect.meth_high"), 0.15, Operation.ADD_MULTIPLIED_TOTAL)
            .addAttributeModifier(Attributes.BLOCK_BREAK_SPEED, HighCraft.id("effect.meth_high"), 0.35, Operation.ADD_MULTIPLIED_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_DAMAGE, HighCraft.id("effect.meth_high"), 0.08, Operation.ADD_MULTIPLIED_TOTAL));

    /** Ecstasy: warm, loving euphoria; slow regeneration, but it makes you thirsty (hunger). */
    public static final DeferredHolder<MobEffect, MobEffect> EUPHORIA = EFFECTS.register("euphoria", () -> new EuphoriaEffect()
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, HighCraft.id("effect.euphoria"), 0.08, Operation.ADD_MULTIPLIED_TOTAL)
            .addAttributeModifier(Attributes.LUCK, HighCraft.id("effect.euphoria"), 1.0, Operation.ADD_VALUE)
            .addAttributeModifier(Attributes.JUMP_STRENGTH, HighCraft.id("effect.euphoria"), 0.06, Operation.ADD_MULTIPLIED_TOTAL));

    /** After meth or ecstasy: drained, slow, gloomy. */
    public static final DeferredHolder<MobEffect, MobEffect> COMEDOWN = EFFECTS.register("comedown", () -> new DrugEffect(MobEffectCategory.HARMFUL, 0x4E4E6E)
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, HighCraft.id("effect.comedown"), -0.12, Operation.ADD_MULTIPLIED_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_DAMAGE, HighCraft.id("effect.comedown"), -0.15, Operation.ADD_MULTIPLIED_TOTAL)
            .addAttributeModifier(Attributes.BLOCK_BREAK_SPEED, HighCraft.id("effect.comedown"), -0.25, Operation.ADD_MULTIPLIED_TOTAL));

    /** Toxic lab fumes from an unventilated chemical reactor. A gas mask protects. */
    public static final DeferredHolder<MobEffect, MobEffect> TOXIC_FUMES = EFFECTS.register("toxic_fumes", ToxicFumesEffect::new);

    // Note: always use getDelegate() for lookups/adding - the map keys of active effects are
    // the registry holders (Holder.Reference), whose hashCode differs from DeferredHolder.

    private ModEffects() {}
}
