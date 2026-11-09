package de.highcraft.block;

import com.mojang.serialization.MapCodec;
import de.highcraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;
import org.jetbrains.annotations.Nullable;

/**
 * Grow lamp (full-spectrum LED). Emits light level 15 and speeds up crops in a
 * 5x5 area up to 5 blocks below. Also counts as "plenty of light" for harvest quality.
 */
public class GrowLampBlock extends BaseEntityBlock {
    public static final MapCodec<GrowLampBlock> CODEC = simpleCodec(GrowLampBlock::new);
    private static final VoxelShape SHAPE = Shapes.or(Block.box(1, 12, 1, 15, 14, 15), Block.box(7, 14, 7, 9, 16, 9));

    public GrowLampBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) { return SHAPE; }

    @Override
    protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GrowLampBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.GROW_LAMP.get(), (l, p, s, be) -> be.serverTick((ServerLevel) l));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) == 0) {
            level.addParticle(new DustParticleOptions(new Vector3f(0.85f, 0.35f, 1.0f), 0.5f),
                    pos.getX() + 0.15 + random.nextDouble() * 0.7, pos.getY() + 0.7, pos.getZ() + 0.15 + random.nextDouble() * 0.7, 0, -0.02, 0);
        }
    }

    public static class GrowLampBlockEntity extends BlockEntity {
        public GrowLampBlockEntity(BlockPos pos, BlockState state) {
            super(ModBlockEntities.GROW_LAMP.get(), pos, state);
        }

        public void serverTick(ServerLevel level) {
            if (level.getGameTime() % 20 != (worldPosition.asLong() & 15)) return;
            RandomSource random = level.random;
            for (int i = 0; i < 4; i++) {
                BlockPos p = worldPosition.offset(random.nextInt(5) - 2, -1 - random.nextInt(5), random.nextInt(5) - 2);
                BlockState s = level.getBlockState(p);
                if (s.getBlock() instanceof BonemealableBlock && s.isRandomlyTicking() && random.nextFloat() < 0.5f) {
                    s.randomTick(level, p, random);
                }
            }
        }
    }
}
