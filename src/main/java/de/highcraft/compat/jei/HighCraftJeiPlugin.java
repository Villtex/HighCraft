package de.highcraft.compat.jei;

import de.highcraft.HighCraft;
import de.highcraft.block.MachineType;
import de.highcraft.client.screen.MachineScreen;
import de.highcraft.menu.MachineMenu;
import de.highcraft.recipe.GrowingRecipe;
import de.highcraft.recipe.MachineRecipe;
import de.highcraft.registry.ModItems;
import de.highcraft.registry.ModMenus;
import de.highcraft.registry.ModRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ItemLike;

import java.util.Collection;
import java.util.List;

/**
 * JEI integration: own categories for all machines, catalysts, clickable progress arrows,
 * recipe transfer (+ button) and info pages for plants and tools.
 */
@JeiPlugin
public class HighCraftJeiPlugin implements IModPlugin {
    public static final RecipeType<RecipeHolder<MachineRecipe>> DRYING = RecipeType.createRecipeHolderType(HighCraft.id("drying"));
    public static final RecipeType<RecipeHolder<MachineRecipe>> GRINDING = RecipeType.createRecipeHolderType(HighCraft.id("grinding"));
    public static final RecipeType<RecipeHolder<MachineRecipe>> EXTRACTING = RecipeType.createRecipeHolderType(HighCraft.id("extracting"));
    public static final RecipeType<RecipeHolder<MachineRecipe>> LAB = RecipeType.createRecipeHolderType(HighCraft.id("lab"));
    public static final RecipeType<RecipeHolder<MachineRecipe>> PRESSING = RecipeType.createRecipeHolderType(HighCraft.id("pressing"));
    public static final RecipeType<RecipeHolder<GrowingRecipe>> GROWING = RecipeType.createRecipeHolderType(HighCraft.id("growing"));
    public static final RecipeType<RecipeHolder<MachineRecipe>> REACTING = RecipeType.createRecipeHolderType(HighCraft.id("reacting"));
    public static final RecipeType<RecipeHolder<MachineRecipe>> CRYSTALLIZING = RecipeType.createRecipeHolderType(HighCraft.id("crystallizing"));
    public static final RecipeType<RecipeHolder<MachineRecipe>> PILL_PRESSING = RecipeType.createRecipeHolderType(HighCraft.id("pill_pressing"));

    /** Runtime reference (e.g. for tests or to open recipes from our own GUIs). */
    public static mezz.jei.api.runtime.IJeiRuntime runtime;

    @Override
    public void onRuntimeAvailable(mezz.jei.api.runtime.IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    @Override
    public ResourceLocation getPluginUid() {
        return HighCraft.id("jei_plugin");
    }

    public static RecipeType<?> typeFor(MachineType machine) {
        return switch (machine) {
            case GRINDER -> GRINDING;
            case EXTRACTOR -> EXTRACTING;
            case LAB -> LAB;
            case PRESS -> PRESSING;
            case GROW_TENT -> GROWING;
            case REACTOR -> REACTING;
            case CRYSTALLIZER -> CRYSTALLIZING;
            case PILL_PRESS -> PILL_PRESSING;
        };
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration reg) {
        IGuiHelper gui = reg.getJeiHelpers().getGuiHelper();
        reg.addRecipeCategories(
                new MachineRecipeCategory(gui, DRYING, MachineRecipe.Kind.DRYING, ModItems.DRYING_RACK.get(), false),
                new MachineRecipeCategory(gui, GRINDING, MachineRecipe.Kind.GRINDING, ModItems.GRINDER.get(), true),
                new MachineRecipeCategory(gui, EXTRACTING, MachineRecipe.Kind.EXTRACTING, ModItems.EXTRACTOR.get(), true),
                new MachineRecipeCategory(gui, LAB, MachineRecipe.Kind.LAB, ModItems.LAB_STATION.get(), true),
                new MachineRecipeCategory(gui, PRESSING, MachineRecipe.Kind.PRESSING, ModItems.PRESS.get(), true),
                new GrowingRecipeCategory(gui, GROWING),
                new MachineRecipeCategory(gui, REACTING, MachineRecipe.Kind.REACTING, ModItems.CHEMICAL_REACTOR.get(), true),
                new MachineRecipeCategory(gui, CRYSTALLIZING, MachineRecipe.Kind.CRYSTALLIZING, ModItems.CRYSTALLIZER.get(), false),
                new MachineRecipeCategory(gui, PILL_PRESSING, MachineRecipe.Kind.PILL_PRESSING, ModItems.PILL_PRESS.get(), true));
    }

    @Override
    public void registerRecipes(IRecipeRegistration reg) {
        if (Minecraft.getInstance().level == null) return;
        RecipeManager rm = Minecraft.getInstance().level.getRecipeManager();
        reg.addRecipes(DRYING, rm.getAllRecipesFor(ModRecipes.DRYING.get()));
        reg.addRecipes(GRINDING, rm.getAllRecipesFor(ModRecipes.GRINDING.get()));
        reg.addRecipes(EXTRACTING, rm.getAllRecipesFor(ModRecipes.EXTRACTING.get()));
        reg.addRecipes(LAB, rm.getAllRecipesFor(ModRecipes.LAB.get()));
        reg.addRecipes(PRESSING, rm.getAllRecipesFor(ModRecipes.PRESSING.get()));
        reg.addRecipes(GROWING, rm.getAllRecipesFor(ModRecipes.GROWING.get()));
        reg.addRecipes(REACTING, rm.getAllRecipesFor(ModRecipes.REACTING.get()));
        reg.addRecipes(CRYSTALLIZING, rm.getAllRecipesFor(ModRecipes.CRYSTALLIZING.get()));
        reg.addRecipes(PILL_PRESSING, rm.getAllRecipesFor(ModRecipes.PILL_PRESSING.get()));

        // Info pages
        info(reg, ModItems.CANNABIS_SEEDS.get(), "cannabis");
        info(reg, ModItems.TOBACCO_SEEDS.get(), "tobacco");
        info(reg, ModItems.COCA_SEEDS.get(), "coca");
        info(reg, ModItems.POPPY_SEEDS.get(), "poppy");
        info(reg, ModItems.RAW_OPIUM.get(), "raw_opium");
        info(reg, ModItems.MUSHROOM_SPORES.get(), "mushroom");
        info(reg, ModItems.FERTILIZER.get(), "fertilizer");
        info(reg, ModItems.ERGOT.get(), "ergot");
        info(reg, ModItems.LSD_BLOTTER.get(), "lsd");
        info(reg, ModItems.GROW_LAMP.get(), "grow_lamp");
        info(reg, ModItems.GROW_TENT.get(), "grow_tent");
        info(reg, ModItems.DRYING_RACK.get(), "drying_rack");
        info(reg, ModItems.BONG.get(), "bong");
        info(reg, ModItems.PRECISION_SCALE.get(), "precision_scale");
        info(reg, ModItems.DRUG_TEST.get(), "drug_test");
        info(reg, ModItems.DETOX_TEA.get(), "detox_tea");
        info(reg, ModItems.LIGHTER.get(), "lighter");
        info(reg, ModItems.SMOKING_PIPE.get(), "smoking_pipe");
        info(reg, ModItems.COCAINE.get(), "addiction");
        info(reg, ModItems.CHEMICAL_REACTOR.get(), "chemical_reactor");
        info(reg, ModItems.CRYSTALLIZER.get(), "crystallizer");
        info(reg, ModItems.PILL_PRESS.get(), "pill_press");
        info(reg, ModItems.FUME_HOOD.get(), "ventilation");
        info(reg, ModItems.EXHAUST_FAN.get(), "ventilation");
        info(reg, ModItems.GAS_MASK.get(), "gas_mask");
        info(reg, ModItems.CRYSTAL_METH.get(), "meth");
        info(reg, ModItems.GLASS_PIPE.get(), "meth");
        for (var pill : List.of(ModItems.ECSTASY_PILL_PINK, ModItems.ECSTASY_PILL_BLUE, ModItems.ECSTASY_PILL_YELLOW, ModItems.ECSTASY_PILL_PURPLE)) {
            info(reg, pill.get(), "ecstasy");
        }
        reg.addItemStackInfo(List.of(new ItemStack(ModItems.GRINDER.get()), new ItemStack(ModItems.EXTRACTOR.get()),
                new ItemStack(ModItems.LAB_STATION.get()), new ItemStack(ModItems.PRESS.get())),
                Component.translatable("jei.highcraft.info.machines"));
    }

    private static void info(IRecipeRegistration reg, ItemLike item, String key) {
        reg.addItemStackInfo(new ItemStack(item), Component.translatable("jei.highcraft.info." + key));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
        reg.addRecipeCatalyst(new ItemStack(ModItems.DRYING_RACK.get()), DRYING);
        reg.addRecipeCatalyst(new ItemStack(ModItems.GRINDER.get()), GRINDING);
        reg.addRecipeCatalyst(new ItemStack(ModItems.EXTRACTOR.get()), EXTRACTING);
        reg.addRecipeCatalyst(new ItemStack(ModItems.LAB_STATION.get()), LAB);
        reg.addRecipeCatalyst(new ItemStack(ModItems.PRESS.get()), PRESSING);
        reg.addRecipeCatalyst(new ItemStack(ModItems.GROW_TENT.get()), GROWING);
        reg.addRecipeCatalyst(new ItemStack(ModItems.CHEMICAL_REACTOR.get()), REACTING);
        reg.addRecipeCatalyst(new ItemStack(ModItems.FUME_HOOD.get()), REACTING);
        reg.addRecipeCatalyst(new ItemStack(ModItems.CRYSTALLIZER.get()), CRYSTALLIZING);
        reg.addRecipeCatalyst(new ItemStack(ModItems.PILL_PRESS.get()), PILL_PRESSING);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration reg) {
        reg.addGuiContainerHandler(MachineScreen.class, new IGuiContainerHandler<>() {
            @Override
            public Collection<IGuiClickableArea> getGuiClickableAreas(MachineScreen screen, double mouseX, double mouseY) {
                MachineType m = screen.machine();
                return List.of(IGuiClickableArea.createBasic(m.arrowPos[0], m.arrowPos[1], 24, 17, typeFor(m)));
            }
        });
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration reg) {
        transfer(reg, MachineType.GRINDER, GRINDING);
        transfer(reg, MachineType.EXTRACTOR, EXTRACTING);
        transfer(reg, MachineType.LAB, LAB);
        transfer(reg, MachineType.PRESS, PRESSING);
        transfer(reg, MachineType.REACTOR, REACTING);
        transfer(reg, MachineType.CRYSTALLIZER, CRYSTALLIZING);
        transfer(reg, MachineType.PILL_PRESS, PILL_PRESSING);
        reg.addRecipeTransferHandler(MachineMenu.class, ModMenus.get(MachineType.GROW_TENT), GROWING,
                0, 1, MachineType.GROW_TENT.slotCount(), 36);
    }

    private static void transfer(IRecipeTransferRegistration reg, MachineType m, RecipeType<RecipeHolder<MachineRecipe>> type) {
        reg.addRecipeTransferHandler(MachineMenu.class, ModMenus.get(m), type, 0, m.inputCount(), m.slotCount(), 36);
    }
}
