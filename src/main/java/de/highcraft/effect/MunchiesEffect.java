package de.highcraft.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** "Munchies": hunger rises faster. */
public class MunchiesEffect extends DrugEffect {
    public MunchiesEffect() { super(MobEffectCategory.NEUTRAL, 0xE0A040); }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) { return true; }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player p) p.causeFoodExhaustion(0.012f * (amplifier + 1));
        return true;
    }
}
