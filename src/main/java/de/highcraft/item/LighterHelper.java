package de.highcraft.item;

import de.highcraft.registry.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Finds a lighter (or flint and steel) in the inventory and uses 1 durability. */
public final class LighterHelper {
    private LighterHelper() {}

    public static boolean hasLighter(Player player) {
        if (player.getAbilities().instabuild) return true;
        for (ItemStack s : player.getInventory().items) if (isLighter(s)) return true;
        return isLighter(player.getOffhandItem());
    }

    public static boolean useLighter(Player player) {
        if (player.getAbilities().instabuild) return true;
        ItemStack off = player.getOffhandItem();
        if (isLighter(off)) {
            damage(player, off, EquipmentSlot.OFFHAND);
            return true;
        }
        for (ItemStack s : player.getInventory().items) {
            if (isLighter(s)) {
                damage(player, s, null);
                return true;
            }
        }
        return false;
    }

    private static boolean isLighter(ItemStack s) {
        return s.is(ModItems.LIGHTER.get()) || s.is(Items.FLINT_AND_STEEL);
    }

    private static void damage(Player player, ItemStack stack, EquipmentSlot slot) {
        if (player.level().isClientSide) return;
        player.level().playSound(null, player.blockPosition(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 0.6f, 1.3f);
        if (slot != null) stack.hurtAndBreak(1, player, slot);
        else if (player.level() instanceof net.minecraft.server.level.ServerLevel sl) {
            stack.hurtAndBreak(1, sl, player, item -> {});
        }
    }
}
