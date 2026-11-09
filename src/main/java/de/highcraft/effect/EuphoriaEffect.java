package de.highcraft.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** "Euphoria" (ecstasy): slowly heals, but you dehydrate - hunger drains faster. */
public class EuphoriaEffect extends DrugEffect {
    public EuphoriaEffect() { super(MobEffectCategory.BENEFICIAL, 0xFF79C6); }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 80 == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.getHealth() < entity.getMaxHealth()) entity.heal(1.0f);
        if (entity instanceof Player player) player.causeFoodExhaustion(1.2f + 0.6f * amplifier);
        return true;
    }
}
