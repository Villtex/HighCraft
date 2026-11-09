package de.highcraft.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.highcraft.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;

/**
 * Grow tent recipe: seeds -> harvest (with chances).
 * {@code display_block} decides which plant is rendered inside the tent.
 */
public class GrowingRecipe implements Recipe<MachineRecipeInput> {

    public record Output(ItemStack stack, float chance) {
        public static final Codec<Output> CODEC = RecordCodecBuilder.create(i -> i.group(
                ItemStack.CODEC.fieldOf("item").forGetter(Output::stack),
                Codec.floatRange(0f, 1f).optionalFieldOf("chance", 1f).forGetter(Output::chance)
        ).apply(i, Output::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Output> STREAM_CODEC = StreamCodec.composite(
                ItemStack.STREAM_CODEC, Output::stack,
                ByteBufCodecs.FLOAT, Output::chance,
                Output::new);
    }

    private final Ingredient seed;
    private final List<Output> outputs;
    private final int growthTime;
    private final int water;
    private final Block displayBlock;

    public GrowingRecipe(Ingredient seed, List<Output> outputs, int growthTime, int water, Block displayBlock) {
        this.seed = seed;
        this.outputs = List.copyOf(outputs);
        this.growthTime = growthTime;
        this.water = water;
        this.displayBlock = displayBlock;
    }

    public Ingredient seed() { return seed; }
    public List<Output> outputs() { return outputs; }
    public int growthTime() { return growthTime; }
    public int water() { return water; }
    public Block displayBlock() { return displayBlock; }

    @Override
    public boolean matches(MachineRecipeInput input, Level level) {
        return !input.getItem(0).isEmpty() && seed.test(input.getItem(0));
    }

    @Override
    public ItemStack assemble(MachineRecipeInput input, HolderLookup.Provider registries) {
        return outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0).stack().copy();
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) { return true; }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0).stack();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() { return NonNullList.of(Ingredient.EMPTY, seed); }

    @Override
    public boolean isSpecial() { return true; }

    @Override
    public RecipeSerializer<?> getSerializer() { return ModRecipes.GROWING_SERIALIZER.get(); }

    @Override
    public RecipeType<?> getType() { return ModRecipes.GROWING.get(); }

    public static class Serializer implements RecipeSerializer<GrowingRecipe> {
        public static final MapCodec<GrowingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("seed").forGetter(GrowingRecipe::seed),
                Output.CODEC.listOf().fieldOf("outputs").forGetter(GrowingRecipe::outputs),
                Codec.INT.optionalFieldOf("growth_time", 2400).forGetter(GrowingRecipe::growthTime),
                Codec.INT.optionalFieldOf("water", 250).forGetter(GrowingRecipe::water),
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("display_block").forGetter(GrowingRecipe::displayBlock)
        ).apply(i, GrowingRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, GrowingRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, GrowingRecipe::seed,
                Output.STREAM_CODEC.apply(ByteBufCodecs.list()), GrowingRecipe::outputs,
                ByteBufCodecs.VAR_INT, GrowingRecipe::growthTime,
                ByteBufCodecs.VAR_INT, GrowingRecipe::water,
                ByteBufCodecs.registry(net.minecraft.core.registries.Registries.BLOCK), GrowingRecipe::displayBlock,
                GrowingRecipe::new);

        @Override
        public MapCodec<GrowingRecipe> codec() { return CODEC; }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, GrowingRecipe> streamCodec() { return STREAM_CODEC; }
    }
}
