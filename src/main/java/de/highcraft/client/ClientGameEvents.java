package de.highcraft.client;

import de.highcraft.HighCraft;
import de.highcraft.config.HighCraftConfig;
import de.highcraft.drug.Quality;
import de.highcraft.registry.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

@EventBusSubscriber(modid = HighCraft.MODID, value = Dist.CLIENT)
public final class ClientGameEvents {
    private ClientGameEvents() {}

    private static final List<SoundEvent> TRIP_SOUNDS = List.of(
            SoundEvents.AMETHYST_BLOCK_CHIME, SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, SoundEvents.NOTE_BLOCK_CHIME.value(),
            SoundEvents.ENDERMAN_AMBIENT, SoundEvents.CAT_PURR, SoundEvents.BELL_RESONATE);

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused()) return;
        DrugVisuals.tick();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;
        RandomSource r = player.getRandom();

        float trip = Math.max(DrugVisuals.get(DrugVisuals.TRIP, 1f), DrugVisuals.get(DrugVisuals.ACID, 1f) * 1.5f);
        if (trip > 0.05f && HighCraftConfig.ENABLE_PARTICLES.get()) {
            int count = Mth.ceil(trip * 3);
            for (int i = 0; i < count; i++) {
                double x = player.getX() + (r.nextDouble() - 0.5) * 12;
                double y = player.getEyeY() + (r.nextDouble() - 0.4) * 5;
                double z = player.getZ() + (r.nextDouble() - 0.5) * 12;
                mc.level.addParticle(ModParticles.TRIP_SPARKLE.get(), x, y, z, (r.nextDouble() - 0.5) * 0.02, 0.01, (r.nextDouble() - 0.5) * 0.02);
            }
            if (r.nextFloat() < 0.004f * trip * 4) {
                SoundEvent s = TRIP_SOUNDS.get(r.nextInt(TRIP_SOUNDS.size()));
                mc.level.playLocalSound(player.getX() + r.nextGaussian() * 6, player.getY() + r.nextGaussian() * 2,
                        player.getZ() + r.nextGaussian() * 6, s, SoundSource.AMBIENT, 0.4f, 0.5f + r.nextFloat(), false);
            }
        }
        float stoned = DrugVisuals.get(DrugVisuals.STONED, 1f);
        if (stoned > 0.3f && HighCraftConfig.ENABLE_PARTICLES.get() && r.nextFloat() < 0.04f * stoned) {
            mc.level.addParticle(ModParticles.SMOKE_PUFF.get(), player.getX() + r.nextGaussian() * 0.6,
                    player.getEyeY() + 0.3, player.getZ() + r.nextGaussian() * 0.6, 0, 0.01, 0);
        }
        // Ecstasy: hearts above everyone nearby
        float ecstasy = DrugVisuals.get(DrugVisuals.ECSTASY, 1f);
        if (ecstasy > 0.2f && HighCraftConfig.ENABLE_PARTICLES.get() && player.tickCount % 6 == 0) {
            for (var e : mc.level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, player.getBoundingBox().inflate(10))) {
                if (e == player || r.nextFloat() > 0.5f * ecstasy) continue;
                mc.level.addParticle(net.minecraft.core.particles.ParticleTypes.HEART, e.getX() + r.nextGaussian() * 0.3,
                        e.getY() + e.getBbHeight() + 0.3, e.getZ() + r.nextGaussian() * 0.3, 0, 0.05, 0);
            }
            if (r.nextFloat() < 0.15f * ecstasy) {
                mc.level.addParticle(net.minecraft.core.particles.ParticleTypes.NOTE, player.getX() + r.nextGaussian() * 2,
                        player.getEyeY() + 0.6, player.getZ() + r.nextGaussian() * 2, r.nextFloat(), 0, 0);
            }
        }
        // Meth: racing heart
        float meth = DrugVisuals.get(DrugVisuals.METH, 1f);
        if (meth > 0.3f && player.tickCount % 11 == 0) {
            mc.level.playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_HEARTBEAT,
                    SoundSource.PLAYERS, 0.25f * meth, 1.6f, false);
        }
        float overdose = DrugVisuals.get(DrugVisuals.OVERDOSE, 1f);
        if (overdose > 0.2f && player.tickCount % 18 == 0) {
            mc.level.playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_HEARTBEAT,
                    SoundSource.PLAYERS, 0.6f * overdose, 1.2f, false);
        }
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (!HighCraftConfig.ENABLE_CAMERA_EFFECTS.get() || !DrugVisuals.anyActive()) return;
        float pt = (float) event.getPartialTick();
        float t = DrugVisuals.time(pt);
        float trip = DrugVisuals.get(DrugVisuals.TRIP, pt);
        float stoned = DrugVisuals.get(DrugVisuals.STONED, pt);
        float sedated = DrugVisuals.get(DrugVisuals.SEDATED, pt);
        float withdrawal = DrugVisuals.get(DrugVisuals.WITHDRAWAL, pt);
        float overdose = DrugVisuals.get(DrugVisuals.OVERDOSE, pt);
        float acid = DrugVisuals.get(DrugVisuals.ACID, pt);
        float roll = trip * 4.0f * Mth.sin(t * 0.7f) + acid * 7.0f * Mth.sin(t * 0.25f)
                + stoned * 1.4f * Mth.sin(t * 0.45f)
                + sedated * 2.2f * Mth.sin(t * 0.3f)
                + withdrawal * 0.6f * Mth.sin(t * 23f)
                + overdose * 5.0f * Mth.sin(t * 1.1f);
        float meth = DrugVisuals.get(DrugVisuals.METH, pt);
        float ecstasy = DrugVisuals.get(DrugVisuals.ECSTASY, pt);
        // Meth: nervous twitches (short, sharp jitters)
        float twitch = (float) Math.pow(Math.max(0, Mth.sin(t * 2.3f) * Mth.sin(t * 3.7f)), 6) * 8f;
        roll += meth * twitch * Mth.sin(t * 61f) * 0.8f + ecstasy * 1.2f * Mth.sin(t * 0.8f);
        event.setYaw(event.getYaw() + meth * twitch * Mth.sin(t * 47f) * 0.5f);
        event.setPitch(event.getPitch() + meth * twitch * Mth.cos(t * 53f) * 0.35f);
        event.setRoll(event.getRoll() + roll);
        event.setYaw(event.getYaw() + acid * 2.5f * Mth.sin(t * 0.21f) + trip * 1.5f * Mth.sin(t * 0.37f) + withdrawal * 0.3f * Mth.sin(t * 31f));
        event.setPitch(event.getPitch() + trip * 1.2f * Mth.cos(t * 0.41f) + stoned * 0.6f * Mth.sin(t * 0.33f));
    }

    @SubscribeEvent
    public static void onFov(ComputeFovModifierEvent event) {
        if (!HighCraftConfig.ENABLE_CAMERA_EFFECTS.get() || !DrugVisuals.anyActive()) return;
        float t = DrugVisuals.time(1f);
        float trip = DrugVisuals.get(DrugVisuals.TRIP, 1f);
        float rush = DrugVisuals.get(DrugVisuals.RUSH, 1f);
        float sedated = DrugVisuals.get(DrugVisuals.SEDATED, 1f);
        float beat = (float) Math.pow(Math.max(0, Mth.sin(t * 8f)), 8);
        float acid = DrugVisuals.get(DrugVisuals.ACID, 1f);
        float meth = DrugVisuals.get(DrugVisuals.METH, 1f);
        float ecstasy = DrugVisuals.get(DrugVisuals.ECSTASY, 1f);
        float danceBeat = (float) Math.pow(Math.max(0, Mth.sin(t * 6.7f)), 6);
        float mod = 1f + acid * 0.12f * Mth.sin(t * 0.6f) + trip * 0.06f * Mth.sin(t * 1.2f) + rush * 0.035f * beat - sedated * 0.04f
                - meth * 0.07f + ecstasy * 0.04f * danceBeat;
        event.setNewFovModifier(event.getNewFovModifier() * mod);
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (Quality.has(event.getItemStack())) {
            event.getToolTip().add(1, Quality.tooltip(Quality.get(event.getItemStack())));
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        DrugVisuals.reset();
    }
}
