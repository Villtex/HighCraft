package de.highcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Grow tent - two blocks tall. The lower half holds the block entity (inventory, energy, the plant);
 * the upper half carries the roof and the lamp and forwards clicks, comparators and capabilities
 * (see ModCapabilities) to the lower half.
 * <ul>
 *     <li>Placing needs a free block above; breaking either half removes the whole tent.</li>
 *     <li>The upper half mirrors facing and lamp state ({@link #LIT}) of the lower half.</li>
 *     <li>A lower half without a roof is tolerated (tents from before 1.2.1 were one block tall - they get their
 *     roof back on the next tick if there is room, see {@link #restoreUpperHalf}).</li>
 * </ul>
 */
public class GrowTentBlock extends MachineBlock {
    public static final MapCodec<GrowTentBlock> CODEC = simpleCodec(GrowTentBlock::new);
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public GrowTentBlock(Properties properties) {
        super(properties, MachineType.GROW_TENT);
        registerDefaultState(defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HALF);
    }

    public static boolean isUpper(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER;
    }

    /** Position of the lower half (where the block entity lives). */
    public static BlockPos basePos(BlockState state, BlockPos pos) {
        return isUpper(state) ? pos.below() : pos;
    }

    public static boolean hasUpperHalf(LevelReader level, BlockPos lowerPos) {
        BlockState above = level.getBlockState(lowerPos.above());
        return above.getBlock() instanceof GrowTentBlock && isUpper(above);
    }

    private static BlockState upperFor(BlockState lower) {
        return lower.setValue(HALF, DoubleBlockHalf.UPPER);
    }

    /** Adds a missing upper half (tents placed before 1.2.1 were one block tall) if there is room above. */
    public static void restoreUpperHalf(Level level, BlockPos lowerPos, BlockState lower) {
        if (!(lower.getBlock() instanceof GrowTentBlock) || isUpper(lower) || hasUpperHalf(level, lowerPos)) return;
        BlockPos above = lowerPos.above();
        if (level.isOutsideBuildHeight(above) || !level.getBlockState(above).canBeReplaced()) return;
        level.setBlock(above, upperFor(lower), Block.UPDATE_ALL);
    }

    // ---------------- Placement & removal ----------------

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos above = ctx.getClickedPos().above();
        Level level = ctx.getLevel();
        if (level.isOutsideBuildHeight(above) || !level.getBlockState(above).canBeReplaced(ctx)) return null;
        return super.getStateForPlacement(ctx).setValue(HALF, DoubleBlockHalf.LOWER);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        level.setBlock(pos.above(), upperFor(state), Block.UPDATE_ALL);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (dir == Direction.DOWN && isUpper(state)) {
            // The roof only exists on top of a lower half and follows its facing and lamp
            if (!neighbor.is(this) || isUpper(neighbor)) return Blocks.AIR.defaultBlockState();
            return state.setValue(FACING, neighbor.getValue(FACING)).setValue(LIT, neighbor.getValue(LIT));
        }
        return super.updateShape(state, dir, neighbor, level, pos, neighborPos);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && isUpper(state)) {
            BlockPos below = pos.below();
            BlockState lower = level.getBlockState(below);
            if (lower.is(this) && !isUpper(lower)) {
                // Breaking the roof takes down the whole tent - the item and the contents come from the lower half
                if (player.isCreative()) {
                    level.setBlock(below, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                    level.levelEvent(player, 2001, below, Block.getId(lower));
                } else {
                    level.destroyBlock(below, true, player);
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        super.onPlace(state, level, pos, oldState, moved);
        // The upper half provides capabilities without a block entity -> caches must be told (NeoForge contract)
        if (isUpper(state)) level.invalidateCapabilities(pos);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        super.onRemove(state, level, pos, newState, moved);
        if (isUpper(state)) level.invalidateCapabilities(pos);
    }

    // ---------------- Upper half forwards to the lower half ----------------

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return isUpper(state) ? null : super.newBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return super.useWithoutItem(state, level, basePos(state, pos), player, hit);
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return super.getAnalogOutputSignal(state, level, basePos(state, pos));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!isUpper(state)) super.animateTick(state, level, pos, random);
    }
}
