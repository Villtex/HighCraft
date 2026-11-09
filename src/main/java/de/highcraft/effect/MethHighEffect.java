package de.highcraft.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** "Meth High": wired and tireless - suppresses hunger, but the heart races (see ClientGameEvents). */
public class MethHighEffect extends DrugEffect {
    public MethHighEffect() { super(MobEffectCategory.BENEFICIAL, 0x7FD6F2); }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 40 == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // Appetite suppression: hunger barely drops while high
        if (entity instanceof Player player) {
            player.getFoodData().setExhaustion(Math.max(0f, player.getFoodData().getExhaustionLevel() - 0.6f));
        }
        return true;
    }
}
