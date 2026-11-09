package de.highcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Non-rotating lab prop with a custom hitbox (barrels, gas cylinder, ceiling light). */
public class ShapedDecorBlock extends Block {
    private final MapCodec<ShapedDecorBlock> codec;
    private final VoxelShape shape;

    public ShapedDecorBlock(Properties properties, double[]... boxes) {
        super(properties);
        this.codec = simpleCodec(p -> new ShapedDecorBlock(p, boxes));
        VoxelShape s = Shapes.empty();
        for (double[] b : boxes) s = Shapes.or(s, Block.box(b[0], b[1], b[2], b[3], b[4], b[5]));
        this.shape = boxes.length == 0 ? Shapes.block() : s.optimize();
    }

    @Override
    protected MapCodec<? extends Block> codec() { return codec; }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return shape;
    }
}
