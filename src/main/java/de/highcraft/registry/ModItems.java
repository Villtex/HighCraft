package de.highcraft.registry;

import de.highcraft.HighCraft;
import de.highcraft.drug.DrugDoses;
import de.highcraft.item.*;
import de.highcraft.item.DrugItem.Style;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HighCraft.MODID);

    private static Item.Properties p() { return new Item.Properties(); }

    private static DeferredItem<Item> tip(String name) {
        return ITEMS.register(name, () -> new TooltipItem(p(), name));
    }

    // ---------------- Cannabis ----------------
    public static final DeferredItem<Item> CANNABIS_SEEDS = ITEMS.register("cannabis_seeds",
            () -> new ItemNameBlockItem(ModBlocks.CANNABIS_CROP.get(), p()));
    public static final DeferredItem<Item> CANNABIS_LEAF = tip("cannabis_leaf");
    public static final DeferredItem<Item> CANNABIS_BUD = tip("cannabis_bud");
    public static final DeferredItem<Item> DRIED_CANNABIS_BUD = tip("dried_cannabis_bud");
    public static final DeferredItem<Item> GROUND_CANNABIS = tip("ground_cannabis");
    public static final DeferredItem<Item> CANNABIS_RESIN = tip("cannabis_resin");
    public static final DeferredItem<Item> HEMP_FIBER = tip("hemp_fiber");
    public static final DeferredItem<Item> ROLLING_PAPER = tip("rolling_paper");
    public static final DeferredItem<Item> HASHISH = ITEMS.register("hashish",
            () -> new DrugItem(p(), DrugDoses.HASHISH, Style.SMOKE));
    public static final DeferredItem<Item> JOINT = ITEMS.register("joint",
            () -> new DrugItem(p().stacksTo(16), DrugDoses.JOINT, Style.SMOKE));
    public static final DeferredItem<Item> SPLIFF = ITEMS.register("spliff",
            () -> new DrugItem(p().stacksTo(16), DrugDoses.SPLIFF, Style.SMOKE));
    public static final DeferredItem<Item> SPACE_COOKIE = ITEMS.register("space_cookie",
            () -> new DrugItem(p().food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.3f).alwaysEdible().build()),
                    DrugDoses.SPACE_COOKIE, Style.EAT));

    // ---------------- Tobacco ----------------
    public static final DeferredItem<Item> TOBACCO_SEEDS = ITEMS.register("tobacco_seeds",
            () -> new ItemNameBlockItem(ModBlocks.TOBACCO_CROP.get(), p()));
    public static final DeferredItem<Item> TOBACCO_LEAF = tip("tobacco_leaf");
    public static final DeferredItem<Item> DRIED_TOBACCO = tip("dried_tobacco");
    public static final DeferredItem<Item> CUT_TOBACCO = tip("cut_tobacco");
    public static final DeferredItem<Item> CIGARETTE = ITEMS.register("cigarette",
            () -> new DrugItem(p().stacksTo(20), DrugDoses.CIGARETTE, Style.SMOKE));
    public static final DeferredItem<Item> CIGAR = ITEMS.register("cigar",
            () -> new DrugItem(p().stacksTo(16).rarity(Rarity.UNCOMMON), DrugDoses.CIGAR, Style.SMOKE));

    // ---------------- Coca ----------------
    public static final DeferredItem<Item> COCA_SEEDS = ITEMS.register("coca_seeds",
            () -> new ItemNameBlockItem(ModBlocks.COCA_BUSH.get(), p()));
    public static final DeferredItem<Item> COCA_LEAVES = tip("coca_leaves");
    public static final DeferredItem<Item> DRIED_COCA_LEAVES = tip("dried_coca_leaves");
    public static final DeferredItem<Item> COCA_PASTE = tip("coca_paste");
    public static final DeferredItem<Item> COCAINE = ITEMS.register("cocaine",
            () -> new DrugItem(p().rarity(Rarity.UNCOMMON), DrugDoses.COCAINE, Style.SNORT));
    public static final DeferredItem<Item> COCA_TEA = ITEMS.register("coca_tea",
            () -> new DrugItem(p().stacksTo(16), DrugDoses.COCA_TEA, Style.DRINK, () -> Items.GLASS_BOTTLE));

    // ---------------- Poppy ----------------
    public static final DeferredItem<Item> POPPY_SEEDS = ITEMS.register("poppy_seeds",
            () -> new ItemNameBlockItem(ModBlocks.POPPY_CROP.get(), p()));
    public static final DeferredItem<Item> POPPY_POD = tip("poppy_pod");
    public static final DeferredItem<Item> RAW_OPIUM = tip("raw_opium");
    public static final DeferredItem<Item> OPIUM = tip("opium");
    public static final DeferredItem<Item> MORPHINE = ITEMS.register("morphine",
            () -> new TooltipItem(p().rarity(Rarity.UNCOMMON), "morphine"));
    public static final DeferredItem<Item> EMPTY_SYRINGE = tip("empty_syringe");
    public static final DeferredItem<Item> MORPHINE_SYRINGE = ITEMS.register("morphine_syringe",
            () -> new DrugItem(p().stacksTo(16).rarity(Rarity.UNCOMMON), DrugDoses.MORPHINE, Style.INJECT, EMPTY_SYRINGE));

    // ---------------- Magic mushrooms ----------------
    public static final DeferredItem<Item> MUSHROOM_SPORES = ITEMS.register("mushroom_spores",
            () -> new ItemNameBlockItem(ModBlocks.MAGIC_MUSHROOM.get(), p()));
    public static final DeferredItem<Item> MAGIC_MUSHROOM = ITEMS.register("magic_mushroom",
            () -> new DrugItem(p().food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.1f).alwaysEdible().build()),
                    DrugDoses.MAGIC_MUSHROOM, Style.EAT));
    public static final DeferredItem<Item> DRIED_MAGIC_MUSHROOM = ITEMS.register("dried_magic_mushroom",
            () -> new DrugItem(p().food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.1f).alwaysEdible().fast().build()),
                    DrugDoses.DRIED_MAGIC_MUSHROOM, Style.EAT));
    public static final DeferredItem<Item> MUSHROOM_TEA = ITEMS.register("mushroom_tea",
            () -> new DrugItem(p().stacksTo(16).rarity(Rarity.UNCOMMON), DrugDoses.MUSHROOM_TEA, Style.DRINK, () -> Items.GLASS_BOTTLE));

    // ---------------- LSD ----------------
    public static final DeferredItem<Item> ERGOT = tip("ergot");
    public static final DeferredItem<Item> LSD_SOLUTION = ITEMS.register("lsd_solution",
            () -> new TooltipItem(p().stacksTo(16).rarity(Rarity.RARE), "lsd_solution"));
    public static final DeferredItem<Item> LSD_BLOTTER = ITEMS.register("lsd_blotter",
            () -> new DrugItem(p().rarity(Rarity.RARE), DrugDoses.LSD_BLOTTER, Style.TAB));

    // ---------------- Chemistry & tools ----------------
    public static final DeferredItem<Item> SOLVENT = tip("solvent");
    public static final DeferredItem<Item> REAGENT = tip("reagent");
    public static final DeferredItem<Item> FERTILIZER = ITEMS.register("fertilizer", () -> new FertilizerItem(p()));
    public static final DeferredItem<Item> SCORING_KNIFE = ITEMS.register("scoring_knife",
            () -> new ScoringKnifeItem(p().durability(128)));
    public static final DeferredItem<Item> SMOKING_PIPE = ITEMS.register("smoking_pipe",
            () -> new SmokingPipeItem(p().durability(64)));
    public static final DeferredItem<Item> LIGHTER = ITEMS.register("lighter",
            () -> new TooltipItem(p().durability(100), "lighter"));
    public static final DeferredItem<Item> DRUG_TEST = ITEMS.register("drug_test", () -> new DrugTestItem(p().stacksTo(1)));
    public static final DeferredItem<Item> DETOX_TEA = ITEMS.register("detox_tea", () -> new DetoxTeaItem(p().stacksTo(16)));

    // ---------------- Meth & ecstasy ----------------
    public static final DeferredItem<Item> PRECURSOR = tip("precursor");
    public static final DeferredItem<Item> CATALYST = tip("catalyst");
    public static final DeferredItem<Item> AROMATIC_OIL = ITEMS.register("aromatic_oil", () -> new TooltipItem(p().stacksTo(16), "aromatic_oil"));
    public static final DeferredItem<Item> METH_BATCH = ITEMS.register("meth_batch", () -> new TooltipItem(p().stacksTo(16), "meth_batch"));
    public static final DeferredItem<Item> CRYSTAL_METH = ITEMS.register("crystal_meth",
            () -> new DrugItem(p().rarity(Rarity.UNCOMMON), DrugDoses.CRYSTAL_METH, Style.SNORT));
    public static final DeferredItem<Item> GLASS_PIPE = ITEMS.register("glass_pipe",
            () -> new SmokingPipeItem(p().durability(48), SmokingPipeItem.GLASS_PIPE_FILLS, "glass_pipe"));
    public static final DeferredItem<Item> MDMA_BATCH = ITEMS.register("mdma_batch", () -> new TooltipItem(p().stacksTo(16), "mdma_batch"));
    public static final DeferredItem<Item> MDMA_CRYSTALS = tip("mdma_crystals");
    public static final DeferredItem<Item> MDMA_POWDER = tip("mdma_powder");
    public static final DeferredItem<Item> ECSTASY_PILL_PINK = pill("ecstasy_pill_pink");
    public static final DeferredItem<Item> ECSTASY_PILL_BLUE = pill("ecstasy_pill_blue");
    public static final DeferredItem<Item> ECSTASY_PILL_YELLOW = pill("ecstasy_pill_yellow");
    public static final DeferredItem<Item> ECSTASY_PILL_PURPLE = pill("ecstasy_pill_purple");

    private static DeferredItem<Item> pill(String name) {
        return ITEMS.register(name, () -> new DrugItem(p().rarity(Rarity.UNCOMMON), DrugDoses.ECSTASY, Style.TAB));
    }

    // ---------------- Protective gear ----------------
    public static final DeferredItem<Item> GAS_MASK = ITEMS.register("gas_mask", () -> new GearItem(ModArmorMaterials.GAS_MASK,
            net.minecraft.world.item.ArmorItem.Type.HELMET, p().durability(net.minecraft.world.item.ArmorItem.Type.HELMET.getDurability(12)), "gas_mask"));
    public static final DeferredItem<Item> HAZMAT_SUIT = ITEMS.register("hazmat_suit", () -> new GearItem(ModArmorMaterials.HAZMAT,
            net.minecraft.world.item.ArmorItem.Type.CHESTPLATE, p().durability(net.minecraft.world.item.ArmorItem.Type.CHESTPLATE.getDurability(12)), "hazmat_suit"));
    public static final DeferredItem<Item> HAZMAT_PANTS = ITEMS.register("hazmat_pants", () -> new GearItem(ModArmorMaterials.HAZMAT,
            net.minecraft.world.item.ArmorItem.Type.LEGGINGS, p().durability(net.minecraft.world.item.ArmorItem.Type.LEGGINGS.getDurability(12)), "hazmat_pants"));
    public static final DeferredItem<Item> HAZMAT_BOOTS = ITEMS.register("hazmat_boots", () -> new GearItem(ModArmorMaterials.HAZMAT,
            net.minecraft.world.item.ArmorItem.Type.BOOTS, p().durability(net.minecraft.world.item.ArmorItem.Type.BOOTS.getDurability(12)), "hazmat_boots"));

    // ---------------- Block items ----------------
    public static final DeferredItem<BlockItem> DRYING_RACK = ITEMS.registerSimpleBlockItem(ModBlocks.DRYING_RACK);
    public static final DeferredItem<BlockItem> GRINDER = ITEMS.registerSimpleBlockItem(ModBlocks.GRINDER);
    public static final DeferredItem<BlockItem> EXTRACTOR = ITEMS.registerSimpleBlockItem(ModBlocks.EXTRACTOR);
    public static final DeferredItem<BlockItem> LAB_STATION = ITEMS.registerSimpleBlockItem(ModBlocks.LAB_STATION);
    public static final DeferredItem<BlockItem> PRESS = ITEMS.registerSimpleBlockItem(ModBlocks.PRESS);
    public static final DeferredItem<BlockItem> GROW_TENT = ITEMS.registerSimpleBlockItem(ModBlocks.GROW_TENT);
    public static final DeferredItem<BlockItem> GROW_LAMP = ITEMS.registerSimpleBlockItem(ModBlocks.GROW_LAMP);
    public static final DeferredItem<BlockItem> BONG = ITEMS.registerSimpleBlockItem(ModBlocks.BONG);
    public static final DeferredItem<BlockItem> PRECISION_SCALE = ITEMS.registerSimpleBlockItem(ModBlocks.PRECISION_SCALE);
    public static final DeferredItem<BlockItem> WILD_CANNABIS = ITEMS.registerSimpleBlockItem(ModBlocks.WILD_CANNABIS);
    public static final DeferredItem<BlockItem> WILD_TOBACCO = ITEMS.registerSimpleBlockItem(ModBlocks.WILD_TOBACCO);
    public static final DeferredItem<BlockItem> WILD_COCA = ITEMS.registerSimpleBlockItem(ModBlocks.WILD_COCA);
    public static final DeferredItem<BlockItem> WILD_POPPY = ITEMS.registerSimpleBlockItem(ModBlocks.WILD_POPPY);
    public static final DeferredItem<BlockItem> CHEMICAL_REACTOR = ITEMS.registerSimpleBlockItem(ModBlocks.CHEMICAL_REACTOR);
    public static final DeferredItem<BlockItem> CRYSTALLIZER = ITEMS.registerSimpleBlockItem(ModBlocks.CRYSTALLIZER);
    public static final DeferredItem<BlockItem> PILL_PRESS = ITEMS.registerSimpleBlockItem(ModBlocks.PILL_PRESS);
    public static final DeferredItem<BlockItem> FUME_HOOD = ITEMS.registerSimpleBlockItem(ModBlocks.FUME_HOOD);
    public static final DeferredItem<BlockItem> EXHAUST_FAN = ITEMS.registerSimpleBlockItem(ModBlocks.EXHAUST_FAN);
    public static final DeferredItem<BlockItem> LAB_TILES = ITEMS.registerSimpleBlockItem(ModBlocks.LAB_TILES);
    public static final DeferredItem<BlockItem> LAB_TILE_SLAB = ITEMS.registerSimpleBlockItem(ModBlocks.LAB_TILE_SLAB);
    public static final DeferredItem<BlockItem> LAB_TILE_STAIRS = ITEMS.registerSimpleBlockItem(ModBlocks.LAB_TILE_STAIRS);
    public static final DeferredItem<BlockItem> HAZARD_STRIPES = ITEMS.registerSimpleBlockItem(ModBlocks.HAZARD_STRIPES);
    public static final DeferredItem<BlockItem> LAB_COUNTER = ITEMS.registerSimpleBlockItem(ModBlocks.LAB_COUNTER);
    public static final DeferredItem<BlockItem> CHEMICAL_BARREL = ITEMS.registerSimpleBlockItem(ModBlocks.CHEMICAL_BARREL);
    public static final DeferredItem<BlockItem> HAZARD_BARREL = ITEMS.registerSimpleBlockItem(ModBlocks.HAZARD_BARREL);
    public static final DeferredItem<BlockItem> GAS_CYLINDER = ITEMS.registerSimpleBlockItem(ModBlocks.GAS_CYLINDER);
    public static final DeferredItem<BlockItem> GLASSWARE_SHELF = ITEMS.registerSimpleBlockItem(ModBlocks.GLASSWARE_SHELF);
    public static final DeferredItem<BlockItem> PLASTIC_SHEETING = ITEMS.registerSimpleBlockItem(ModBlocks.PLASTIC_SHEETING);
    public static final DeferredItem<BlockItem> HAZARD_SIGN = ITEMS.registerSimpleBlockItem(ModBlocks.HAZARD_SIGN);
    public static final DeferredItem<BlockItem> LAB_LIGHT = ITEMS.registerSimpleBlockItem(ModBlocks.LAB_LIGHT);

    private ModItems() {}
}
