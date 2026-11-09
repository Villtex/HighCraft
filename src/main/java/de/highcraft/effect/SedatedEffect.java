package de.highcraft.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/** "Sedated" (opiates): pain-free, sluggish, heals. */
public class SedatedEffect extends DrugEffect {
    public SedatedEffect() { super(MobEffectCategory.NEUTRAL, 0x8E5A9E); }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int interval = Math.max(20, 60 >> amplifier);
        return duration % interval == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.getHealth() < entity.getMaxHealth()) entity.heal(1.0f);
        return true;
    }
}
