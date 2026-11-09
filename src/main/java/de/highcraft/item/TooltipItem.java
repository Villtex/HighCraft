package de.highcraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Simple item with a descriptive tooltip ({@code tooltip.highcraft.<name>}). */
public class TooltipItem extends Item {
    private final String key;

    public TooltipItem(Properties properties, String key) {
        super(properties);
        this.key = key;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.highcraft." + key).withStyle(ChatFormatting.GRAY));
    }
}
