package de.highcraft.drug;

import de.highcraft.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;

/**
 * Quality tiers 1..5 (stars). Stored as a data component on harvested and processed products
 * and passed on through all machines.
 */
public final class Quality {
    public static final int MIN = 1;
    public static final int MAX = 5;

    private Quality() {}

    public static int get(ItemStack stack) {
        Integer q = stack.get(ModDataComponents.QUALITY.get());
        return q == null ? 0 : q;
    }

    public static boolean has(ItemStack stack) {
        return stack.has(ModDataComponents.QUALITY.get());
    }

    public static ItemStack set(ItemStack stack, int quality) {
        if (quality > 0) stack.set(ModDataComponents.QUALITY.get(), Math.max(MIN, Math.min(MAX, quality)));
        return stack;
    }

    /** Effect multiplier: tier 1 = 1.0, tier 5 = 1.6. No quality = 1.0. */
    public static float multiplier(ItemStack stack) {
        int q = get(stack);
        return q <= 0 ? 1.0f : 1.0f + 0.15f * (q - 1);
    }

    /** Average (rounded) quality of all stacks that have one; 0 if none has a quality. */
    public static int average(Collection<ItemStack> stacks) {
        int sum = 0, n = 0;
        for (ItemStack s : stacks) {
            int q = get(s);
            if (q > 0) { sum += q; n++; }
        }
        return n == 0 ? 0 : Math.round((float) sum / n);
    }

    public static Component stars(int quality) {
        MutableComponent c = Component.empty();
        for (int i = 1; i <= MAX; i++) {
            c.append(Component.literal("★").withStyle(i <= quality ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY));
        }
        return c;
    }

    public static Component tooltip(int quality) {
        return Component.translatable("tooltip.highcraft.quality", stars(quality),
                Component.translatable("quality.highcraft." + quality)).withStyle(ChatFormatting.GRAY);
    }
}
