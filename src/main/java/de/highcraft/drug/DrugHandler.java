package de.highcraft.drug;

import de.highcraft.HighCraft;
import de.highcraft.config.HighCraftConfig;
import de.highcraft.registry.ModAttachments;
import de.highcraft.registry.ModEffects;
import de.highcraft.registry.ModParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Central server logic: consumption, tolerance, addiction, withdrawal and overdose.
 */
public final class DrugHandler {
    public static final ResourceKey<DamageType> OVERDOSE_DAMAGE =
            ResourceKey.create(Registries.DAMAGE_TYPE, HighCraft.id("overdose"));

    public static final float OVERDOSE_THRESHOLD = 100f;
    public static final float LETHAL_THRESHOLD = 170f;
    private static final float WITHDRAWAL_MIN_ADDICTION = 25f;

    private DrugHandler() {}

    /**
     * Applies a dose to a living entity.
     *
     * @param qualityMult multiplier from the product's quality (1.0 .. 1.6)
     */
    public static void consume(LivingEntity entity, DrugDose dose, float qualityMult) {
        if (entity.level().isClientSide) return;
        float tolerance = 0f;
        PlayerDrugData data = null;
        if (entity instanceof Player player) {
            data = player.getData(ModAttachments.DRUG_DATA);
            tolerance = data.tolerance(dose.type());
        }
        float durationMult = qualityMult * (1f - tolerance * 0.6f) * HighCraftConfig.EFFECT_DURATION_MULTIPLIER.get().floatValue();

        for (DrugDose.Entry e : dose.effects()) {
            int duration = Math.max(40, Math.round(e.duration() * durationMult));
            int amplifier = e.amplifier();
            MobEffectInstance existing = entity.getEffect(e.effect());
            if (existing != null) {
                // Topping up: extend the duration, strength rises up to +2
                amplifier = Math.min(e.amplifier() + 2, Math.max(existing.getAmplifier() + 1, amplifier));
                duration = Math.min(e.duration() * 3, Math.max(duration, existing.getDuration() + duration / 2));
            }
            entity.addEffect(new MobEffectInstance(e.effect(), duration, amplifier, false, false, true));
        }
        if (dose.instantHeal() > 0) entity.heal(dose.instantHeal() * qualityMult);

        if (data != null) {
            Player player = (Player) entity;
            long now = player.level().getGameTime();
            data.setIntoxication(data.intoxication() + dose.intoxication() * qualityMult * (1f - tolerance * 0.3f));
            if (HighCraftConfig.ENABLE_ADDICTION.get()) {
                float current = data.addiction(dose.type());
                float gain = dose.addiction() * dose.type().addictiveness()
                        * HighCraftConfig.ADDICTION_MULTIPLIER.get().floatValue()
                        * (1f + current / 100f);
                data.setAddiction(dose.type(), current + gain);
            }
            data.setTolerance(dose.type(), data.tolerance(dose.type()) + dose.type().toleranceGain());
            data.setLastUse(dose.type(), now);
            data.addDose(dose.type());

            // Consuming relieves withdrawal
            if (player.hasEffect(ModEffects.WITHDRAWAL.getDelegate()) && !anyCraving(player, data, now)) {
                player.removeEffect(ModEffects.WITHDRAWAL.getDelegate());
            }
            if (data.intoxication() > OVERDOSE_THRESHOLD * 0.8f && data.intoxication() <= OVERDOSE_THRESHOLD) {
                player.displayClientMessage(Component.translatable("message.highcraft.dizzy").withStyle(ChatFormatting.YELLOW), true);
            }
        }
    }

    /** Called every second (20 ticks) per player on the server. */
    public static void tickPlayer(ServerPlayer player) {
        PlayerDrugData data = player.getData(ModAttachments.DRUG_DATA);
        long now = player.level().getGameTime();

        // Crash after the cocaine high (queued by the expired event)
        if (data.pendingCrash() > 0) {
            int amp = data.pendingCrash() - 1;
            data.setPendingCrash(0);
            player.addEffect(new MobEffectInstance(ModEffects.CRASH.getDelegate(), 20 * (60 + 30 * amp), amp, false, true, true));
            player.displayClientMessage(Component.translatable("message.highcraft.crash").withStyle(ChatFormatting.GRAY), true);
        }

        // Comedown after meth or ecstasy (queued by the expired event)
        if (data.pendingComedown() > 0) {
            int amp = data.pendingComedown() - 1;
            data.clearPendingComedown();
            player.addEffect(new MobEffectInstance(ModEffects.COMEDOWN.getDelegate(), 20 * (90 + 60 * amp), amp, false, true, true));
            player.displayClientMessage(Component.translatable("message.highcraft.comedown").withStyle(ChatFormatting.GRAY), true);
        }

        // Intoxication level decays
        data.setIntoxication(data.intoxication() - 0.5f);

        boolean inWithdrawal = player.hasEffect(ModEffects.WITHDRAWAL.getDelegate());
        for (DrugType t : DrugType.values()) {
            data.setTolerance(t, data.tolerance(t) - 0.0005f);
            float decay = inWithdrawal ? 0.03f : 0.01f;
            data.setAddiction(t, data.addiction(t) - decay);
        }

        // Withdrawal
        if (HighCraftConfig.ENABLE_ADDICTION.get() && HighCraftConfig.ENABLE_WITHDRAWAL.get()) {
            DrugType worst = null;
            float worstValue = 0;
            for (DrugType t : DrugType.values()) {
                if (isCraving(data, t, now) && data.addiction(t) > worstValue) {
                    worst = t;
                    worstValue = data.addiction(t);
                }
            }
            if (worst != null) {
                int amp = worstValue >= 75 ? 2 : worstValue >= 50 ? 1 : 0;
                MobEffectInstance current = player.getEffect(ModEffects.WITHDRAWAL.getDelegate());
                if (current == null || current.getDuration() < 60 || current.getAmplifier() < amp) {
                    player.addEffect(new MobEffectInstance(ModEffects.WITHDRAWAL.getDelegate(), 20 * 30, amp, false, true, true));
                }
                if (now - data.lastMessageTick() > 20 * 90) {
                    data.setLastMessageTick(now);
                    player.displayClientMessage(Component.translatable("message.highcraft.craving", worst.displayName())
                            .withStyle(ChatFormatting.RED), true);
                }
            }
        }

        // Overdose
        if (HighCraftConfig.ENABLE_OVERDOSE.get()) {
            float intox = data.intoxication();
            if (intox >= LETHAL_THRESHOLD) {
                data.setIntoxication(OVERDOSE_THRESHOLD * 0.5f);
                player.hurt(overdoseSource(player.serverLevel()), Float.MAX_VALUE);
            } else if (intox >= OVERDOSE_THRESHOLD) {
                int amp = intox >= 140 ? 2 : intox >= 120 ? 1 : 0;
                player.addEffect(new MobEffectInstance(ModEffects.OVERDOSE.getDelegate(), 60, amp, false, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0, false, false, false));
                if (now - data.lastMessageTick() > 20 * 20) {
                    data.setLastMessageTick(now);
                    player.displayClientMessage(Component.translatable("message.highcraft.overdose").withStyle(ChatFormatting.DARK_RED), true);
                }
            }
        }
    }

    public static boolean isCraving(PlayerDrugData data, DrugType t, long now) {
        return data.addiction(t) >= WITHDRAWAL_MIN_ADDICTION
                && t.cravingDelay() != Integer.MAX_VALUE
                && now - data.lastUse(t) > t.cravingDelay();
    }

    private static boolean anyCraving(Player player, PlayerDrugData data, long now) {
        for (DrugType t : DrugType.values()) if (isCraving(data, t, now)) return true;
        return false;
    }

    public static DamageSource overdoseSource(ServerLevel level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(OVERDOSE_DAMAGE));
    }

    /** Exhaled smoke - sent from the server so other players see it too. */
    public static void exhaleSmoke(LivingEntity entity, int count) {
        if (entity.level() instanceof ServerLevel server) {
            Vec3 look = entity.getLookAngle();
            Vec3 mouth = entity.getEyePosition().add(look.scale(0.35)).add(0, -0.12, 0);
            server.sendParticles(ModParticles.SMOKE_PUFF.get(), mouth.x, mouth.y, mouth.z, count,
                    look.x * 0.05, 0.02, look.z * 0.05, 0.02);
        }
    }
}
