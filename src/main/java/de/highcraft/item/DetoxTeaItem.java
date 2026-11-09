package de.highcraft.item;

import de.highcraft.drug.DrugType;
import de.highcraft.drug.PlayerDrugData;
import de.highcraft.registry.ModAttachments;
import de.highcraft.registry.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

/** Detox tea - halves addiction and tolerance, ends withdrawal and lowers intoxication. */
public class DetoxTeaItem extends Item {
    public DetoxTeaItem(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            PlayerDrugData data = player.getData(ModAttachments.DRUG_DATA);
            for (DrugType t : DrugType.values()) {
                data.setAddiction(t, data.addiction(t) * 0.5f);
                data.setTolerance(t, data.tolerance(t) * 0.5f);
                data.setLastUse(t, level.getGameTime());
            }
            data.setIntoxication(data.intoxication() * 0.5f);
            player.removeEffect(ModEffects.WITHDRAWAL.getDelegate());
            player.removeEffect(ModEffects.OVERDOSE.getDelegate());
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
            player.displayClientMessage(Component.translatable("message.highcraft.detox").withStyle(ChatFormatting.AQUA), true);
        }
        if (entity instanceof Player p && !p.getAbilities().instabuild) {
            stack.shrink(1);
            if (stack.isEmpty()) return new ItemStack(Items.GLASS_BOTTLE);
            p.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
        }
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) { return 32; }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.DRINK; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.highcraft.detox_tea").withStyle(ChatFormatting.GRAY));
    }
}
