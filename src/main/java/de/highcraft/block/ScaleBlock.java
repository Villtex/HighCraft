package de.highcraft.block;

import com.mojang.serialization.MapCodec;
import de.highcraft.drug.Quality;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Locale;

/** Precision scale - the dealer's workstation. Right-click with goods to show weight and quality. */
public class ScaleBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<ScaleBlock> CODEC = simpleCodec(ScaleBlock::new);
    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 3, 13);

    public ScaleBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) { return SHAPE; }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            // Pseudo weight: deterministic per item so the scale feels "real"
            float perItem = 0.8f + (Math.abs(stack.getItem().getDescriptionId().hashCode()) % 30) / 10f;
            float grams = perItem * stack.getCount();
            Component msg = Component.translatable("message.highcraft.scale",
                    Component.literal(String.format(Locale.GERMANY, "%.1f", grams)).withStyle(ChatFormatting.GREEN),
                    stack.getHoverName());
            if (Quality.has(stack)) msg = msg.copy().append("  ").append(Quality.stars(Quality.get(stack)));
            player.displayClientMessage(msg, true);
            level.playSound(null, pos, SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.BLOCKS, 0.4f, 2.0f);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
}
