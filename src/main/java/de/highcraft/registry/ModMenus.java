package de.highcraft.registry;

import de.highcraft.HighCraft;
import de.highcraft.block.MachineType;
import de.highcraft.menu.MachineMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, HighCraft.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<MachineMenu>> GRINDER = menu(MachineType.GRINDER);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineMenu>> EXTRACTOR = menu(MachineType.EXTRACTOR);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineMenu>> LAB_STATION = menu(MachineType.LAB);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineMenu>> PRESS = menu(MachineType.PRESS);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineMenu>> GROW_TENT = menu(MachineType.GROW_TENT);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineMenu>> CHEMICAL_REACTOR = menu(MachineType.REACTOR);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineMenu>> CRYSTALLIZER = menu(MachineType.CRYSTALLIZER);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineMenu>> PILL_PRESS = menu(MachineType.PILL_PRESS);

    private static DeferredHolder<MenuType<?>, MenuType<MachineMenu>> menu(MachineType type) {
        return MENUS.register(type.id, () -> IMenuTypeExtension.create((id, inv, buf) -> MachineMenu.client(type, id, inv, buf)));
    }

    public static MenuType<MachineMenu> get(MachineType type) {
        return holder(type).get();
    }

    public static DeferredHolder<MenuType<?>, MenuType<MachineMenu>> holder(MachineType type) {
        return switch (type) {
            case GRINDER -> GRINDER;
            case EXTRACTOR -> EXTRACTOR;
            case LAB -> LAB_STATION;
            case PRESS -> PRESS;
            case GROW_TENT -> GROW_TENT;
            case REACTOR -> CHEMICAL_REACTOR;
            case CRYSTALLIZER -> CRYSTALLIZER;
            case PILL_PRESS -> PILL_PRESS;
        };
    }

    public static Block blockFor(MachineType type) {
        return switch (type) {
            case GRINDER -> ModBlocks.GRINDER.get();
            case EXTRACTOR -> ModBlocks.EXTRACTOR.get();
            case LAB -> ModBlocks.LAB_STATION.get();
            case PRESS -> ModBlocks.PRESS.get();
            case GROW_TENT -> ModBlocks.GROW_TENT.get();
            case REACTOR -> ModBlocks.CHEMICAL_REACTOR.get();
            case CRYSTALLIZER -> ModBlocks.CRYSTALLIZER.get();
            case PILL_PRESS -> ModBlocks.PILL_PRESS.get();
        };
    }

    private ModMenus() {}
}
