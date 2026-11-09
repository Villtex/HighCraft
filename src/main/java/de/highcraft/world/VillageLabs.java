package de.highcraft.world;

import com.mojang.datafixers.util.Pair;
import de.highcraft.HighCraft;
import de.highcraft.config.HighCraftConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Adds the drug lab houses ({@code data/highcraft/structure/village/<type>/drug_lab.nbt}) to the vanilla
 * village house pools, so villages sometimes contain a lab. Weight: config {@code villageLabWeight}.
 */
@EventBusSubscriber(modid = HighCraft.MODID)
public final class VillageLabs {
    public static final String[] VILLAGE_TYPES = {"plains", "desert", "savanna", "snowy", "taiga"};
    private static Field templatesField;
    private static Field rawTemplatesField;

    private VillageLabs() {}

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        int weight = HighCraftConfig.VILLAGE_LAB_WEIGHT.get();
        if (weight <= 0) return;
        MinecraftServer server = event.getServer();
        Registry<StructureTemplatePool> pools = server.registryAccess().registryOrThrow(Registries.TEMPLATE_POOL);
        Registry<StructureProcessorList> processors = server.registryAccess().registryOrThrow(Registries.PROCESSOR_LIST);
        Holder<StructureProcessorList> empty = processors.getHolderOrThrow(
                net.minecraft.resources.ResourceKey.create(Registries.PROCESSOR_LIST, ResourceLocation.withDefaultNamespace("empty")));
        int added = 0;
        for (String type : VILLAGE_TYPES) {
            StructureTemplatePool pool = pools.get(ResourceLocation.withDefaultNamespace("village/" + type + "/houses"));
            if (pool == null) continue;
            SinglePoolElement element = SinglePoolElement.single(HighCraft.MODID + ":village/" + type + "/drug_lab", empty)
                    .apply(StructureTemplatePool.Projection.RIGID);
            if (addToPool(pool, element, weight)) added++;
        }
        HighCraft.LOGGER.info("Added drug labs to {} village house pools (weight {})", added, weight);
    }

    @SuppressWarnings("unchecked")
    private static boolean addToPool(StructureTemplatePool pool, StructurePoolElement element, int weight) {
        try {
            if (templatesField == null) {
                templatesField = StructureTemplatePool.class.getDeclaredField("templates");
                templatesField.setAccessible(true);
                rawTemplatesField = StructureTemplatePool.class.getDeclaredField("rawTemplates");
                rawTemplatesField.setAccessible(true);
            }
            List<StructurePoolElement> templates = (List<StructurePoolElement>) templatesField.get(pool);
            for (StructurePoolElement e : templates) {
                if (e.toString().equals(element.toString())) return false; // already added (e.g. second server start)
            }
            for (int i = 0; i < weight; i++) templates.add(element);
            List<Pair<StructurePoolElement, Integer>> raw = new ArrayList<>((List<Pair<StructurePoolElement, Integer>>) rawTemplatesField.get(pool));
            raw.add(Pair.of(element, weight));
            rawTemplatesField.set(pool, raw);
            return true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            HighCraft.LOGGER.error("Could not add drug lab to village pool", e);
            return false;
        }
    }
}
