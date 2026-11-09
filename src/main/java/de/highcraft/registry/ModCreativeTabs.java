package de.highcraft.registry;

import de.highcraft.HighCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HighCraft.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.highcraft"))
            .icon(() -> new ItemStack(ModItems.CANNABIS_BUD.get()))
            .displayItems((params, out) -> ModItems.ITEMS.getEntries().forEach(e -> out.accept(e.get())))
            .build());

    private ModCreativeTabs() {}
}
