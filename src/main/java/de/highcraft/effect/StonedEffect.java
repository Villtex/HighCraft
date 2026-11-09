package de.highcraft.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/** "Stoned": relaxed, a bit slower, slowly regenerates. */
public class StonedEffect extends DrugEffect {
    public StonedEffect() { super(MobEffectCategory.NEUTRAL, 0x6FBF3A); }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 120 == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.getHealth() < entity.getMaxHealth()) entity.heal(1.0f);
        return true;
    }
}
