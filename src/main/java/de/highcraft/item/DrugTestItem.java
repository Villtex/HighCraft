package de.highcraft.item;

import de.highcraft.drug.DrugType;
import de.highcraft.drug.PlayerDrugData;
import de.highcraft.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Drug test - shows the player's intoxication, addiction and tolerance. */
public class DrugTestItem extends Item {
    public DrugTestItem(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            sendReport(player, player);
            level.playSound(null, player.blockPosition(), SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.PLAYERS, 0.6f, 1.5f);
            player.getCooldowns().addCooldown(this, 40);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static void sendReport(Player target, Player receiver) {
        PlayerDrugData data = target.getData(ModAttachments.DRUG_DATA);
        receiver.sendSystemMessage(Component.translatable("message.highcraft.test_header", target.getDisplayName()).withStyle(ChatFormatting.GOLD));
        receiver.sendSystemMessage(Component.translatable("message.highcraft.test_intox", bar(data.intoxication() / 170f, ChatFormatting.LIGHT_PURPLE),
                Math.round(data.intoxication())));
        for (DrugType t : DrugType.values()) {
            float a = data.addiction(t);
            float tol = data.tolerance(t);
            if (a < 0.5f && tol < 0.01f && data.doses(t) == 0) continue;
            receiver.sendSystemMessage(Component.literal(" ").append(t.displayName()).append(": ")
                    .append(Component.translatable("message.highcraft.test_addiction", bar(a / 100f, ChatFormatting.RED), Math.round(a)))
                    .append("  ")
                    .append(Component.translatable("message.highcraft.test_tolerance", Math.round(tol * 100))));
        }
    }

    private static MutableComponent bar(float fraction, ChatFormatting color) {
        int filled = Math.round(Math.max(0, Math.min(1, fraction)) * 10);
        return Component.literal("|".repeat(filled)).withStyle(color)
                .append(Component.literal("|".repeat(10 - filled)).withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.highcraft.drug_test").withStyle(ChatFormatting.GRAY));
    }
}
