package de.highcraft;

import de.highcraft.config.HighCraftConfig;
import de.highcraft.registry.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HighCraft - growing, processing and trading.
 * Mod entry point: registers all DeferredRegisters on the mod bus.
 */
@Mod(HighCraft.MODID)
public class HighCraft {
    public static final String MODID = "highcraft";
    public static final Logger LOGGER = LoggerFactory.getLogger("HighCraft");

    public HighCraft(IEventBus modBus, ModContainer container) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModArmorMaterials.MATERIALS.register(modBus);
        ModDataComponents.COMPONENTS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModMenus.MENUS.register(modBus);
        ModRecipes.RECIPE_TYPES.register(modBus);
        ModRecipes.RECIPE_SERIALIZERS.register(modBus);
        ModEffects.EFFECTS.register(modBus);
        ModAttachments.ATTACHMENTS.register(modBus);
        ModParticles.PARTICLES.register(modBus);
        ModVillagers.POI_TYPES.register(modBus);
        ModVillagers.PROFESSIONS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        ModLootFunctions.FUNCTIONS.register(modBus);

        modBus.addListener(ModCapabilities::register);

        container.registerConfig(ModConfig.Type.COMMON, HighCraftConfig.COMMON_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, HighCraftConfig.CLIENT_SPEC);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
