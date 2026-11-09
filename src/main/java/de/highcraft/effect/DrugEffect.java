package de.highcraft.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Base class for all drug effects (public constructor). */
public class DrugEffect extends MobEffect {
    public DrugEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
