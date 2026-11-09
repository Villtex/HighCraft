package de.highcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

/**
 * Rotatable lab furniture. The shape is given for facing = north (front towards north) and rotated
 * for the other directions. Wall-mounted blocks take the facing of the clicked wall face.
 */
public class LabDecorBlock extends HorizontalDirectionalBlock {
    private final MapCodec<LabDecorBlock> codec;
    private final Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
    private final boolean wallMounted;

    public LabDecorBlock(Properties properties, boolean wallMounted, double[]... northBoxes) {
        super(properties);
        this.wallMounted = wallMounted;
        this.codec = simpleCodec(p -> new LabDecorBlock(p, wallMounted, northBoxes));
        for (Direction d : Direction.Plane.HORIZONTAL) shapes.put(d, rotated(d, northBoxes));
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    /** Rotates boxes (x1,y1,z1,x2,y2,z2 in pixels) like a blockstate "y" rotation. */
    static VoxelShape rotated(Direction facing, double[]... boxes) {
        if (boxes.length == 0) return Shapes.block();
        VoxelShape out = Shapes.empty();
        int turns = switch (facing) { case EAST -> 1; case SOUTH -> 2; case WEST -> 3; default -> 0; };
        for (double[] b : boxes) {
            double x1 = b[0], z1 = b[2], x2 = b[3], z2 = b[5];
            for (int i = 0; i < turns; i++) {
                double nx1 = 16 - z2, nx2 = 16 - z1, nz1 = x1, nz2 = x2;
                x1 = nx1; x2 = nx2; z1 = nz1; z2 = nz2;
            }
            out = Shapes.or(out, Block.box(x1, b[1], z1, x2, b[4], z2));
        }
        return out.optimize();
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() { return codec; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction face = ctx.getClickedFace();
        if (wallMounted && face.getAxis().isHorizontal()) return defaultBlockState().setValue(FACING, face);
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return shapes.get(state.getValue(FACING));
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
