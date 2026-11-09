package de.highcraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Scoring knife - for scoring ripe poppy pods (see PoppyCropBlock). */
public class ScoringKnifeItem extends Item {
    public ScoringKnifeItem(Properties properties) { super(properties); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.highcraft.scoring_knife").withStyle(ChatFormatting.GRAY));
    }
}
