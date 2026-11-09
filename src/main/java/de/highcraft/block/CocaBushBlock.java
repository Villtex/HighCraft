package de.highcraft.block;

import de.highcraft.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

/**
 * Coca bush - perennial, grows on dirt/grass/farmland, loves warm climates.
 * From stage 3, right-click to pick leaves (back to stage 2).
 */
public class CocaBushBlock extends HighCropBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_4;

    public CocaBushBlock(Properties properties) {
        super(properties, ModItems.COCA_SEEDS);
    }

    @Override
    protected IntegerProperty getAgeProperty() { return AGE; }

    @Override
    public int getMaxAge() { return 4; }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT) || super.mayPlaceOn(state, level, pos);
    }

    @Override
    protected float environmentFactor(BlockState state, ServerLevel level, BlockPos pos) {
        float temp = level.getBiome(pos).value().getBaseTemperature();
        float base = level.getBlockState(pos.below()).is(BlockTags.DIRT) ? 1.6f : 1.0f; // on dirt: base growth like on farmland
        if (temp >= 0.9f) return base * 1.5f;
        if (temp < 0.3f) return base * 0.3f;
        return base;
    }

    @Override
    protected int getBonemealAgeIncrease(Level level) { return 1; }

    @Override
    protected int getRegrowAge() { return 2; }

    @Override
    protected int getHarvestAge() { return 3; }

    @Override
    protected List<ItemStack> getHarvestDrops(BlockState state, RandomSource random) {
        List<ItemStack> drops = new ArrayList<>();
        boolean full = state.getValue(AGE) == 4;
        drops.add(new ItemStack(ModItems.COCA_LEAVES.get(), full ? 3 + random.nextInt(3) : 1 + random.nextInt(2)));
        if (full && random.nextFloat() < 0.35f) drops.add(new ItemStack(ModItems.COCA_SEEDS.get()));
        return drops;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int age = state.getValue(AGE);
        return switch (age) {
            case 0 -> Block.box(5, 0, 5, 11, 6, 11);
            case 1 -> Block.box(3, 0, 3, 13, 10, 13);
            case 2 -> Block.box(2, 0, 2, 14, 13, 14);
            default -> Block.box(1, 0, 1, 15, 16, 15);
        };
    }
}
