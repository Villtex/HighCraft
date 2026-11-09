package de.highcraft.effect;

import de.highcraft.drug.DrugHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/** "Overdose": causes damage. */
public class OverdoseEffect extends DrugEffect {
    public OverdoseEffect() { super(MobEffectCategory.HARMFUL, 0xB01818); }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % Math.max(10, 40 >> amplifier) == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level() instanceof ServerLevel server) {
            entity.hurt(DrugHandler.overdoseSource(server), 1.0f + amplifier);
        }
        return true;
    }
}
