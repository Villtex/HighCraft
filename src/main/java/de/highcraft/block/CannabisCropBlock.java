package de.highcraft.block;

import de.highcraft.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

/**
 * Cannabis plant - grows two blocks tall from stage 4. Mature plants (stage 7) can be harvested
 * with right-click and regrow from stage 5.
 */
public class CannabisCropBlock extends HighCropBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final int DOUBLE_AGE = 4;

    public CannabisCropBlock(Properties properties) {
        super(properties, ModItems.CANNABIS_SEEDS);
        this.registerDefaultState(this.defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HALF);
    }

    public static boolean isUpper(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER;
    }

    @Override
    protected boolean isGrowingPart(BlockState state) {
        return !isUpper(state);
    }

    @Override
    public BlockPos getBasePos(BlockState state, BlockPos pos) {
        return isUpper(state) ? pos.below() : pos;
    }

    @Override
    public void setAge(Level level, BlockPos pos, BlockState state, int age) {
        if (isUpper(state)) {
            pos = pos.below();
            state = level.getBlockState(pos);
            if (!state.is(this)) return;
        }
        age = Math.min(age, getMaxAge());
        BlockPos above = pos.above();
        if (age >= DOUBLE_AGE) {
            BlockState aboveState = level.getBlockState(above);
            boolean hasUpper = aboveState.is(this) && isUpper(aboveState);
            if (!hasUpper && !aboveState.canBeReplaced()) {
                age = Math.min(age, DOUBLE_AGE - 1); // no room above
            }
        }
        BlockState lower = state.setValue(AGE, age);
        level.setBlock(pos, lower, 2);
        if (age >= DOUBLE_AGE) {
            BlockState upper = lower.setValue(HALF, DoubleBlockHalf.UPPER);
            level.setBlock(above, upper, 3);
        }
    }

    @Override
    public boolean fertilize(Level level, BlockPos pos, BlockState state) {
        BlockPos base = getBasePos(state, pos);
        BlockState baseState = level.getBlockState(base);
        if (!baseState.is(this)) return false;
        boolean changed = super.fertilize(level, base, baseState);
        // Keep the upper half in sync
        BlockState now = level.getBlockState(base);
        BlockState up = level.getBlockState(base.above());
        if (up.is(this) && isUpper(up) && up.getValue(FERTILIZED) != now.getValue(FERTILIZED)) {
            level.setBlock(base.above(), up.setValue(FERTILIZED, now.getValue(FERTILIZED)), 2);
        }
        return changed;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (isUpper(state)) {
            BlockState below = level.getBlockState(pos.below());
            return below.is(this) && !isUpper(below) && below.getValue(AGE) >= DOUBLE_AGE;
        }
        return super.canSurvive(state, level, pos);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!isUpper(state) && dir == Direction.UP && state.getValue(AGE) >= DOUBLE_AGE
                && !(neighbor.is(this) && isUpper(neighbor))) {
            // Top was removed -> plant shrinks back to single height
            return state.setValue(AGE, DOUBLE_AGE - 1);
        }
        return super.updateShape(state, dir, neighbor, level, pos, neighborPos);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && isUpper(state)) {
            BlockPos below = pos.below();
            BlockState lower = level.getBlockState(below);
            if (lower.is(this) && !isUpper(lower)) {
                // Breaking the top harvests the whole plant (loot of the lower half)
                level.destroyBlock(below, !player.isCreative(), player);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    /**
     * Automatic harvesters (e.g. Create's mechanical harvester) set {@code getStateForAge(0)} on mature plants -
     * which turns the upper half's position into a young lower half. This is caught here and treated like a
     * right-click harvest: drop the harvest, reset the plant to stage 5 (it regrows).
     */
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        super.onPlace(state, level, pos, oldState, moved);
        if (level.isClientSide || isUpper(state) || !oldState.is(this) || !isUpper(oldState)) return;
        BlockPos below = pos.below();
        BlockState base = level.getBlockState(below);
        if (!base.is(this) || isUpper(base)) return;
        if (level instanceof net.minecraft.server.level.ServerLevel server && oldState.getValue(AGE) >= getMaxAge()) {
            int quality = CropQuality.compute(server, below, base);
            for (ItemStack drop : getHarvestDrops(base, level.random)) {
                Block.popResource(level, pos, de.highcraft.drug.Quality.set(drop, quality));
            }
        }
        setAge(level, below, base.setValue(FERTILIZED, false), getRegrowAge());
    }

    // ---- Bone meal always goes to the lower half ----
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        BlockState base = level.getBlockState(getBasePos(state, pos));
        return base.is(this) && !isMaxAge(base);
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        BlockPos base = getBasePos(state, pos);
        growCrops(level, base, level.getBlockState(base));
    }

    @Override
    public void growCrops(Level level, BlockPos pos, BlockState state) {
        BlockPos base = getBasePos(state, pos);
        BlockState baseState = level.getBlockState(base);
        if (!baseState.is(this)) return;
        super.growCrops(level, base, baseState);
    }

    // ---- Re-harvesting ----
    @Override
    protected int getRegrowAge() { return 5; }

    @Override
    protected List<ItemStack> getHarvestDrops(BlockState state, RandomSource random) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(ModItems.CANNABIS_BUD.get(), 1 + random.nextInt(3)));
        drops.add(new ItemStack(ModItems.CANNABIS_LEAF.get(), 1 + random.nextInt(2)));
        if (random.nextFloat() < 0.25f) drops.add(new ItemStack(ModItems.CANNABIS_SEEDS.get()));
        return drops;
    }

    /*
     * Shapes are precomputed and clamped to 1..16: mods like "More Culling" query the shape of EVERY
     * block state while loading - including combinations that never occur in game (e.g. upper half at
     * age 0). An invalid box (min > max) would abort the resource reload there and leave the
     * client stuck on the loading screen.
     */
    private static final VoxelShape[] LOWER_SHAPES = new VoxelShape[8];
    private static final VoxelShape[] UPPER_SHAPES = new VoxelShape[8];
    static {
        for (int age = 0; age < 8; age++) {
            LOWER_SHAPES[age] = Block.box(2, 0, 2, 14, clampHeight(3 + age * 3), 14);
            UPPER_SHAPES[age] = Block.box(2, 0, 2, 14, clampHeight(4 + (age - DOUBLE_AGE) * 3), 14);
        }
    }

    private static int clampHeight(int h) {
        return Math.max(1, Math.min(16, h));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int age = Math.max(0, Math.min(7, state.getValue(AGE)));
        return isUpper(state) ? UPPER_SHAPES[age] : LOWER_SHAPES[age];
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(ModItems.CANNABIS_SEEDS.get());
    }

    /** Helper for world generation/tests. */
    public static void placeMature(LevelAccessor level, BlockPos pos, Block block) {
        BlockState lower = block.defaultBlockState().setValue(AGE, 7);
        level.setBlock(pos, lower, 2);
        if (level.getBlockState(pos.above()).is(Blocks.AIR)) {
            level.setBlock(pos.above(), lower.setValue(HALF, DoubleBlockHalf.UPPER), 2);
        }
    }
}
