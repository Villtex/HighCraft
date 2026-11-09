package de.highcraft.compat.jei;

import de.highcraft.recipe.MachineRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.Arrays;
import java.util.List;

/** Category for drying rack, grinder, extractor, chemistry lab and press. */
public class MachineRecipeCategory extends AbstractRecipeCategory<RecipeHolder<MachineRecipe>> {
    private static final int WIDTH = 132, HEIGHT = 44;
    private final MachineRecipe.Kind kind;
    private final boolean usesFuel;

    public MachineRecipeCategory(IGuiHelper gui, RecipeType<RecipeHolder<MachineRecipe>> type, MachineRecipe.Kind kind, ItemLike icon, boolean usesFuel) {
        super(type, Component.translatable("jei.highcraft.category." + type.getUid().getPath()),
                gui.createDrawableItemLike(icon), WIDTH, HEIGHT);
        this.kind = kind;
        this.usesFuel = usesFuel;
    }

    private int arrowX() { return 6 + 18 * kind.maxIngredients + 6; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<MachineRecipe> holder, IFocusGroup focuses) {
        MachineRecipe recipe = holder.value();
        List<SizedIngredient> ings = recipe.sizedIngredients();
        for (int i = 0; i < ings.size(); i++) {
            builder.addInputSlot(6 + i * 18, 4)
                    .setStandardSlotBackground()
                    .addItemStacks(Arrays.asList(ings.get(i).getItems()));
        }
        // Draw empty slots anyway so the layout matches the machine
        builder.addOutputSlot(arrowX() + 32, 4)
                .setOutputSlotBackground()
                .addItemStack(recipe.result());
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<MachineRecipe> holder, IFocusGroup focuses) {
        MachineRecipe recipe = holder.value();
        builder.addAnimatedRecipeArrowWidget(Math.max(20, recipe.processingTime())).setPosition(arrowX(), 5);
        if (usesFuel) builder.addAnimatedRecipeFlameWidget(300).setPosition(6, 25);
        float seconds = recipe.processingTime() / 20f;
        Component time = kind == MachineRecipe.Kind.DRYING
                ? Component.translatable("jei.highcraft.drying_time", Math.round(seconds / 2f))
                : Component.translatable("jei.highcraft.time", String.format(java.util.Locale.ROOT, "%.1f", seconds));
        builder.addText(time, WIDTH - (usesFuel ? 26 : 6), 10).setPosition(usesFuel ? 26 : 6, 30).setColor(0xFF606060);
        if (kind == MachineRecipe.Kind.CRYSTALLIZING) {
            builder.addText(Component.translatable("jei.highcraft.needs_cold"), WIDTH - 50, 10).setPosition(50, 30).setColor(0xFF1E5AA8);
        } else if (kind == MachineRecipe.Kind.REACTING) {
            builder.addText(Component.translatable("jei.highcraft.needs_ventilation"), WIDTH - 60, 10).setPosition(60, 30).setColor(0xFF2E7D32);
        }
    }
}
