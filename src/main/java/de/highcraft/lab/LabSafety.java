package de.highcraft.lab;

import de.highcraft.HighCraft;
import de.highcraft.config.HighCraftConfig;
import de.highcraft.registry.ModBlocks;
import de.highcraft.registry.ModEffects;
import de.highcraft.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

/**
 * Lab rules: an unventilated chemical reactor fills the room with toxic fumes and can have accidents;
 * a fume hood or exhaust fan nearby prevents that. Gas mask = no fume damage, full hazmat suit = immune
 * to fumes and lab accidents. The crystallizer needs cold (coolant, ice next to it or a cold biome).
 */
public final class LabSafety {
    public static final ResourceKey<DamageType> TOXIC_FUMES = ResourceKey.create(Registries.DAMAGE_TYPE, HighCraft.id("toxic_fumes"));
    public static final ResourceKey<DamageType> LAB_ACCIDENT = ResourceKey.create(Registries.DAMAGE_TYPE, HighCraft.id("lab_accident"));

    public static final int VENT_RADIUS = 4;
    public static final int FUME_RADIUS = 5;
    private static final DustParticleOptions FUME_DUST = new DustParticleOptions(new Vector3f(0.55f, 0.75f, 0.2f), 1.6f);

    private LabSafety() {}

    public static DamageSource fumesSource(ServerLevel level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(TOXIC_FUMES));
    }

    public static DamageSource accidentSource(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(LAB_ACCIDENT));
    }

    public static boolean hasGasMask(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.GAS_MASK.get());
    }

    public static boolean hasFullHazmat(LivingEntity entity) {
        return hasGasMask(entity)
                && entity.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.HAZMAT_SUIT.get())
                && entity.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.HAZMAT_PANTS.get())
                && entity.getItemBySlot(EquipmentSlot.FEET).is(ModItems.HAZMAT_BOOTS.get());
    }

    /** True if a fume hood or exhaust fan is within {@link #VENT_RADIUS} blocks. */
    public static boolean isVentilated(Level level, BlockPos pos) {
        Block hood = ModBlocks.FUME_HOOD.get(), fan = ModBlocks.EXHAUST_FAN.get();
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-VENT_RADIUS, -2, -VENT_RADIUS), pos.offset(VENT_RADIUS, 3, VENT_RADIUS))) {
            Block b = level.getBlockState(p).getBlock();
            if (b == hood || b == fan) return true;
        }
        return false;
    }

    /** 0 = warm, 1 = cold (cold biome or ice/snow next to it), 2 = deep cold (packed or blue ice next to it). */
    public static int passiveCooling(Level level, BlockPos pos) {
        int best = 0;
        for (Direction d : Direction.values()) {
            BlockState s = level.getBlockState(pos.relative(d));
            if (s.is(Blocks.BLUE_ICE) || s.is(Blocks.PACKED_ICE)) return 2;
            if (s.is(Blocks.ICE) || s.is(Blocks.SNOW_BLOCK) || s.is(Blocks.POWDER_SNOW)) best = 1;
        }
        if (level.getBiome(pos).value().coldEnoughToSnow(pos)) best = Math.max(best, 1);
        return best;
    }

    /** How long (ticks) a coolant item keeps the crystallizer cold; 0 = not a coolant. */
    public static int coolantValue(net.minecraft.world.item.ItemStack stack) {
        if (stack.is(net.minecraft.world.item.Items.BLUE_ICE)) return 12800;
        if (stack.is(net.minecraft.world.item.Items.PACKED_ICE)) return 3200;
        if (stack.is(net.minecraft.world.item.Items.ICE)) return 800;
        if (stack.is(net.minecraft.world.item.Items.SNOW_BLOCK)) return 600;
        if (stack.is(net.minecraft.world.item.Items.POWDER_SNOW_BUCKET)) return 1200;
        if (stack.is(net.minecraft.world.item.Items.SNOWBALL)) return 150;
        return 0;
    }

    /** Called every 40 ticks while an unventilated reactor runs. */
    public static void emitFumes(ServerLevel level, BlockPos pos) {
        level.sendParticles(FUME_DUST, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 12, 1.2, 0.6, 1.2, 0.01);
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 2, 0.2, 0.1, 0.2, 0.01);
        if (!HighCraftConfig.LAB_FUMES.get()) return;
        AABB box = new AABB(pos).inflate(FUME_RADIUS);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (hasGasMask(e) || hasFullHazmat(e)) continue;
            MobEffectInstance cur = e.getEffect(ModEffects.TOXIC_FUMES.getDelegate());
            if (cur == null || cur.getDuration() < 100) {
                e.addEffect(new MobEffectInstance(ModEffects.TOXIC_FUMES.getDelegate(), 200, 0, false, true, true));
                if (e instanceof ServerPlayer sp && cur == null) {
                    sp.displayClientMessage(Component.translatable("message.highcraft.fumes").withStyle(ChatFormatting.DARK_GREEN), true);
                }
            }
        }
    }

    /** Small explosion without block damage (unventilated reactor, see config). */
    public static void accident(ServerLevel level, BlockPos pos) {
        HighCraft.LOGGER.info("Lab accident: unventilated chemical reactor at {} boiled over", pos.toShortString());
        level.explode(null, accidentSource(level), null, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                2.2f, false, Level.ExplosionInteraction.NONE);
        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.0f, 1.3f);
        for (ServerPlayer p : level.getEntitiesOfClass(ServerPlayer.class, new AABB(pos).inflate(12))) {
            p.displayClientMessage(Component.translatable("message.highcraft.lab_accident").withStyle(ChatFormatting.RED), true);
        }
    }
}
