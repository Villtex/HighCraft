package de.highcraft.block;

import de.highcraft.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Computes the harvest quality (1..5) from the growing conditions:
 * <ul>
 *     <li>+1 fertilized</li>
 *     <li>+1 plenty of light (sky light >= 14 or a grow lamp nearby)</li>
 *     <li>+1 well-watered farmland or a suitable climate</li>
 *     <li>+1 with a 30 % chance ("good genetics")</li>
 * </ul>
 */
public final class CropQuality {
    private CropQuality() {}

    public static int compute(ServerLevel level, BlockPos pos, BlockState state) {
        int q = 1;
        if (state.hasProperty(HighCropBlock.FERTILIZED) && state.getValue(HighCropBlock.FERTILIZED)) q++;
        if (level.getBrightness(LightLayer.SKY, pos) >= 14 && level.isDay() || hasGrowLampNearby(level, pos)) q++;
        BlockState below = level.getBlockState(pos.below());
        if (below.hasProperty(FarmBlock.MOISTURE) && below.getValue(FarmBlock.MOISTURE) >= FarmBlock.MAX_MOISTURE) {
            q++;
        } else if (state.is(ModBlocks.COCA_BUSH.get()) && level.getBiome(pos).value().getBaseTemperature() >= 0.9f) {
            q++;
        } else if (state.is(ModBlocks.MAGIC_MUSHROOM.get()) && level.getBrightness(LightLayer.BLOCK, pos) <= 4) {
            q++;
        }
        if (level.random.nextFloat() < 0.3f) q++;
        return Math.min(5, q);
    }

    public static boolean hasGrowLampNearby(ServerLevel level, BlockPos pos) {
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-2, 0, -2), pos.offset(2, 5, 2))) {
            if (level.getBlockState(p).is(ModBlocks.GROW_LAMP.get())) return true;
        }
        return false;
    }
}
