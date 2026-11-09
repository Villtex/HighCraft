package de.highcraft.block.entity;

import de.highcraft.block.CropQuality;
import de.highcraft.block.MachineType;
import de.highcraft.drug.Quality;
import de.highcraft.recipe.GrowingRecipe;
import de.highcraft.recipe.MachineRecipeInput;
import de.highcraft.registry.ModBlockEntities;
import de.highcraft.registry.ModItems;
import de.highcraft.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Grow tent: grows plants regardless of weather/season.
 * <ul>
 *     <li>Slot 0: seeds · slot 1: fertilizer (optional, +50 % speed, +1 quality) · slot 2: water bucket</li>
 *     <li>Light: FE power (lamp in the tent), a grow lamp nearby or open sky during the day</li>
 * </ul>
 */
public class GrowTentBlockEntity extends AbstractMachineBlockEntity {
    public static final int SLOT_SEED = 0, SLOT_FERTILIZER = 1, SLOT_WATER = 2;
    public static final int LIGHT_NONE = 0, LIGHT_SUN = 1, LIGHT_LAMP = 2, LIGHT_POWER = 3;
    private static final int ENERGY_PER_TICK = 15;

    private int water;
    private int lightState;
    private boolean fertilizedCycle;
    @Nullable
    private RecipeHolder<GrowingRecipe> cachedRecipe;
    private boolean recipeDirty = true;

    // Client rendering
    private Block displayBlock = Blocks.AIR;
    private float displayProgress;

    public GrowTentBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GROW_TENT.get(), pos, state, MachineType.GROW_TENT);
        maxProgress = 2400;
    }

    @Override
    protected boolean isValidForSlot(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_SEED -> level == null || level.getRecipeManager().getAllRecipesFor(ModRecipes.GROWING.get())
                    .stream().anyMatch(r -> r.value().seed().test(stack));
            case SLOT_FERTILIZER -> stack.is(ModItems.FERTILIZER.get()) || stack.is(Items.BONE_MEAL);
            case SLOT_WATER -> stack.is(Items.WATER_BUCKET);
            default -> false;
        };
    }

    @Override
    protected boolean canAutomationExtract(int slot) {
        return machine.isOutput(slot) || (slot == SLOT_WATER && inventory.getStackInSlot(slot).is(Items.BUCKET));
    }

    @Override
    protected void onInventoryChanged(int slot) {
        if (slot == SLOT_SEED) recipeDirty = true;
    }

    @Override
    protected int getDataValue(int index) {
        return switch (index) {
            case MachineType.DATA_WATER -> water;
            case MachineType.DATA_LIGHT -> lightState;
            case MachineType.DATA_FERTILIZED -> fertilizedCycle ? 1 : 0;
            case MachineType.DATA_MAX_PROGRESS -> maxProgress * 2;
            default -> super.getDataValue(index);
        };
    }

    @Nullable
    private RecipeHolder<GrowingRecipe> findRecipe() {
        if (level == null) return null;
        if (recipeDirty) {
            MachineRecipeInput input = new MachineRecipeInput(List.of(inventory.getStackInSlot(SLOT_SEED)));
            cachedRecipe = level.getRecipeManager().getRecipeFor(ModRecipes.GROWING.get(), input, level, cachedRecipe).orElse(null);
            recipeDirty = false;
        }
        return cachedRecipe;
    }

    private int computeLight() {
        if (level == null) return LIGHT_NONE;
        if (energy.getEnergyStored() >= ENERGY_PER_TICK) return LIGHT_POWER;
        if (level instanceof ServerLevel sl && level.getGameTime() % 40 == 0) {
            cachedLamp = CropQuality.hasGrowLampNearby(sl, worldPosition.below(2));
        }
        if (cachedLamp) return LIGHT_LAMP;
        if (level.isDay() && level.canSeeSky(worldPosition.above())) return LIGHT_SUN;
        return LIGHT_NONE;
    }

    private boolean cachedLamp;

    @Override
    public void serverTick() {
        if (level == null) return;
        // Water bucket -> tank
        ItemStack bucket = inventory.getStackInSlot(SLOT_WATER);
        if (bucket.is(Items.WATER_BUCKET) && water + 1000 <= MachineType.WATER_CAPACITY) {
            water += 1000;
            inventory.setStackInSlot(SLOT_WATER, new ItemStack(Items.BUCKET));
        }

        RecipeHolder<GrowingRecipe> holder = findRecipe();
        int newLight = computeLight();
        if (newLight != lightState) {
            lightState = newLight;
            setChanged();
        }
        Block newDisplay = holder == null ? Blocks.AIR : holder.value().displayBlock();

        boolean canRun = holder != null && lightState != LIGHT_NONE && water >= holder.value().water()
                && hasOutputSpace(holder.value());
        if (canRun) {
            GrowingRecipe recipe = holder.value();
            maxProgress = Math.max(20, recipe.growthTime());
            if (progress == 0) {
                ItemStack fert = inventory.getStackInSlot(SLOT_FERTILIZER);
                fertilizedCycle = !fert.isEmpty();
                if (fertilizedCycle) {
                    fert = fert.copy();
                    fert.shrink(1);
                    inventory.setStackInSlot(SLOT_FERTILIZER, fert);
                }
            }
            if (lightState == LIGHT_POWER) energy.consume(ENERGY_PER_TICK);
            int step = fertilizedCycle ? 3 : 2;            // fertilizer: +50 %
            if (lightState == LIGHT_SUN) step = Math.max(1, step - 1); // sun in the tent: slower
            progress += step;
            if (progress >= maxProgress * 2) {
                harvest(recipe);
                progress = 0;
                fertilizedCycle = false;
            }
            setChanged();
        }
        float shown = holder == null ? 0f : Math.min(1f, progress / (float) (maxProgress * 2));
        // Only send to clients on a visible change (8 stages)
        if (newDisplay != displayBlock || (int) (shown * 8) != (int) (displayProgress * 8)) {
            displayBlock = newDisplay;
            displayProgress = shown;
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        setLit(canRun && lightState == LIGHT_POWER);
    }

    private boolean hasOutputSpace(GrowingRecipe recipe) {
        for (GrowingRecipe.Output o : recipe.outputs()) {
            ItemStack test = o.stack().copy();
            Quality.set(test, 5);
            boolean fits = false;
            for (int s = machine.firstOutputSlot(); s < machine.slotCount(); s++) {
                ItemStack existing = inventory.getStackInSlot(s);
                if (existing.isEmpty() || (existing.is(test.getItem()) && existing.getCount() + test.getCount() <= existing.getMaxStackSize())) {
                    fits = true;
                    break;
                }
            }
            if (!fits) return false;
        }
        return true;
    }

    private void harvest(GrowingRecipe recipe) {
        water -= recipe.water();
        ItemStack seed = inventory.getStackInSlot(SLOT_SEED).copy();
        seed.shrink(1);
        inventory.setStackInSlot(SLOT_SEED, seed);
        recipeDirty = true;

        int quality = 1;
        if (fertilizedCycle) quality++;
        if (lightState == LIGHT_POWER || lightState == LIGHT_LAMP) quality++;
        if (water >= MachineType.WATER_CAPACITY / 2) quality++;
        if (level.random.nextFloat() < 0.3f) quality++;

        for (GrowingRecipe.Output o : recipe.outputs()) {
            if (level.random.nextFloat() <= o.chance()) {
                ItemStack out = o.stack().copy();
                if (!out.is(net.minecraft.tags.ItemTags.VILLAGER_PLANTABLE_SEEDS) && !isSeed(out)) Quality.set(out, quality);
                insertOutput(out, false);
            }
        }
    }

    private static boolean isSeed(ItemStack stack) {
        return stack.is(ModItems.CANNABIS_SEEDS.get()) || stack.is(ModItems.TOBACCO_SEEDS.get())
                || stack.is(ModItems.COCA_SEEDS.get()) || stack.is(ModItems.POPPY_SEEDS.get())
                || stack.is(ModItems.MUSHROOM_SPORES.get());
    }

    public int water() { return water; }
    public Block displayBlock() { return displayBlock; }
    public float displayProgress() { return displayProgress; }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Water", water);
        tag.putBoolean("Fertilized", fertilizedCycle);
        tag.putString("Display", BuiltInRegistries.BLOCK.getKey(displayBlock).toString());
        tag.putFloat("DisplayProgress", displayProgress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        water = tag.getInt("Water");
        fertilizedCycle = tag.getBoolean("Fertilized");
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("Display"));
        displayBlock = id == null ? Blocks.AIR : BuiltInRegistries.BLOCK.get(id);
        displayProgress = tag.getFloat("DisplayProgress");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Display", BuiltInRegistries.BLOCK.getKey(displayBlock).toString());
        tag.putFloat("DisplayProgress", displayProgress);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
