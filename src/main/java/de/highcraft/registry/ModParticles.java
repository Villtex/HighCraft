package de.highcraft.registry;

import de.highcraft.HighCraft;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, HighCraft.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SMOKE_PUFF = PARTICLES.register("smoke_puff", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TRIP_SPARKLE = PARTICLES.register("trip_sparkle", () -> new SimpleParticleType(true));

    private ModParticles() {}
}
