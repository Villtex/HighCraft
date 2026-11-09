package de.highcraft.block;

import de.highcraft.config.HighCraftConfig;
import de.highcraft.drug.Quality;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.CommonHooks;

import java.util.List;
import java.util.function.Supplier;

/**
 * Base of all HighCraft crops.
 * <ul>
 *     <li>Property {@code fertilized}: treated with fertilizer -> faster growth, better quality</li>
 *     <li>Optional re-harvesting with right-click ({@link #getRegrowAge()})</li>
 *     <li>The harvest gets its quality from {@link CropQuality}</li>
 * </ul>
 */
public abstract class HighCropBlock extends CropBlock {
    public static final BooleanProperty FERTILIZED = BooleanProperty.create("fertilized");

    private final Supplier<? extends ItemLike> seed;

    protected HighCropBlock(Properties properties, Supplier<? extends ItemLike> seed) {
        super(properties);
        this.seed = seed;
        this.registerDefaultState(this.defaultBlockState().setValue(FERTILIZED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(getAgeProperty(), FERTILIZED);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return seed.get();
    }

    /** Growth factor at this position (biome, light, ...). */
    protected float environmentFactor(BlockState state, ServerLevel level, BlockPos pos) {
        return 1.0f;
    }

    /** Whether this state grows by itself (e.g. only the lower half of tall plants). */
    protected boolean isGrowingPart(BlockState state) {
        return true;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return isGrowingPart(state) && !isMaxAge(state);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!isGrowingPart(state) || !level.isAreaLoaded(pos, 1)) return;
        if (level.getRawBrightness(pos, 0) < 9) return;
        int age = getAge(state);
        if (age >= getMaxAge()) return;
        float speed = getGrowthSpeed(state, level, pos) * environmentFactor(state, level, pos);
        if (state.getValue(FERTILIZED)) speed *= 1.5f;
        speed *= HighCraftConfig.CROP_GROWTH_MULTIPLIER.get().floatValue();
        speed = Math.max(0.05f, speed);
        if (CommonHooks.canCropGrow(level, pos, state, random.nextInt((int) (25.0F / speed) + 1) == 0)) {
            setAge(level, pos, state, age + 1);
            CommonHooks.fireCropGrowPost(level, pos, level.getBlockState(pos));
        }
    }

    /** Sets the age while keeping the other properties. */
    public void setAge(Level level, BlockPos pos, BlockState state, int age) {
        level.setBlock(pos, state.setValue(getAgeProperty(), Math.min(age, getMaxAge())), 2);
    }

    @Override
    public void growCrops(Level level, BlockPos pos, BlockState state) {
        int age = Math.min(getMaxAge(), getAge(state) + getBonemealAgeIncrease(level));
        setAge(level, pos, state, age);
    }

    /** Apply fertilizer: marks the plant and lets it grow one step. */
    public boolean fertilize(Level level, BlockPos pos, BlockState state) {
        boolean changed = false;
        if (!state.getValue(FERTILIZED)) {
            state = state.setValue(FERTILIZED, true);
            level.setBlock(pos, state, 2);
            changed = true;
        }
        if (!isMaxAge(state)) {
            setAge(level, pos, state, getAge(state) + 1);
            changed = true;
        }
        return changed;
    }

    // ---------------- Re-harvesting ----------------

    /** Age the plant falls back to after right-click harvesting; -1 = no right-click harvesting. */
    protected int getRegrowAge() {
        return -1;
    }

    /** From which age the plant can be harvested with right-click. */
    protected int getHarvestAge() {
        return getMaxAge();
    }

    /** Creates the right-click harvest (without quality - it is set afterwards). */
    protected List<ItemStack> getHarvestDrops(BlockState state, RandomSource random) {
        return List.of();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (getRegrowAge() < 0) return InteractionResult.PASS;
        BlockPos base = getBasePos(state, pos);
        BlockState baseState = level.getBlockState(base);
        if (!baseState.is(this) || getAge(baseState) < getHarvestAge()) return InteractionResult.PASS;
        if (level instanceof ServerLevel server) {
            int quality = CropQuality.compute(server, base, baseState);
            for (ItemStack drop : getHarvestDrops(baseState, level.random)) {
                Block.popResource(level, pos, Quality.set(drop, quality));
            }
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
            setAge(level, base, baseState.setValue(FERTILIZED, false), getRegrowAge());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Position of the "main" block (lower half for tall plants). */
    public BlockPos getBasePos(BlockState state, BlockPos pos) {
        return pos;
    }

    // ---------------- Shape ----------------

    /**
     * One shape per age, built on first use (getShape runs for outlines, raycasts and culling - no allocation per call).
     * Volatile: chunk meshing (e.g. Sodium) queries shapes off-thread, the array must be published fully built.
     */
    private volatile VoxelShape[] shapeByAge;

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape[] shapes = shapeByAge;
        if (shapes == null) {
            int max = getMaxAge();
            shapes = new VoxelShape[max + 1];
            for (int age = 0; age <= max; age++) {
                float progress = (age + 1) / (float) (max + 1);
                shapes[age] = Block.box(1, 0, 1, 15, Math.max(2, Math.min(16, Math.round(16 * progress))), 15);
            }
            shapeByAge = shapes;
        }
        return shapes[Math.max(0, Math.min(shapes.length - 1, getAge(state)))];
    }

    public IntegerProperty ageProperty() {
        return getAgeProperty();
    }
}
