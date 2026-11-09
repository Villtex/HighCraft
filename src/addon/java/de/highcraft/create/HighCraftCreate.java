package de.highcraft.create;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * HighCraft: Create - hooks HighCraft processing into Create machines.
 * <p>
 * Most of it is datapack recipes (data/highcraft_create/recipe). Only the intermediate items
 * of sequenced assembly are registered here - like Create's own, they show a progress bar.
 */
@Mod(HighCraftCreate.MODID)
public class HighCraftCreate {
    public static final String MODID = "highcraft_create";

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredItem<Item> INCOMPLETE_JOINT = incomplete("incomplete_joint");
    public static final DeferredItem<Item> INCOMPLETE_SPLIFF = incomplete("incomplete_spliff");
    public static final DeferredItem<Item> INCOMPLETE_CIGARETTE = incomplete("incomplete_cigarette");
    public static final DeferredItem<Item> INCOMPLETE_CIGAR = incomplete("incomplete_cigar");
    public static final DeferredItem<Item> INCOMPLETE_BLOTTER_SHEET = incomplete("incomplete_blotter_sheet");
    public static final DeferredItem<Item> INCOMPLETE_SYRINGE = incomplete("incomplete_syringe");

    private static DeferredItem<Item> incomplete(String name) {
        return ITEMS.register(name, () -> new SequencedAssemblyItem(new Item.Properties().stacksTo(1)));
    }

    public HighCraftCreate(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
