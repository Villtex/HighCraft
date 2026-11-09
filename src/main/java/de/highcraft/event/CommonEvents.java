package de.highcraft.event;

import de.highcraft.HighCraft;
import de.highcraft.command.HighCraftCommand;
import de.highcraft.drug.DrugHandler;
import de.highcraft.drug.PlayerDrugData;
import de.highcraft.registry.ModAttachments;
import de.highcraft.registry.ModEffects;
import de.highcraft.villager.ModTrades;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

@EventBusSubscriber(modid = HighCraft.MODID)
public final class CommonEvents {
    private CommonEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer sp && sp.tickCount % 20 == 0) {
            DrugHandler.tickPlayer(sp);
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance inst = event.getEffectInstance();
        if (inst == null) return;
        if (inst.is(ModEffects.RUSH.getDelegate()) && event.getEntity() instanceof Player player) {
            // Don't add directly (ConcurrentModification during tickEffects) - queue it
            player.getData(ModAttachments.DRUG_DATA).setPendingCrash(inst.getAmplifier() + 1);
        }
        if ((inst.is(ModEffects.METH_HIGH.getDelegate()) || inst.is(ModEffects.EUPHORIA.getDelegate())) && event.getEntity() instanceof Player player) {
            player.getData(ModAttachments.DRUG_DATA).setPendingComedown(inst.getAmplifier() + 1);
        }
    }

    /** Nobody sleeps while on meth. */
    @SubscribeEvent
    public static void onCanSleep(net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent event) {
        if (event.getEntity().hasEffect(ModEffects.METH_HIGH.getDelegate())) {
            event.setProblem(Player.BedSleepingProblem.OTHER_PROBLEM);
            event.getEntity().displayClientMessage(net.minecraft.network.chat.Component.translatable("message.highcraft.too_wired"), true);
        }
    }

    /** Full hazmat suit: immune to lab accidents and toxic fumes. */
    @SubscribeEvent
    public static void onIncomingDamage(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event) {
        var source = event.getSource();
        if ((source.is(de.highcraft.lab.LabSafety.LAB_ACCIDENT) || source.is(de.highcraft.lab.LabSafety.TOXIC_FUMES))
                && de.highcraft.lab.LabSafety.hasFullHazmat(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!event.isEndConquered()) {
            PlayerDrugData data = event.getEntity().getData(ModAttachments.DRUG_DATA);
            data.setIntoxication(0);
            data.setPendingCrash(0);
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        HighCraftCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        ModTrades.addVillagerTrades(event);
    }

    @SubscribeEvent
    public static void onWandererTrades(WandererTradesEvent event) {
        ModTrades.addWandererTrades(event);
    }
}
