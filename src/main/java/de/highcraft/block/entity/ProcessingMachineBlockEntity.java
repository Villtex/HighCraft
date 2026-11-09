package de.highcraft.block.entity;

import de.highcraft.block.MachineType;
import de.highcraft.config.HighCraftConfig;
import de.highcraft.drug.Quality;
import de.highcraft.lab.LabSafety;
import net.minecraft.server.level.ServerLevel;
import de.highcraft.recipe.MachineRecipe;
import de.highcraft.recipe.MachineRecipeInput;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Grinder, extractor, chemistry lab, press, chemical reactor, crystallizer and pill press:
 * inputs -> (fuel, coolant or FE) -> output.
 * <ul>
 *     <li>Chemical reactor: needs ventilation (fume hood / exhaust fan nearby), otherwise toxic fumes,
 *     possible accidents and lower quality.</li>
 *     <li>Crystallizer: uses coolant (ice) instead of fuel, or runs on passive cold (ice next to it, cold biome).</li>
 * </ul>
 * The recipe, the slot assignment, the predicted result and the output space are cached and only recomputed
 * when a slot changes, the lab conditions change or the recipes are reloaded - a running machine does no
 * recipe matching and no allocations per tick. {@link #craft} re-validates everything before it consumes inputs.
 */
public class ProcessingMachineBlockEntity extends AbstractMachineBlockEntity {
    // Recipe lookup + slot assignment: refreshed when an input slot changes or the recipes are reloaded
    @Nullable
    private RecipeHolder<MachineRecipe> cachedRecipe;
    @Nullable
    private int[] cachedMatch;
    @Nullable
    private RecipeManager cachedRecipeManager;
    private boolean recipeDirty = true;
    // Predicted result + output space: refreshed when any slot or the lab conditions change
    private ItemStack cachedResult = ItemStack.EMPTY;
    private boolean cachedCanRun;
    private boolean outputDirty = true;
    private int cachedConditions = -1;

    /** Reactor: 1 = ventilated; crystallizer: 0 warm, 1 cold, 2 deep cold (3 = coolant running, see getDataValue). */
    private int environment = -1;
    private int envTimer;

    public ProcessingMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, MachineType machine) {
        super(type, pos, state, machine);
    }

    @Override
    protected boolean isValidForSlot(int slot, ItemStack stack) {
        if (machine.isOutput(slot)) return false;
        if (slot == machine.fuelSlot()) {
            return machine.usesCoolant() ? LabSafety.coolantValue(stack) > 0 : stack.getBurnTime(RecipeType.SMELTING) > 0;
        }
        if (level == null) return true;
        for (RecipeHolder<MachineRecipe> r : level.getRecipeManager().getAllRecipesFor(machine.kind.type())) {
            if (r.value().isIngredient(stack)) return true;
        }
        return false;
    }

    @Override
    protected void onInventoryChanged(int slot) {
        if (slot < machine.inputCount()) recipeDirty = true;
        outputDirty = true;
    }

    private List<ItemStack> inputs() {
        List<ItemStack> list = new ArrayList<>(machine.inputCount());
        for (int i = 0; i < machine.inputCount(); i++) list.add(inventory.getStackInSlot(i));
        return list;
    }

    private void refreshRecipe() {
        RecipeManager recipes = level.getRecipeManager();
        if (recipes != cachedRecipeManager) {
            // /reload creates a new RecipeManager - the cached holder may be outdated
            cachedRecipeManager = recipes;
            cachedRecipe = null;
            recipeDirty = true;
        }
        if (!recipeDirty) return;
        recipeDirty = false;
        outputDirty = true;
        List<ItemStack> inputs = inputs();
        cachedRecipe = recipes.getRecipeFor(machine.kind.type(), new MachineRecipeInput(inputs), level, cachedRecipe).orElse(null);
        cachedMatch = cachedRecipe == null ? null : cachedRecipe.value().match(inputs);
    }

    /** Lab conditions that change the predicted quality (see {@link #predictResult}). */
    private int conditions() {
        if (machine == MachineType.REACTOR) return environment;
        if (machine == MachineType.CRYSTALLIZER) return environment >= 2 || burnTime > 0 ? 1 : 0;
        return 0;
    }

    /** Result including the carried-over quality (and lab conditions for reactor / crystallizer). */
    private ItemStack predictResult(MachineRecipe recipe, int[] match) {
        ItemStack result = recipe.result().copy();
        List<ItemStack> used = new ArrayList<>(match.length);
        for (int slot : match) used.add(inventory.getStackInSlot(slot));
        int q = Quality.average(used);
        if (machine == MachineType.REACTOR) {
            q = (q > 0 ? q : 2) + (environment == 1 ? 1 : -1);
        } else if (machine == MachineType.CRYSTALLIZER && q > 0 && (environment >= 2 || burnTime > 0)) {
            q += 1;
        }
        q = Math.min(5, q);
        return q > 0 ? Quality.set(result, Math.max(1, q)) : result;
    }

    @Override
    protected int getDataValue(int index) {
        if (index == MachineType.DATA_STATUS && (machine == MachineType.REACTOR || machine == MachineType.CRYSTALLIZER)) {
            if (machine == MachineType.CRYSTALLIZER && burnTime > 0) return 3;
            return Math.max(0, environment);
        }
        return super.getDataValue(index);
    }

    private void updateEnvironment() {
        if (environment >= 0 && --envTimer > 0) return;
        envTimer = 40;
        if (machine == MachineType.REACTOR) environment = LabSafety.isVentilated(level, worldPosition) ? 1 : 0;
        else if (machine == MachineType.CRYSTALLIZER) environment = LabSafety.passiveCooling(level, worldPosition);
        else environment = 0;
    }

    @Override
    public void serverTick() {
        if (level == null) return;
        updateEnvironment();
        refreshRecipe();
        int conditions = conditions();
        if (outputDirty || conditions != cachedConditions) {
            outputDirty = false;
            cachedConditions = conditions;
            cachedResult = cachedMatch == null ? ItemStack.EMPTY : predictResult(cachedRecipe.value(), cachedMatch);
            cachedCanRun = cachedMatch != null && canFitOutput(cachedResult);
        }
        boolean canRun = cachedCanRun;
        int ept = HighCraftConfig.MACHINE_ENERGY_PER_TICK.get();

        boolean powered = false;
        if (canRun) {
            if (energy.getEnergyStored() >= ept) {
                energy.consume(ept);
                powered = true;
            } else if (burnTime > 0) {
                powered = true;
            } else if (machine == MachineType.CRYSTALLIZER && environment >= 1) {
                // Passive cold: deep cold runs at full speed, plain cold at half speed
                powered = environment >= 2 || level.getGameTime() % 2 == 0;
            } else if (machine.hasFuel()) {
                ItemStack fuel = inventory.getStackInSlot(machine.fuelSlot());
                int bt = machine.usesCoolant() ? LabSafety.coolantValue(fuel) : fuel.getBurnTime(RecipeType.SMELTING);
                if (bt > 0) {
                    burnTime = burnDuration = bt;
                    ItemStack remainder = fuel.getCraftingRemainingItem();
                    fuel.shrink(1);
                    if (fuel.isEmpty() && !remainder.isEmpty()) inventory.setStackInSlot(machine.fuelSlot(), remainder);
                    else inventory.setStackInSlot(machine.fuelSlot(), fuel);
                    powered = true;
                }
            }
        }
        if (burnTime > 0) {
            burnTime--;
            markUnsaved();
        }

        if (canRun && powered && machine == MachineType.REACTOR && environment == 0
                && level.getGameTime() % 40 == 0 && level instanceof ServerLevel server) {
            LabSafety.emitFumes(server, worldPosition);
        }

        if (canRun && powered) {
            maxProgress = Math.max(1, cachedRecipe.value().processingTime());
            progress++;
            if (progress >= maxProgress) {
                craft(cachedRecipe.value());
                progress = 0;
            }
            markUnsaved();
        } else if (progress > 0 && !canRun) {
            progress = Math.max(0, progress - 2);
            markUnsaved();
        }
        setLit(burnTime > 0 || (canRun && powered));
    }

    private void craft(MachineRecipe recipe) {
        // The per-tick state is cached - re-validate against the actual slots before consuming anything
        int[] match = recipe.match(inputs());
        if (match == null) {
            recipeDirty = true;
            return;
        }
        ItemStack result = predictResult(recipe, match);
        if (!canFitOutput(result)) {
            outputDirty = true;
            return;
        }
        boolean ruined = false;
        if (machine == MachineType.REACTOR && level instanceof ServerLevel server) {
            if (environment == 0 && HighCraftConfig.LAB_ACCIDENTS.get() && server.random.nextFloat() < 0.08f) {
                LabSafety.accident(server, worldPosition);
                ruined = true;
            } else if (Quality.has(result) && server.random.nextFloat() < 0.25f) {
                ItemStack better = Quality.set(result.copy(), Math.min(5, Quality.get(result) + 1));
                // A different quality doesn't stack with the output - only upgrade if it still fits
                if (canFitOutput(better)) result = better;
            }
        }
        for (int i = 0; i < match.length; i++) {
            int slot = match[i];
            ItemStack stack = inventory.getStackInSlot(slot).copy();
            ItemStack remainder = stack.getCraftingRemainingItem();
            stack.shrink(recipe.sizedIngredients().get(i).count());
            inventory.setStackInSlot(slot, stack.isEmpty() && !remainder.isEmpty() ? remainder : stack);
        }
        if (!ruined) dropOverflow(insertOutput(result));
    }
}
