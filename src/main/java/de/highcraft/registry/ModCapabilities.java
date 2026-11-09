package de.highcraft.registry;

import de.highcraft.block.entity.AbstractMachineBlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/** Connect hoppers/pipes (items) and cables (FE) to all machines. */
public final class ModCapabilities {
    private ModCapabilities() {}

    public static void register(RegisterCapabilitiesEvent event) {
        var machines = java.util.List.of(ModBlockEntities.GRINDER.get(), ModBlockEntities.EXTRACTOR.get(),
                ModBlockEntities.LAB_STATION.get(), ModBlockEntities.PRESS.get(), ModBlockEntities.CHEMICAL_REACTOR.get(),
                ModBlockEntities.CRYSTALLIZER.get(), ModBlockEntities.PILL_PRESS.get());
        for (var type : machines) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, (be, side) -> be.automationHandler());
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, type, (be, side) -> be.energyStorage());
        }
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.GROW_TENT.get(), (be, side) -> be.automationHandler());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.GROW_TENT.get(), AbstractMachineBlockEntity::energyStorageFor);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.DRYING_RACK.get(), (be, side) -> be.automationHandler());
    }
}
