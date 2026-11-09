package de.highcraft.registry;

import de.highcraft.HighCraft;
import de.highcraft.recipe.GrowingRecipe;
import de.highcraft.recipe.MachineRecipe;
import de.highcraft.recipe.QualityShapelessRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, HighCraft.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, HighCraft.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> DRYING = type("drying");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> GRINDING = type("grinding");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> EXTRACTING = type("extracting");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> LAB = type("lab");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> PRESSING = type("pressing");
    public static final DeferredHolder<RecipeType<?>, RecipeType<GrowingRecipe>> GROWING = type("growing");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> REACTING = type("reacting");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> CRYSTALLIZING = type("crystallizing");
    public static final DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>> PILL_PRESSING = type("pill_pressing");

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MachineRecipe>> DRYING_SERIALIZER =
            RECIPE_SERIALIZERS.register("drying", () -> new MachineRecipe.Serializer(MachineRecipe.Kind.DRYING));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MachineRecipe>> GRINDING_SERIALIZER =
            RECIPE_SERIALIZERS.register("grinding", () -> new MachineRecipe.Serializer(MachineRecipe.Kind.GRINDING));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MachineRecipe>> EXTRACTING_SERIALIZER =
            RECIPE_SERIALIZERS.register("extracting", () -> new MachineRecipe.Serializer(MachineRecipe.Kind.EXTRACTING));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MachineRecipe>> LAB_SERIALIZER =
            RECIPE_SERIALIZERS.register("lab", () -> new MachineRecipe.Serializer(MachineRecipe.Kind.LAB));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MachineRecipe>> PRESSING_SERIALIZER =
            RECIPE_SERIALIZERS.register("pressing", () -> new MachineRecipe.Serializer(MachineRecipe.Kind.PRESSING));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MachineRecipe>> REACTING_SERIALIZER =
            RECIPE_SERIALIZERS.register("reacting", () -> new MachineRecipe.Serializer(MachineRecipe.Kind.REACTING));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MachineRecipe>> CRYSTALLIZING_SERIALIZER =
            RECIPE_SERIALIZERS.register("crystallizing", () -> new MachineRecipe.Serializer(MachineRecipe.Kind.CRYSTALLIZING));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MachineRecipe>> PILL_PRESSING_SERIALIZER =
            RECIPE_SERIALIZERS.register("pill_pressing", () -> new MachineRecipe.Serializer(MachineRecipe.Kind.PILL_PRESSING));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GrowingRecipe>> GROWING_SERIALIZER =
            RECIPE_SERIALIZERS.register("growing", GrowingRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<QualityShapelessRecipe>> QUALITY_SHAPELESS =
            RECIPE_SERIALIZERS.register("quality_shapeless", QualityShapelessRecipe.Serializer::new);

    private static <T extends net.minecraft.world.item.crafting.Recipe<?>> DeferredHolder<RecipeType<?>, RecipeType<T>> type(String name) {
        return RECIPE_TYPES.register(name, () -> RecipeType.simple(HighCraft.id(name)));
    }

    private ModRecipes() {}
}
