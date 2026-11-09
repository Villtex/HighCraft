package de.highcraft.block.entity;

import de.highcraft.drug.Quality;
import de.highcraft.recipe.MachineRecipe;
import de.highcraft.recipe.MachineRecipeInput;
import de.highcraft.registry.ModBlockEntities;
import de.highcraft.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class DryingRackBlockEntity extends BlockEntity {
    public static final int SLOTS = 4;
    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
    private final int[] progress = new int[SLOTS];
    private final int[] total = new int[SLOTS];

    private final IItemHandler automation = new IItemHandler() {
        @Override public int getSlots() { return SLOTS; }
        @Override public ItemStack getStackInSlot(int slot) { return items.get(slot); }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!items.get(slot).isEmpty() || !canDry(stack)) return stack;
            if (!simulate) {
                items.set(slot, stack.copyWithCount(1));
                progress[slot] = 0;
                total[slot] = 0;
                changed();
            }
            ItemStack rest = stack.copy();
            rest.shrink(1);
            return rest;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack s = items.get(slot);
            if (s.isEmpty() || amount <= 0 || canDry(s)) return ItemStack.EMPTY;
            if (!simulate) {
                items.set(slot, ItemStack.EMPTY);
                changed();
            }
            return s.copy();
        }

        @Override public int getSlotLimit(int slot) { return 1; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return canDry(stack); }
    };

    public DryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRYING_RACK.get(), pos, state);
    }

    public IItemHandler automationHandler() { return automation; }
    public NonNullList<ItemStack> items() { return items; }

    public float progress(int slot) {
        return total[slot] <= 0 ? 0 : progress[slot] / (float) total[slot];
    }

    private Optional<RecipeHolder<MachineRecipe>> recipeFor(ItemStack stack) {
        if (level == null || stack.isEmpty()) return Optional.empty();
        return level.getRecipeManager().getRecipeFor(ModRecipes.DRYING.get(), new MachineRecipeInput(List.of(stack)), level);
    }

    public boolean canDry(ItemStack stack) {
        return recipeFor(stack).isPresent();
    }

    public boolean addItem(ItemStack stack) {
        for (int i = 0; i < SLOTS; i++) {
            if (items.get(i).isEmpty()) {
                items.set(i, stack.split(1));
                progress[i] = 0;
                total[i] = 0;
                if (level != null) level.playSound(null, worldPosition, SoundEvents.AZALEA_LEAVES_PLACE, SoundSource.BLOCKS, 0.8f, 1.1f);
                changed();
                return true;
            }
        }
        return false;
    }

    /** Prefers finished dried goods, otherwise takes the most recently hung part. */
    public ItemStack takeItem() {
        for (int i = 0; i < SLOTS; i++) {
            if (!items.get(i).isEmpty() && !canDry(items.get(i))) return remove(i);
        }
        for (int i = SLOTS - 1; i >= 0; i--) {
            if (!items.get(i).isEmpty()) return remove(i);
        }
        return ItemStack.EMPTY;
    }

    private ItemStack remove(int i) {
        ItemStack s = items.get(i);
        items.set(i, ItemStack.EMPTY);
        progress[i] = 0;
        total[i] = 0;
        changed();
        return s;
    }

    public void dropAll() {
        if (level == null) return;
        for (ItemStack s : items) Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), s);
        items.clear();
    }

    /** Drying speed depending on the surroundings. */
    private int speed() {
        if (level == null) return 1;
        boolean sky = level.canSeeSky(worldPosition.above());
        if (sky && level.isRainingAt(worldPosition.above())) return 0;
        float temp = level.getBiome(worldPosition).value().getBaseTemperature();
        int s = 2;
        if (temp >= 1.0f) s += 1;               // desert, savanna, nether
        if (sky && level.isDay()) s += 1;
        if (temp < 0.2f) s -= 1;
        return Math.max(1, s);
    }

    public void serverTick() {
        if (level == null || level.getGameTime() % 2 != 0) return;
        int speed = speed();
        if (speed == 0) return;
        boolean dirty = false;
        for (int i = 0; i < SLOTS; i++) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty()) continue;
            Optional<RecipeHolder<MachineRecipe>> r = recipeFor(stack);
            if (r.isEmpty()) continue;
            total[i] = r.get().value().processingTime();
            progress[i] += speed;
            if (progress[i] >= total[i]) {
                ItemStack result = r.get().value().result().copy();
                int q = Quality.get(stack);
                if (q > 0) Quality.set(result, q);
                items.set(i, result);
                progress[i] = 0;
                total[i] = 0;
                dirty = true;
            } else if (progress[i] % 40 == 0) {
                setChanged();
            }
        }
        if (dirty) {
            level.playSound(null, worldPosition, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 0.4f, 1.6f);
            changed();
        }
    }

    private void changed() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, true, registries);
        tag.putIntArray("Progress", progress);
        tag.putIntArray("Total", total);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.clear();
        ContainerHelper.loadAllItems(tag, items, registries);
        int[] p = tag.getIntArray("Progress");
        int[] t = tag.getIntArray("Total");
        for (int i = 0; i < SLOTS; i++) {
            progress[i] = i < p.length ? p[i] : 0;
            total[i] = i < t.length ? t[i] : 0;
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        ContainerHelper.saveAllItems(tag, items, true, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
