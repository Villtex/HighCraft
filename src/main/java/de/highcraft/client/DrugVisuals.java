package de.highcraft.client;

import de.highcraft.registry.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

/**
 * Computes the smoothed intensities (0..1) of all drug states of the local player every client tick.
 * Shaders, camera, HUD and particles only read these values.
 */
public final class DrugVisuals {
    public static final int STONED = 0, TRIP = 1, RUSH = 2, SEDATED = 3, WITHDRAWAL = 4, OVERDOSE = 5, NICOTINE = 6, CRASH = 7, ACID = 8,
            METH = 9, ECSTASY = 10, COMEDOWN = 11;
    private static final int N = 12;

    private static final float[] current = new float[N];
    private static final float[] previous = new float[N];
    private static float time;
    private static float prevTime;
    /** Cached once per tick - queried several times per frame (camera, FOV, shader, HUD). */
    private static boolean active;

    private DrugVisuals() {}

    public static void tick() {
        Player player = Minecraft.getInstance().player;
        System.arraycopy(current, 0, previous, 0, N);
        prevTime = time;
        time += 0.05f;
        if (time > 3600f) { time -= 3600f; prevTime -= 3600f; }
        boolean any = false;
        for (int i = 0; i < N; i++) {
            float target = player == null ? 0 : target(player, effectFor(i));
            float rate = target > current[i] ? 0.015f : 0.008f; // fade in slowly, fade out even more slowly
            current[i] = Mth.approach(current[i], target, rate);
            if (current[i] > 0.001f || previous[i] > 0.001f) any = true;
        }
        active = any;
    }

    private static Holder<MobEffect> effectFor(int i) {
        return switch (i) {
            case STONED -> ModEffects.STONED.getDelegate();
            case TRIP -> ModEffects.TRIP.getDelegate();
            case RUSH -> ModEffects.RUSH.getDelegate();
            case SEDATED -> ModEffects.SEDATED.getDelegate();
            case WITHDRAWAL -> ModEffects.WITHDRAWAL.getDelegate();
            case OVERDOSE -> ModEffects.OVERDOSE.getDelegate();
            case NICOTINE -> ModEffects.NICOTINE_BUZZ.getDelegate();
            case ACID -> ModEffects.ACID.getDelegate();
            case METH -> ModEffects.METH_HIGH.getDelegate();
            case ECSTASY -> ModEffects.EUPHORIA.getDelegate();
            case COMEDOWN -> ModEffects.COMEDOWN.getDelegate();
            default -> ModEffects.CRASH.getDelegate();
        };
    }

    private static float target(Player player, Holder<MobEffect> effect) {
        MobEffectInstance inst = player.getEffect(effect);
        if (inst == null) return 0;
        float strength = 0.6f + 0.2f * Math.min(2, inst.getAmplifier());
        // Last 10 seconds: fade out
        float fade = inst.isInfiniteDuration() ? 1f : Mth.clamp(inst.getDuration() / 200f, 0f, 1f);
        return strength * fade;
    }

    public static float get(int index, float partialTick) {
        return Mth.lerp(partialTick, previous[index], current[index]);
    }

    public static float time(float partialTick) {
        return Mth.lerp(partialTick, prevTime, time);
    }

    public static boolean anyActive() {
        return active;
    }

    public static void reset() {
        java.util.Arrays.fill(current, 0);
        java.util.Arrays.fill(previous, 0);
        active = false;
    }
}
