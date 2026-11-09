package de.highcraft.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** "Withdrawal": weak, shaky, occasional nausea. */
public class WithdrawalEffect extends DrugEffect {
    public WithdrawalEffect() { super(MobEffectCategory.HARMFUL, 0x5A6A7A); }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 100 == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.getRandom().nextFloat() < 0.15f * (amplifier + 1)) {
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 0, false, false, false));
        }
        if (entity instanceof Player p) p.causeFoodExhaustion(0.4f * (amplifier + 1));
        return true;
    }
}
