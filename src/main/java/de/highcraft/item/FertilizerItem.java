package de.highcraft.item;

import de.highcraft.block.HighCropBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** Fertilizer - marks HighCraft crops as fertilized (+growth, +quality), otherwise works like bone meal. */
public class FertilizerItem extends Item {
    public FertilizerItem(Properties properties) { super(properties); }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof HighCropBlock crop) {
            if (!level.isClientSide) {
                if (crop.fertilize(level, pos, state)) {
                    ctx.getItemInHand().shrink(1);
                    level.levelEvent(1505, pos, 15); // growth particles
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (BoneMealItem.applyBonemeal(ctx.getItemInHand(), level, pos, ctx.getPlayer())) {
            if (!level.isClientSide) level.levelEvent(1505, pos, 15);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.highcraft.fertilizer").withStyle(ChatFormatting.GRAY));
    }
}
