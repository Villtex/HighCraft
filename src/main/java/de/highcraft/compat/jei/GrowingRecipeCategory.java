package de.highcraft.compat.jei;

import de.highcraft.recipe.GrowingRecipe;
import de.highcraft.registry.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

/** Grow tent: seeds (+ optional fertilizer, water) -> harvest with chances. */
public class GrowingRecipeCategory extends AbstractRecipeCategory<RecipeHolder<GrowingRecipe>> {
    private static final int WIDTH = 150, HEIGHT = 66;

    public GrowingRecipeCategory(IGuiHelper gui, RecipeType<RecipeHolder<GrowingRecipe>> type) {
        super(type, Component.translatable("jei.highcraft.category.growing"), gui.createDrawableItemLike(ModItems.GROW_TENT.get()), WIDTH, HEIGHT);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<GrowingRecipe> holder, IFocusGroup focuses) {
        GrowingRecipe recipe = holder.value();
        builder.addInputSlot(4, 2).setStandardSlotBackground().addIngredients(recipe.seed());
        builder.addInputSlot(4, 20).setStandardSlotBackground()
                .addItemStacks(List.of(new ItemStack(ModItems.FERTILIZER.get()), new ItemStack(Items.BONE_MEAL)))
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable("jei.highcraft.optional").withStyle(ChatFormatting.GRAY)));
        builder.addInputSlot(4, 38).setStandardSlotBackground().addItemStack(new ItemStack(Items.WATER_BUCKET));
        List<GrowingRecipe.Output> outs = recipe.outputs();
        for (int i = 0; i < outs.size() && i < 4; i++) {
            GrowingRecipe.Output o = outs.get(i);
            int x = 62 + (i % 2) * 18, y = 6 + (i / 2) * 18;
            var slot = builder.addOutputSlot(x, y).setOutputSlotBackground().addItemStack(o.stack());
            if (o.chance() < 1f) {
                slot.addRichTooltipCallback((view, tooltip) -> tooltip.add(
                        Component.translatable("jei.highcraft.chance", Math.round(o.chance() * 100)).withStyle(ChatFormatting.GOLD)));
            }
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<GrowingRecipe> holder, IFocusGroup focuses) {
        GrowingRecipe recipe = holder.value();
        builder.addAnimatedRecipeArrowWidget(Math.max(20, recipe.growthTime())).setPosition(30, 14);
        builder.addText(Component.translatable("jei.highcraft.grow_time", Math.round(recipe.growthTime() / 20f)), 60, 10)
                .setPosition(100, 8).setColor(0xFF606060);
        builder.addText(Component.translatable("jei.highcraft.water", recipe.water()), 60, 10)
                .setPosition(100, 22).setColor(0xFF3060A0);
        builder.addText(Component.translatable("jei.highcraft.needs_light"), 146, 10)
                .setPosition(4, 57).setColor(0xFF808080);
    }
}
