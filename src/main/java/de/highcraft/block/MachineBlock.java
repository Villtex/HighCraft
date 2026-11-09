package de.highcraft.block;

import com.mojang.serialization.MapCodec;
import de.highcraft.block.entity.AbstractMachineBlockEntity;
import de.highcraft.block.entity.GrowTentBlockEntity;
import de.highcraft.block.entity.ProcessingMachineBlockEntity;
import de.highcraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

/** Block of all GUI machines (grinder, extractor, chemistry lab, press, grow tent). */
public class MachineBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    private final MachineType machine;
    private final MapCodec<MachineBlock> codec;
    private final VoxelShape shape;

    public MachineBlock(Properties properties, MachineType machine) {
        super(properties);
        this.machine = machine;
        this.codec = simpleCodec(p -> new MachineBlock(p, machine));
        this.shape = switch (machine) {
            case LAB -> Shapes.or(Block.box(0, 0, 0, 16, 12, 16), Block.box(1, 12, 1, 15, 16, 15));
            case EXTRACTOR -> Shapes.or(Block.box(1, 0, 1, 15, 4, 15), Block.box(2, 4, 2, 14, 16, 14));
            case PRESS -> Shapes.or(Block.box(0, 0, 0, 16, 5, 16), Block.box(1, 5, 1, 15, 16, 15));
            case REACTOR -> Shapes.or(Block.box(0, 0, 0, 16, 6, 16), Block.box(2, 6, 2, 14, 15, 14));
            case CRYSTALLIZER -> Shapes.or(Block.box(0, 0, 0, 16, 9, 16), Block.box(1, 9, 1, 15, 12, 15));
            case PILL_PRESS -> Shapes.or(Block.box(1, 0, 1, 15, 6, 15), Block.box(3, 6, 3, 13, 16, 13));
            default -> Shapes.block();
        };
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    public MachineType machine() { return machine; }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return codec; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return shape;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch (machine) {
            case GRINDER -> new ProcessingMachineBlockEntity(ModBlockEntities.GRINDER.get(), pos, state, machine);
            case EXTRACTOR -> new ProcessingMachineBlockEntity(ModBlockEntities.EXTRACTOR.get(), pos, state, machine);
            case LAB -> new ProcessingMachineBlockEntity(ModBlockEntities.LAB_STATION.get(), pos, state, machine);
            case PRESS -> new ProcessingMachineBlockEntity(ModBlockEntities.PRESS.get(), pos, state, machine);
            case REACTOR -> new ProcessingMachineBlockEntity(ModBlockEntities.CHEMICAL_REACTOR.get(), pos, state, machine);
            case CRYSTALLIZER -> new ProcessingMachineBlockEntity(ModBlockEntities.CRYSTALLIZER.get(), pos, state, machine);
            case PILL_PRESS -> new ProcessingMachineBlockEntity(ModBlockEntities.PILL_PRESS.get(), pos, state, machine);
            case GROW_TENT -> new GrowTentBlockEntity(pos, state);
        };
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof AbstractMachineBlockEntity machineBe) machineBe.serverTick();
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer sp
                && level.getBlockEntity(pos) instanceof AbstractMachineBlockEntity be) {
            sp.openMenu(be, buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof AbstractMachineBlockEntity be) {
            be.dropContents(level, pos);
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) { return true; }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof AbstractMachineBlockEntity be) {
            return ItemHandlerHelper.calcRedstoneFromInventory(be.inventory());
        }
        return 0;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;
        double x = pos.getX() + 0.5, y = pos.getY(), z = pos.getZ() + 0.5;
        switch (machine) {
            case GRINDER -> {
                if (random.nextInt(3) == 0) level.addParticle(ParticleTypes.WHITE_ASH, x + random.nextGaussian() * 0.2, y + 1.05, z + random.nextGaussian() * 0.2, 0, 0.02, 0);
                if (random.nextInt(20) == 0) level.playLocalSound(x, y, z, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.25f, 0.8f + random.nextFloat() * 0.3f, false);
            }
            case EXTRACTOR -> {
                level.addParticle(ParticleTypes.BUBBLE_POP, x + random.nextGaussian() * 0.12, y + 0.9, z + random.nextGaussian() * 0.12, 0, 0.03, 0);
                if (random.nextInt(25) == 0) level.playLocalSound(x, y, z, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.3f, 1.2f, false);
            }
            case LAB -> {
                level.addParticle(ParticleTypes.SMALL_FLAME, x + random.nextGaussian() * 0.05, y + 0.85, z + random.nextGaussian() * 0.05, 0, 0.005, 0);
                if (random.nextInt(4) == 0) level.addParticle(ParticleTypes.WITCH, x + random.nextGaussian() * 0.2, y + 1.1, z + random.nextGaussian() * 0.2, 0, 0.05, 0);
                if (random.nextInt(30) == 0) level.playLocalSound(x, y, z, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.4f, 0.7f, false);
            }
            case PRESS -> {
                if (random.nextInt(4) == 0) level.addParticle(ParticleTypes.SMOKE, x + random.nextGaussian() * 0.15, y + 1.0, z + random.nextGaussian() * 0.15, 0, 0.02, 0);
                if (random.nextInt(30) == 0) level.playLocalSound(x, y, z, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.2f, 0.6f, false);
            }
            case REACTOR -> {
                level.addParticle(ParticleTypes.BUBBLE_POP, x + random.nextGaussian() * 0.15, y + 0.95, z + random.nextGaussian() * 0.15, 0, 0.04, 0);
                if (random.nextInt(3) == 0) level.addParticle(ParticleTypes.SMALL_FLAME, x + random.nextGaussian() * 0.2, y + 0.3, z + random.nextGaussian() * 0.2, 0, 0.005, 0);
                if (random.nextInt(5) == 0) level.addParticle(ParticleTypes.WHITE_SMOKE, x, y + 1.1, z, 0, 0.04, 0);
                if (random.nextInt(25) == 0) level.playLocalSound(x, y, z, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.4f, 0.6f, false);
            }
            case CRYSTALLIZER -> {
                if (random.nextInt(3) == 0) level.addParticle(ParticleTypes.SNOWFLAKE, x + random.nextGaussian() * 0.25, y + 0.8, z + random.nextGaussian() * 0.25, 0, 0.01, 0);
                if (random.nextInt(40) == 0) level.playLocalSound(x, y, z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.5f, 1.5f, false);
            }
            case PILL_PRESS -> {
                if (random.nextInt(12) == 0) level.playLocalSound(x, y, z, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.15f, 1.6f, false);
                if (random.nextInt(4) == 0) level.addParticle(ParticleTypes.WHITE_ASH, x + random.nextGaussian() * 0.2, y + 0.6, z + random.nextGaussian() * 0.2, 0, 0.02, 0);
            }
            case GROW_TENT -> {
                if (random.nextInt(6) == 0) level.addParticle(ParticleTypes.HAPPY_VILLAGER, x + random.nextGaussian() * 0.25, y + 0.6, z + random.nextGaussian() * 0.25, 0, 0.02, 0);
            }
        }
    }
}
