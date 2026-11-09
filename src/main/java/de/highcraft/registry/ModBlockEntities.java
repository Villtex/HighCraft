package de.highcraft.registry;

import de.highcraft.HighCraft;
import de.highcraft.block.GrowLampBlock;
import de.highcraft.block.MachineType;
import de.highcraft.block.entity.DryingRackBlockEntity;
import de.highcraft.block.entity.GrowTentBlockEntity;
import de.highcraft.block.entity.ProcessingMachineBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, HighCraft.MODID);

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DryingRackBlockEntity>> DRYING_RACK = BLOCK_ENTITIES.register("drying_rack",
            () -> BlockEntityType.Builder.of(DryingRackBlockEntity::new, ModBlocks.DRYING_RACK.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProcessingMachineBlockEntity>> GRINDER = processing("grinder", MachineType.GRINDER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProcessingMachineBlockEntity>> EXTRACTOR = processing("extractor", MachineType.EXTRACTOR);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProcessingMachineBlockEntity>> LAB_STATION = processing("lab_station", MachineType.LAB);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProcessingMachineBlockEntity>> PRESS = processing("press", MachineType.PRESS);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProcessingMachineBlockEntity>> CHEMICAL_REACTOR = processing("chemical_reactor", MachineType.REACTOR);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProcessingMachineBlockEntity>> CRYSTALLIZER = processing("crystallizer", MachineType.CRYSTALLIZER);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProcessingMachineBlockEntity>> PILL_PRESS = processing("pill_press", MachineType.PILL_PRESS);

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GrowTentBlockEntity>> GROW_TENT = BLOCK_ENTITIES.register("grow_tent",
            () -> BlockEntityType.Builder.of(GrowTentBlockEntity::new, ModBlocks.GROW_TENT.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GrowLampBlock.GrowLampBlockEntity>> GROW_LAMP = BLOCK_ENTITIES.register("grow_lamp",
            () -> BlockEntityType.Builder.of(GrowLampBlock.GrowLampBlockEntity::new, ModBlocks.GROW_LAMP.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    private static DeferredHolder<BlockEntityType<?>, BlockEntityType<ProcessingMachineBlockEntity>> processing(String name, MachineType type) {
        // The type is only needed after registration - holder reference via array trick
        @SuppressWarnings("unchecked")
        final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProcessingMachineBlockEntity>>[] self = new DeferredHolder[1];
        self[0] = BLOCK_ENTITIES.register(name, () -> BlockEntityType.Builder.<ProcessingMachineBlockEntity>of(
                (pos, state) -> new ProcessingMachineBlockEntity(self[0].get(), pos, state, type),
                switch (type) {
                    case GRINDER -> ModBlocks.GRINDER.get();
                    case EXTRACTOR -> ModBlocks.EXTRACTOR.get();
                    case LAB -> ModBlocks.LAB_STATION.get();
                    case REACTOR -> ModBlocks.CHEMICAL_REACTOR.get();
                    case CRYSTALLIZER -> ModBlocks.CRYSTALLIZER.get();
                    case PILL_PRESS -> ModBlocks.PILL_PRESS.get();
                    default -> ModBlocks.PRESS.get();
                }).build(null));
        return self[0];
    }

    private ModBlockEntities() {}
}
