package de.highcraft.recipe;

import com.mojang.serialization.MapCodec;
import de.highcraft.drug.Quality;
import de.highcraft.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Shapeless crafting recipe that carries the average quality of the ingredients over to the result.
 * Same JSON format as {@code minecraft:crafting_shapeless}, type {@code highcraft:quality_shapeless}.
 */
public class QualityShapelessRecipe extends ShapelessRecipe {
    public QualityShapelessRecipe(String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients) {
        super(group, category, result, ingredients);
    }

    private QualityShapelessRecipe(ShapelessRecipe base) {
        this(base.getGroup(), base.category(), base.getResultItem(null), base.getIngredients());
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack out = super.assemble(input, registries);
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) stacks.add(input.getItem(i));
        int q = Quality.average(stacks);
        return q > 0 ? Quality.set(out, q) : out;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.QUALITY_SHAPELESS.get();
    }

    public static class Serializer implements RecipeSerializer<QualityShapelessRecipe> {
        private static final MapCodec<QualityShapelessRecipe> CODEC =
                RecipeSerializer.SHAPELESS_RECIPE.codec().xmap(QualityShapelessRecipe::new, r -> r);
        private static final StreamCodec<RegistryFriendlyByteBuf, QualityShapelessRecipe> STREAM_CODEC =
                RecipeSerializer.SHAPELESS_RECIPE.streamCodec().map(QualityShapelessRecipe::new, r -> r);

        @Override
        public MapCodec<QualityShapelessRecipe> codec() { return CODEC; }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, QualityShapelessRecipe> streamCodec() { return STREAM_CODEC; }
    }
}
