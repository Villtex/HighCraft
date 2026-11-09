package de.highcraft.item;

import de.highcraft.config.HighCraftConfig;
import de.highcraft.drug.DrugDose;
import de.highcraft.drug.DrugHandler;
import de.highcraft.drug.Quality;
import de.highcraft.registry.ModParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Supplier;

/**
 * Consumable drug. The way it is consumed ({@link Style}) decides animation, duration,
 * sounds and whether a lighter is needed.
 */
public class DrugItem extends Item {
    public enum Style {
        SMOKE(48, UseAnim.TOOT_HORN, true),
        SNORT(24, UseAnim.TOOT_HORN, false),
        EAT(32, UseAnim.EAT, false),
        DRINK(32, UseAnim.DRINK, false),
        INJECT(28, UseAnim.BOW, false),
        /** Put the blotter on your tongue */
        TAB(16, UseAnim.EAT, false);

        final int duration;
        final UseAnim anim;
        final boolean needsLighter;

        Style(int duration, UseAnim anim, boolean needsLighter) {
            this.duration = duration;
            this.anim = anim;
            this.needsLighter = needsLighter;
        }
    }

    private final Supplier<DrugDose> dose;
    private final Style style;
    private final Supplier<? extends Item> remainder;

    public DrugItem(Properties properties, Supplier<DrugDose> dose, Style style, Supplier<? extends Item> remainder) {
        super(properties);
        this.dose = dose;
        this.style = style;
        this.remainder = remainder;
    }

    public DrugItem(Properties properties, Supplier<DrugDose> dose, Style style) {
        this(properties, dose, style, null);
    }

    public DrugDose dose() { return dose.get(); }
    public Style style() { return style; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (style == Style.EAT && stack.getFoodProperties(player) != null) {
            // Edibles: via the normal eating system (hunger is ignored: alwaysEdible)
            return super.use(level, player, hand);
        }
        if (style.needsLighter && !LighterHelper.hasLighter(player)) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable("message.highcraft.need_lighter").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }
        if (style.needsLighter && !level.isClientSide) LighterHelper.useLighter(player);
        if (style == Style.SNORT) level.playSound(null, player.blockPosition(), SoundEvents.SNIFFER_SNIFFING, SoundSource.PLAYERS, 0.8f, 1.5f);
        if (style == Style.INJECT) level.playSound(null, player.blockPosition(), SoundEvents.BEEHIVE_SHEAR, SoundSource.PLAYERS, 0.5f, 1.8f);
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (style == Style.SMOKE && level.isClientSide && remaining % 4 == 0 && HighCraftConfig.ENABLE_PARTICLES.get()) {
            // Glowing tip
            Vec3 look = entity.getLookAngle();
            Vec3 tip = entity.getEyePosition().add(look.scale(0.6)).add(0, -0.15, 0);
            level.addParticle(ModParticles.SMOKE_PUFF.get(), tip.x, tip.y, tip.z, look.x * 0.01, 0.015, look.z * 0.01);
            if (remaining % 12 == 0) level.addParticle(net.minecraft.core.particles.ParticleTypes.SMALL_FLAME, tip.x, tip.y, tip.z, 0, 0.005, 0);
        }
        if (style == Style.SMOKE && !level.isClientSide && remaining % 16 == 0) {
            level.playSound(null, entity.blockPosition(), SoundEvents.CAMPFIRE_CRACKLE, SoundSource.PLAYERS, 0.4f, 1.6f);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        float qualityMult = Quality.multiplier(stack);
        ItemStack result;
        if (style == Style.EAT && stack.getFoodProperties(entity) != null) {
            result = super.finishUsingItem(stack, level, entity);
        } else {
            if (entity instanceof Player p && !p.getAbilities().instabuild) stack.shrink(1);
            result = stack;
        }
        if (!level.isClientSide) {
            DrugHandler.consume(entity, dose.get(), qualityMult);
            if (style == Style.SMOKE) {
                DrugHandler.exhaleSmoke(entity, 8);
                level.playSound(null, entity.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.2f, 1.8f);
            }
            if (entity instanceof ServerPlayer sp) sp.awardStat(Stats.ITEM_USED.get(this));
        }
        if (remainder != null && entity instanceof Player p && !p.getAbilities().instabuild) {
            ItemStack rem = new ItemStack(remainder.get());
            if (result.isEmpty()) return rem;
            if (!p.getInventory().add(rem)) p.drop(rem, false);
        }
        return result;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        if (style == Style.EAT && stack.getFoodProperties(entity) != null) return super.getUseDuration(stack, entity);
        return style.duration;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return style.anim;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        DrugDose d = dose.get();
        tooltip.add(Component.translatable("tooltip.highcraft.substance", d.type().displayName()).withStyle(ChatFormatting.GRAY));
        if (flag.hasShiftDown()) {
            for (DrugDose.Entry e : d.effects()) {
                Component name = e.effect().value().getDisplayName();
                String level = e.amplifier() > 0 ? " " + Component.translatable("enchantment.level." + (e.amplifier() + 1)).getString() : "";
                tooltip.add(Component.literal(" • ").append(name).append(level + " (" + (e.duration() / 20) + "s)").withStyle(ChatFormatting.DARK_AQUA));
            }
            tooltip.add(Component.translatable("tooltip.highcraft.addictive", Math.round(d.addiction() * d.type().addictiveness() * 10) / 10f)
                    .withStyle(ChatFormatting.DARK_RED));
            if (style.needsLighter) tooltip.add(Component.translatable("tooltip.highcraft.needs_lighter").withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltip.add(Component.translatable("tooltip.highcraft.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
