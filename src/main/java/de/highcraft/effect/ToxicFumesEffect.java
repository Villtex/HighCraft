package de.highcraft.effect;

import de.highcraft.lab.LabSafety;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/** "Toxic Fumes": nausea and damage over time. Wearing a gas mask stops the damage. */
public class ToxicFumesEffect extends DrugEffect {
    public ToxicFumesEffect() { super(MobEffectCategory.HARMFUL, 0x9AC23C); }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 30 == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level() instanceof ServerLevel server && !LabSafety.hasGasMask(entity)) {
            entity.hurt(LabSafety.fumesSource(server), 1.0f + amplifier);
            if (!entity.hasEffect(MobEffects.CONFUSION)) {
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0, false, false, true));
            }
        }
        return true;
    }
}
