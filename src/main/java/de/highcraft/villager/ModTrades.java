package de.highcraft.villager;

import de.highcraft.drug.Quality;
import de.highcraft.registry.ModItems;
import de.highcraft.registry.ModVillagers;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

import java.util.List;
import java.util.function.Supplier;

/** Trades of the dealer, the grower, the chemist and the wandering trader. */
public final class ModTrades {
    private ModTrades() {}

    /** Villager buys goods for emeralds. */
    private static VillagerTrades.ItemListing buy(Supplier<? extends Item> item, int count, int emeralds, int maxUses, int xp) {
        return (trader, random) -> new MerchantOffer(new ItemCost(item.get(), count), new ItemStack(Items.EMERALD, emeralds), maxUses, xp, 0.05f);
    }

    /** Villager sells goods for emeralds. */
    private static VillagerTrades.ItemListing sell(Supplier<? extends Item> item, int count, int emeralds, int maxUses, int xp) {
        return (trader, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, emeralds), new ItemStack(item.get(), count), maxUses, xp, 0.05f);
    }

    /** Sale with quality (the dealer offers "premium goods"). */
    private static VillagerTrades.ItemListing sellQuality(Supplier<? extends Item> item, int count, int emeralds, int quality, int maxUses, int xp) {
        return (trader, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, emeralds),
                Quality.set(new ItemStack(item.get(), count), quality), maxUses, xp, 0.05f);
    }

    public static void addVillagerTrades(VillagerTradesEvent event) {
        Int2ObjectMap<List<VillagerTrades.ItemListing>> t = event.getTrades();
        if (event.getType() == ModVillagers.DEALER.get()) {
            t.get(1).add(sell(ModItems.ROLLING_PAPER, 8, 1, 16, 2));
            t.get(1).add(sell(ModItems.LIGHTER, 1, 2, 8, 2));
            t.get(1).add(buy(ModItems.JOINT, 3, 2, 16, 3));
            t.get(1).add(buy(ModItems.CIGARETTE, 6, 1, 16, 2));
            t.get(2).add(buy(ModItems.HASHISH, 2, 4, 12, 8));
            t.get(2).add(sell(ModItems.SMOKING_PIPE, 1, 4, 6, 6));
            t.get(2).add(sell(ModItems.DRUG_TEST, 1, 3, 6, 5));
            t.get(3).add(buy(ModItems.COCAINE, 1, 7, 10, 15));
            t.get(3).add(sell(ModItems.SOLVENT, 2, 3, 12, 10));
            t.get(3).add(sell(ModItems.COCA_SEEDS, 1, 6, 6, 10));
            t.get(4).add(buy(ModItems.OPIUM, 1, 5, 10, 18));
            t.get(4).add(sell(ModItems.REAGENT, 1, 4, 12, 15));
            t.get(4).add(sell(ModItems.POPPY_SEEDS, 2, 5, 6, 12));
            t.get(5).add(buy(ModItems.MORPHINE_SYRINGE, 1, 10, 8, 30));
            t.get(5).add(buy(ModItems.LSD_BLOTTER, 2, 9, 8, 30));
            t.get(4).add(sell(ModItems.ERGOT, 2, 5, 6, 15));
            t.get(5).add(sell(ModItems.MUSHROOM_SPORES, 2, 10, 4, 30));
            t.get(5).add(sellQuality(ModItems.DRIED_CANNABIS_BUD, 4, 12, 5, 4, 30));
            t.get(2).add(sell(ModItems.GAS_MASK, 1, 6, 4, 6));
            t.get(2).add(sell(ModItems.GLASS_PIPE, 1, 4, 6, 6));
            t.get(3).add(buy(ModItems.ECSTASY_PILL_PINK, 3, 6, 10, 15));
            t.get(3).add(buy(ModItems.ECSTASY_PILL_BLUE, 3, 6, 10, 15));
            t.get(3).add(sell(ModItems.CATALYST, 4, 5, 8, 12));
            t.get(4).add(buy(ModItems.CRYSTAL_METH, 1, 9, 10, 20));
            t.get(4).add(sell(ModItems.PRECURSOR, 2, 6, 8, 15));
            t.get(5).add(sell(ModItems.HAZMAT_SUIT, 1, 14, 3, 25));
        } else if (event.getType() == ModVillagers.CHEMIST.get()) {
            // novice: basic lab supplies, buys glass bottles and coal
            t.get(1).add(buy(() -> Items.GLASS_BOTTLE, 9, 1, 16, 2));
            t.get(1).add(buy(() -> Items.COAL, 15, 1, 16, 2));
            t.get(1).add(sell(ModItems.SOLVENT, 2, 2, 12, 1));
            t.get(1).add(sell(ModItems.LAB_TILES, 8, 1, 16, 1));
            // apprentice: reagents, lab furniture
            t.get(2).add(sell(ModItems.REAGENT, 1, 3, 12, 5));
            t.get(2).add(sell(ModItems.GLASSWARE_SHELF, 1, 2, 12, 5));
            t.get(2).add(sell(ModItems.LAB_COUNTER, 2, 3, 12, 5));
            t.get(2).add(buy(() -> Items.SUGAR, 20, 1, 16, 5));
            // journeyman: precursor chemicals, ventilation, safety gear
            t.get(3).add(sell(ModItems.CATALYST, 2, 4, 10, 10));
            t.get(3).add(sell(ModItems.FUME_HOOD, 1, 8, 4, 10));
            t.get(3).add(sell(ModItems.GAS_MASK, 1, 5, 6, 10));
            t.get(3).add(buy(ModItems.MDMA_POWDER, 2, 5, 10, 15));
            // expert: precursor, exhaust fan, hazmat parts, buys finished crystals
            t.get(4).add(sell(ModItems.PRECURSOR, 2, 5, 8, 15));
            t.get(4).add(sell(ModItems.EXHAUST_FAN, 1, 6, 4, 15));
            t.get(4).add(sell(ModItems.HAZMAT_BOOTS, 1, 6, 4, 15));
            t.get(4).add(sell(ModItems.HAZMAT_PANTS, 1, 8, 4, 15));
            t.get(4).add(buy(ModItems.CRYSTAL_METH, 1, 8, 10, 20));
            // master: the full suit and the lab machines themselves
            t.get(5).add(sell(ModItems.HAZMAT_SUIT, 1, 12, 3, 30));
            t.get(5).add(sell(ModItems.CRYSTALLIZER, 1, 20, 2, 30));
            t.get(5).add(sell(ModItems.PILL_PRESS, 1, 18, 2, 30));
        } else if (event.getType() == ModVillagers.GROWER.get()) {
            t.get(1).add(buy(ModItems.CANNABIS_LEAF, 12, 1, 16, 2));
            t.get(1).add(buy(ModItems.TOBACCO_LEAF, 10, 1, 16, 2));
            t.get(1).add(sell(ModItems.CANNABIS_SEEDS, 3, 1, 16, 1));
            t.get(1).add(sell(ModItems.FERTILIZER, 4, 1, 16, 1));
            t.get(2).add(buy(ModItems.DRIED_CANNABIS_BUD, 6, 2, 12, 6));
            t.get(2).add(sell(ModItems.TOBACCO_SEEDS, 3, 1, 12, 5));
            t.get(2).add(sell(ModItems.SCORING_KNIFE, 1, 3, 6, 5));
            t.get(3).add(buy(ModItems.POPPY_POD, 6, 2, 12, 10));
            t.get(3).add(sell(ModItems.GROW_LAMP, 1, 6, 6, 10));
            t.get(3).add(sell(ModItems.COCA_SEEDS, 2, 4, 8, 10));
            t.get(4).add(buy(ModItems.COCA_LEAVES, 16, 2, 12, 15));
            t.get(4).add(sell(ModItems.GROW_TENT, 1, 18, 3, 15));
            t.get(4).add(sell(ModItems.DETOX_TEA, 2, 3, 8, 10));
            t.get(5).add(sell(ModItems.MUSHROOM_SPORES, 2, 8, 6, 30));
            t.get(5).add(sell(ModItems.FERTILIZER, 24, 4, 8, 20));
        }
    }

    public static void addWandererTrades(WandererTradesEvent event) {
        event.getGenericTrades().add(sell(ModItems.CANNABIS_SEEDS, 2, 2, 6, 1));
        event.getGenericTrades().add(sell(ModItems.TOBACCO_SEEDS, 2, 2, 6, 1));
        event.getGenericTrades().add(sell(ModItems.POPPY_SEEDS, 2, 3, 4, 1));
        event.getRareTrades().add(sell(ModItems.COCA_SEEDS, 2, 6, 3, 1));
        event.getRareTrades().add(sell(ModItems.MUSHROOM_SPORES, 2, 8, 3, 1));
        event.getRareTrades().add(sell(ModItems.ERGOT, 3, 6, 3, 1));
        event.getRareTrades().add(sell(ModItems.GAS_MASK, 1, 8, 2, 1));
    }
}
