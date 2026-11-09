package de.highcraft.block;

import de.highcraft.registry.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** Tobacco - big leaves, can be re-harvested with right-click. */
public class TobaccoCropBlock extends HighCropBlock {
    public TobaccoCropBlock(Properties properties) {
        super(properties, ModItems.TOBACCO_SEEDS);
    }

    @Override
    protected int getRegrowAge() { return 5; }

    @Override
    protected List<ItemStack> getHarvestDrops(BlockState state, RandomSource random) {
        return List.of(new ItemStack(ModItems.TOBACCO_LEAF.get(), 2 + random.nextInt(2)));
    }
}
