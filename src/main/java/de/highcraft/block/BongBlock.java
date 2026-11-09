package de.highcraft.block;

import com.mojang.serialization.MapCodec;
import de.highcraft.drug.DrugDose;
import de.highcraft.drug.DrugDoses;
import de.highcraft.drug.DrugHandler;
import de.highcraft.item.LighterHelper;
import de.highcraft.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

/**
 * Glass bong. Fill with water (water bottle/bucket), pack the bowl (weed, tobacco or opium),
 * then take a hit with an empty hand and a lighter in your inventory. Without water it scratches your throat ...
 */
public class BongBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<BongBlock> CODEC = simpleCodec(BongBlock::new);
    public static final BooleanProperty WATER = BooleanProperty.create("water");
    public static final EnumProperty<Load> LOAD = EnumProperty.create("load", Load.class);
    private static final VoxelShape SHAPE = Shapes.or(Block.box(4, 0, 4, 12, 5, 12), Block.box(6, 5, 6, 10, 15, 10));

    public enum Load implements StringRepresentable {
        EMPTY("empty", null), CANNABIS("cannabis", DrugDoses.BONG_CANNABIS),
        TOBACCO("tobacco", DrugDoses.BONG_TOBACCO), OPIUM("opium", DrugDoses.BONG_OPIUM);

        private final String name;
        private final Supplier<DrugDose> dose;

        Load(String name, Supplier<DrugDose> dose) {
            this.name = name;
            this.dose = dose;
        }

        @Override
        public String getSerializedName() { return name; }
    }

    public BongBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATER, false).setValue(LOAD, Load.EMPTY));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATER, LOAD);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) { return SHAPE; }

    private static Load loadFor(ItemStack stack) {
        if (stack.is(ModItems.GROUND_CANNABIS.get())) return Load.CANNABIS;
        if (stack.is(ModItems.CUT_TOBACCO.get())) return Load.TOBACCO;
        if (stack.is(ModItems.OPIUM.get())) return Load.OPIUM;
        return Load.EMPTY;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        // Fill water
        boolean waterBottle = stack.is(Items.POTION) && stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER);
        if (!state.getValue(WATER) && (waterBottle || stack.is(Items.WATER_BUCKET))) {
            if (!level.isClientSide) {
                ItemStack empty = new ItemStack(waterBottle ? Items.GLASS_BOTTLE : Items.BUCKET);
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, empty));
                level.setBlock(pos, state.setValue(WATER, true), 3);
                level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1f, 1f);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        // Pack the bowl
        Load load = loadFor(stack);
        if (load != Load.EMPTY && state.getValue(LOAD) == Load.EMPTY) {
            if (!level.isClientSide) {
                stack.consume(1, player);
                level.setBlock(pos, state.setValue(LOAD, load), 3);
                level.playSound(null, pos, SoundEvents.COMPOSTER_FILL, SoundSource.BLOCKS, 0.6f, 1.4f);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        Load load = state.getValue(LOAD);
        if (load == Load.EMPTY) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable("message.highcraft.bong_empty").withStyle(ChatFormatting.GRAY), true);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!LighterHelper.useLighter(player)) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable("message.highcraft.need_lighter").withStyle(ChatFormatting.RED), true);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (level instanceof ServerLevel server) {
            boolean water = state.getValue(WATER);
            DrugHandler.consume(player, load.dose.get(), 1.0f);
            DrugHandler.exhaleSmoke(player, 10);
            if (water) {
                server.sendParticles(ParticleTypes.BUBBLE_POP, pos.getX() + 0.5, pos.getY() + 0.25, pos.getZ() + 0.5, 12, 0.12, 0.05, 0.12, 0.02);
                level.playSound(null, pos, SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, SoundSource.BLOCKS, 1.0f, 1.2f);
            } else {
                // Without water: coughing!
                player.hurt(level.damageSources().generic(), 1.0f);
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 0, false, false, false));
                player.displayClientMessage(Component.translatable("message.highcraft.cough").withStyle(ChatFormatting.GRAY), true);
            }
            server.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.95, pos.getZ() + 0.5, 6, 0.05, 0.05, 0.05, 0.01);
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.3f, 1.6f);
            // Water gets dirty over time: 25 % chance it needs to be changed
            boolean keepWater = water && level.random.nextFloat() > 0.25f;
            level.setBlock(pos, state.setValue(LOAD, Load.EMPTY).setValue(WATER, keepWater), 3);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
