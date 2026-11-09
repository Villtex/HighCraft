package de.highcraft.registry;

import com.mojang.serialization.MapCodec;
import de.highcraft.HighCraft;
import de.highcraft.block.CropQuality;
import de.highcraft.drug.Quality;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public final class ModLootFunctions {
    public static final DeferredRegister<LootItemFunctionType<?>> FUNCTIONS = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, HighCraft.MODID);

    /** {@code highcraft:crop_quality} - sets the harvest quality based on the growing conditions. */
    public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<CropQualityFunction>> CROP_QUALITY =
            FUNCTIONS.register("crop_quality", () -> new LootItemFunctionType<>(CropQualityFunction.CODEC));

    public static class CropQualityFunction extends LootItemConditionalFunction {
        public static final MapCodec<CropQualityFunction> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(
                i -> commonFields(i).apply(i, CropQualityFunction::new));

        protected CropQualityFunction(List<LootItemCondition> conditions) {
            super(conditions);
        }

        @Override
        protected ItemStack run(ItemStack stack, LootContext ctx) {
            BlockState state = ctx.getParamOrNull(LootContextParams.BLOCK_STATE);
            Vec3 origin = ctx.getParamOrNull(LootContextParams.ORIGIN);
            if (state == null || origin == null) return stack;
            int q = CropQuality.compute(ctx.getLevel(), BlockPos.containing(origin), state);
            return Quality.set(stack, q);
        }

        @Override
        public LootItemFunctionType<CropQualityFunction> getType() {
            return CROP_QUALITY.get();
        }
    }

    private ModLootFunctions() {}
}
