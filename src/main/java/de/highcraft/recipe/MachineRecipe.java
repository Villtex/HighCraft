package de.highcraft.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
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
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.List;

/**
 * Shared recipe class of all processing machines (drying rack, grinder, extractor, lab, press).
 * Ingredients are shapeless (order doesn't matter) and can require amounts.
 *
 * <pre>
 * {
 *   "type": "highcraft:grinding",
 *   "ingredients": [ { "item": "highcraft:dried_cannabis_bud", "count": 1 } ],
 *   "result": { "id": "highcraft:ground_cannabis", "count": 2 },
 *   "processing_time": 160
 * }
 * </pre>
 */
public class MachineRecipe implements Recipe<MachineRecipeInput> {

    public enum Kind {
        DRYING(1200, 1), GRINDING(160, 1), EXTRACTING(240, 2), LAB(320, 3), PRESSING(200, 1),
        REACTING(400, 3), CRYSTALLIZING(600, 1), PILL_PRESSING(120, 3);

        public final int defaultTime;
        public final int maxIngredients;

        Kind(int defaultTime, int maxIngredients) {
            this.defaultTime = defaultTime;
            this.maxIngredients = maxIngredients;
        }

        public RecipeType<MachineRecipe> type() {
            return switch (this) {
                case DRYING -> ModRecipes.DRYING.get();
                case GRINDING -> ModRecipes.GRINDING.get();
                case EXTRACTING -> ModRecipes.EXTRACTING.get();
                case LAB -> ModRecipes.LAB.get();
                case PRESSING -> ModRecipes.PRESSING.get();
                case REACTING -> ModRecipes.REACTING.get();
                case CRYSTALLIZING -> ModRecipes.CRYSTALLIZING.get();
                case PILL_PRESSING -> ModRecipes.PILL_PRESSING.get();
            };
        }

        public RecipeSerializer<MachineRecipe> serializer() {
            return switch (this) {
                case DRYING -> ModRecipes.DRYING_SERIALIZER.get();
                case GRINDING -> ModRecipes.GRINDING_SERIALIZER.get();
                case EXTRACTING -> ModRecipes.EXTRACTING_SERIALIZER.get();
                case LAB -> ModRecipes.LAB_SERIALIZER.get();
                case PRESSING -> ModRecipes.PRESSING_SERIALIZER.get();
                case REACTING -> ModRecipes.REACTING_SERIALIZER.get();
                case CRYSTALLIZING -> ModRecipes.CRYSTALLIZING_SERIALIZER.get();
                case PILL_PRESSING -> ModRecipes.PILL_PRESSING_SERIALIZER.get();
            };
        }
    }

    private final Kind kind;
    private final List<SizedIngredient> ingredients;
    private final ItemStack result;
    private final int processingTime;

    public MachineRecipe(Kind kind, List<SizedIngredient> ingredients, ItemStack result, int processingTime) {
        this.kind = kind;
        this.ingredients = List.copyOf(ingredients);
        this.result = result;
        this.processingTime = processingTime;
    }

    public Kind kind() { return kind; }
    public List<SizedIngredient> sizedIngredients() { return ingredients; }
    public ItemStack result() { return result; }
    public int processingTime() { return processingTime; }

    @Override
    public boolean matches(MachineRecipeInput input, Level level) {
        return match(input.items()) != null;
    }

    /**
     * Assigns every ingredient its own input slot.
     *
     * @return array {@code slotFor[ingredientIndex]} or {@code null} if it doesn't match.
     */
    public int[] match(List<ItemStack> inputs) {
        int[] assignment = new int[ingredients.size()];
        boolean[] used = new boolean[inputs.size()];
        if (!assign(0, inputs, used, assignment)) return null;
        // All unassigned slots must be empty
        for (int i = 0; i < inputs.size(); i++) {
            if (!used[i] && !inputs.get(i).isEmpty()) return null;
        }
        return assignment;
    }

    private boolean assign(int idx, List<ItemStack> inputs, boolean[] used, int[] assignment) {
        if (idx == ingredients.size()) return true;
        SizedIngredient ing = ingredients.get(idx);
        for (int s = 0; s < inputs.size(); s++) {
            if (used[s]) continue;
            ItemStack stack = inputs.get(s);
            if (!stack.isEmpty() && ing.test(stack)) {
                used[s] = true;
                assignment[idx] = s;
                if (assign(idx + 1, inputs, used, assignment)) return true;
                used[s] = false;
            }
        }
        return false;
    }

    /** Whether the stack matches any ingredient (for slot validation/automation). */
    public boolean isIngredient(ItemStack stack) {
        for (SizedIngredient ing : ingredients) if (ing.ingredient().test(stack)) return true;
        return false;
    }

    @Override
    public ItemStack assemble(MachineRecipeInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return true; }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) { return result; }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        for (SizedIngredient s : ingredients) list.add(s.ingredient());
        return list;
    }

    @Override
    public boolean isSpecial() { return true; } // not in the recipe book

    @Override
    public RecipeSerializer<?> getSerializer() { return kind.serializer(); }

    @Override
    public RecipeType<?> getType() { return kind.type(); }

    // ------------------------------------------------------------------

    public static class Serializer implements RecipeSerializer<MachineRecipe> {
        private final MapCodec<MachineRecipe> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, MachineRecipe> streamCodec;

        public Serializer(Kind kind) {
            Codec<List<SizedIngredient>> list = SizedIngredient.FLAT_CODEC.listOf().validate(l ->
                    l.isEmpty() || l.size() > kind.maxIngredients
                            ? DataResult.error(() -> "Expected 1.." + kind.maxIngredients + " ingredients, got " + l.size())
                            : DataResult.success(l));
            this.codec = RecordCodecBuilder.mapCodec(i -> i.group(
                    list.fieldOf("ingredients").forGetter(MachineRecipe::sizedIngredients),
                    ItemStack.CODEC.fieldOf("result").forGetter(MachineRecipe::result),
                    Codec.INT.optionalFieldOf("processing_time", kind.defaultTime).forGetter(MachineRecipe::processingTime)
            ).apply(i, (ing, res, time) -> new MachineRecipe(kind, ing, res, time)));
            this.streamCodec = StreamCodec.composite(
                    SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), MachineRecipe::sizedIngredients,
                    ItemStack.STREAM_CODEC, MachineRecipe::result,
                    ByteBufCodecs.VAR_INT, MachineRecipe::processingTime,
                    (ing, res, time) -> new MachineRecipe(kind, ing, res, time));
        }

        @Override
        public MapCodec<MachineRecipe> codec() { return codec; }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MachineRecipe> streamCodec() { return streamCodec; }
    }
}
