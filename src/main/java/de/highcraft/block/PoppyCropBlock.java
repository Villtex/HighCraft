package de.highcraft.block;

import de.highcraft.drug.Quality;
import de.highcraft.item.ScoringKnifeItem;
import de.highcraft.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Opium poppy - ripe pods (stage 7) can be scored with the scoring knife.
 * That yields raw opium; the plant falls back to stage 6 and the pod ripens again.
 */
public class PoppyCropBlock extends HighCropBlock {
    public PoppyCropBlock(Properties properties) {
        super(properties, ModItems.POPPY_SEEDS);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof ScoringKnifeItem) {
            if (!isMaxAge(state)) return ItemInteractionResult.FAIL;
            if (level instanceof ServerLevel server) {
                int quality = CropQuality.compute(server, pos, state);
                Block.popResource(level, pos, Quality.set(new ItemStack(ModItems.RAW_OPIUM.get(), 1 + level.random.nextInt(2)), quality));
                level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 0.7F, 1.4F);
                server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.WHITE_CONCRETE_POWDER.defaultBlockState()),
                        pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 6, 0.15, 0.1, 0.15, 0.02);
                setAge(level, pos, state, getMaxAge() - 1);
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }
}
