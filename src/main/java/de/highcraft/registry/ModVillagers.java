package de.highcraft.registry;

import com.google.common.collect.ImmutableSet;
import de.highcraft.HighCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;

public final class ModVillagers {
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, HighCraft.MODID);
    public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(Registries.VILLAGER_PROFESSION, HighCraft.MODID);

    public static final DeferredHolder<PoiType, PoiType> DEALER_POI = POI_TYPES.register("dealer",
            () -> new PoiType(states(ModBlocks.PRECISION_SCALE.get()), 1, 1));
    public static final DeferredHolder<PoiType, PoiType> GROWER_POI = POI_TYPES.register("grower",
            () -> new PoiType(states(ModBlocks.DRYING_RACK.get()), 1, 1));

    /** Dealer - workstation: precision scale. Buys finished products, sells accessories and rare seeds. */
    public static final DeferredHolder<VillagerProfession, VillagerProfession> DEALER = PROFESSIONS.register("dealer",
            () -> new VillagerProfession(HighCraft.id("dealer").toString(),
                    h -> h.is(DEALER_POI.getKey()), h -> h.is(DEALER_POI.getKey()),
                    ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_CARTOGRAPHER));

    /** Grower - workstation: drying rack. Buys harvests, sells seeds, fertilizer and growing equipment. */
    public static final DeferredHolder<VillagerProfession, VillagerProfession> GROWER = PROFESSIONS.register("grower",
            () -> new VillagerProfession(HighCraft.id("grower").toString(),
                    h -> h.is(GROWER_POI.getKey()), h -> h.is(GROWER_POI.getKey()),
                    ImmutableSet.of(ModItems.CANNABIS_SEEDS.get(), ModItems.TOBACCO_SEEDS.get()), ImmutableSet.of(),
                    SoundEvents.VILLAGER_WORK_FARMER));

    private static Set<BlockState> states(Block block) {
        return ImmutableSet.copyOf(block.getStateDefinition().getPossibleStates());
    }

    private ModVillagers() {}
}
