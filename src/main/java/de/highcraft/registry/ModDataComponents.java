package de.highcraft.registry;

import com.mojang.serialization.Codec;
import de.highcraft.HighCraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, HighCraft.MODID);

    /** Quality 1..5 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> QUALITY = COMPONENTS.register("quality",
            () -> DataComponentType.<Integer>builder().persistent(Codec.intRange(1, 5)).networkSynchronized(ByteBufCodecs.VAR_INT).build());

    private ModDataComponents() {}
}
