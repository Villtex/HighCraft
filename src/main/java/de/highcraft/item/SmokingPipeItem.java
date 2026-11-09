package de.highcraft.item;

import de.highcraft.drug.DrugDose;
import de.highcraft.drug.DrugDoses;
import de.highcraft.drug.DrugHandler;
import de.highcraft.drug.Quality;
import de.highcraft.registry.ModItems;
import de.highcraft.registry.ModParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
 * Pipe - automatically smokes weed, tobacco or opium from the inventory (in that order).
 */
public class SmokingPipeItem extends Item {
    public record Fill(Supplier<? extends Item> item, Supplier<DrugDose> dose) {}

    /** Wooden pipe: weed, tobacco or opium. */
    public static final List<Fill> PIPE_FILLS = List.of(
            new Fill(ModItems.GROUND_CANNABIS, DrugDoses.PIPE_CANNABIS),
            new Fill(ModItems.CUT_TOBACCO, DrugDoses.PIPE_TOBACCO),
            new Fill(ModItems.OPIUM, DrugDoses.PIPE_OPIUM));
    /** Glass pipe: crystal meth only. */
    public static final List<Fill> GLASS_PIPE_FILLS = List.of(new Fill(ModItems.CRYSTAL_METH, DrugDoses.GLASS_PIPE_METH));

    private final List<Fill> fills;
    private final String key;

    public SmokingPipeItem(Properties properties) {
        this(properties, PIPE_FILLS, "pipe");
    }

    public SmokingPipeItem(Properties properties, List<Fill> fills, String key) {
        super(properties);
        this.fills = fills;
        this.key = key;
    }

    private ItemStack findFill(Player player, Fill[] out) {
        for (Fill f : fills) {
            for (ItemStack s : player.getInventory().items) {
                if (s.is(f.item().get())) {
                    out[0] = f;
                    return s;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Fill[] f = new Fill[1];
        if (findFill(player, f).isEmpty()) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable("message.highcraft." + key + "_empty").withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.fail(stack);
        }
        if (!LighterHelper.hasLighter(player)) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable("message.highcraft.need_lighter").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide) LighterHelper.useLighter(player);
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (level.isClientSide && remaining % 5 == 0) {
            Vec3 look = entity.getLookAngle();
            Vec3 tip = entity.getEyePosition().add(look.scale(0.55)).add(0, -0.1, 0);
            level.addParticle(ModParticles.SMOKE_PUFF.get(), tip.x, tip.y, tip.z, 0, 0.02, 0);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            Fill[] f = new Fill[1];
            ItemStack fill = findFill(player, f);
            if (!fill.isEmpty()) {
                float mult = Quality.multiplier(fill);
                if (!player.getAbilities().instabuild) fill.shrink(1);
                DrugHandler.consume(player, f[0].dose().get(), mult);
                DrugHandler.exhaleSmoke(player, 8);
                level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.2f, 1.8f);
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
            }
        }
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) { return 48; }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.TOOT_HORN; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.highcraft." + key).withStyle(ChatFormatting.GRAY));
    }
}
