package de.highcraft.block;

import com.mojang.serialization.MapCodec;
import de.highcraft.drug.Quality;
import de.highcraft.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

/**
 * Magic mushrooms - grow in the dark (or on mycelium/podzol) and slowly spread.
 * Stage 2 = mature cluster, harvestable with right-click.
 */
public class MagicMushroomBlock extends BushBlock implements BonemealableBlock {
    public static final MapCodec<MagicMushroomBlock> CODEC = simpleCodec(MagicMushroomBlock::new);
    public static final IntegerProperty AGE = BlockStateProperties.AGE_2;
    private static final VoxelShape[] SHAPES = {
            Block.box(5, 0, 5, 11, 5, 11),
            Block.box(4, 0, 4, 12, 8, 12),
            Block.box(2, 0, 2, 14, 10, 14)
    };

    public MagicMushroomBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPES[state.getValue(AGE)];
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.isSolidRender(level, pos) || state.is(BlockTags.MUSHROOM_GROW_BLOCK);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState soil = level.getBlockState(below);
        if (soil.is(BlockTags.MUSHROOM_GROW_BLOCK)) return true;
        return level.getRawBrightness(pos, 0) < 13 && mayPlaceOn(soil, level, below);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) { return true; }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        boolean goodSoil = level.getBlockState(pos.below()).is(BlockTags.MUSHROOM_GROW_BLOCK);
        if (random.nextInt(goodSoil ? 6 : 12) != 0) return;
        if (age < 2) {
            level.setBlock(pos, state.setValue(AGE, age + 1), 2);
            return;
        }
        // Mature: spread slowly (max. 4 mushrooms nearby)
        int count = 0;
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-3, -1, -3), pos.offset(3, 1, 3))) {
            if (level.getBlockState(p).is(this) && ++count > 4) return;
        }
        BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
        BlockState young = defaultBlockState();
        if (level.isEmptyBlock(target) && young.canSurvive(level, target)) {
            level.setBlock(target, young, 2);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(AGE) < 2) return InteractionResult.PASS;
        if (level instanceof ServerLevel server) {
            int quality = CropQuality.compute(server, pos, state);
            Block.popResource(level, pos, Quality.set(new ItemStack(ModItems.MAGIC_MUSHROOM.get(), 2 + level.random.nextInt(2)), quality));
            if (level.random.nextFloat() < 0.3f) Block.popResource(level, pos, new ItemStack(ModItems.MUSHROOM_SPORES.get()));
            level.playSound(null, pos, SoundEvents.FUNGUS_BREAK, SoundSource.BLOCKS, 1.0f, 1.2f);
            level.setBlock(pos, state.setValue(AGE, 0), 2);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Faintly glowing spores
        if (state.getValue(AGE) == 2 && random.nextInt(4) == 0) {
            level.addParticle(new DustParticleOptions(new Vector3f(0.55f, 0.75f, 1.0f), 0.6f),
                    pos.getX() + 0.2 + random.nextDouble() * 0.6, pos.getY() + 0.4 + random.nextDouble() * 0.3,
                    pos.getZ() + 0.2 + random.nextDouble() * 0.6, 0, 0.01, 0);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.getValue(AGE) < 2;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return random.nextFloat() < 0.5f;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(AGE, Math.min(2, state.getValue(AGE) + 1)), 2);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(ModItems.MUSHROOM_SPORES.get());
    }
}
