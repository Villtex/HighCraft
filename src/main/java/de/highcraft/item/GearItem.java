package de.highcraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Lab protective gear (gas mask, hazmat suit) with a short explanation in the tooltip. */
public class GearItem extends ArmorItem {
    private final String key;

    public GearItem(Holder<ArmorMaterial> material, Type type, Properties properties, String key) {
        super(material, type, properties);
        this.key = key;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.highcraft." + key).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.highcraft.hazmat_set").withStyle(ChatFormatting.DARK_GREEN));
    }
}
