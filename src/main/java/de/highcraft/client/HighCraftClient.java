package de.highcraft.client;

import de.highcraft.HighCraft;
import de.highcraft.client.particle.SmokePuffParticle;
import de.highcraft.client.particle.TripSparkleParticle;
import de.highcraft.client.render.DryingRackRenderer;
import de.highcraft.client.render.GrowTentRenderer;
import de.highcraft.client.screen.MachineScreen;
import de.highcraft.registry.ModBlockEntities;
import de.highcraft.registry.ModMenus;
import de.highcraft.registry.ModParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** Client entry point (only loaded on the physical client). */
@Mod(value = HighCraft.MODID, dist = Dist.CLIENT)
public class HighCraftClient {
    public HighCraftClient(IEventBus modBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modBus.addListener(HighCraftClient::onRegisterScreens);
        modBus.addListener(HighCraftClient::onRegisterRenderers);
        modBus.addListener(HighCraftClient::onRegisterParticles);
        modBus.addListener(HighCraftClient::onRegisterGuiLayers);
        modBus.addListener(HighCraftClient::onRegisterReloadListeners);
    }

    private static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.GRINDER.get(), MachineScreen::new);
        event.register(ModMenus.EXTRACTOR.get(), MachineScreen::new);
        event.register(ModMenus.LAB_STATION.get(), MachineScreen::new);
        event.register(ModMenus.PRESS.get(), MachineScreen::new);
        event.register(ModMenus.GROW_TENT.get(), MachineScreen::new);
        event.register(ModMenus.CHEMICAL_REACTOR.get(), MachineScreen::new);
        event.register(ModMenus.CRYSTALLIZER.get(), MachineScreen::new);
        event.register(ModMenus.PILL_PRESS.get(), MachineScreen::new);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.DRYING_RACK.get(), DryingRackRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.GROW_TENT.get(), GrowTentRenderer::new);
    }

    private static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.SMOKE_PUFF.get(), SmokePuffParticle.Provider::new);
        event.registerSpriteSet(ModParticles.TRIP_SPARKLE.get(), TripSparkleParticle.Provider::new);
    }

    private static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        // Drug shader as the very first HUD layer (after world, hand and any Iris composite, before the HUD)
        event.registerBelowAll(HighCraft.id("drug_post_effect"), DrugPostEffect::renderLayer);
        event.registerAbove(VanillaGuiLayers.CAMERA_OVERLAYS, HighCraft.id("drug_overlay"), DrugHudLayer::render);
    }

    private static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener) rm -> DrugPostEffect.invalidate());
    }
}
