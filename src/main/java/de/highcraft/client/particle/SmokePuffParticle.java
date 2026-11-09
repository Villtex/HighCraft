package de.highcraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/** Soft, slowly rising smoke that grows and becomes more transparent. */
public class SmokePuffParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final float startSize;

    protected SmokePuffParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, SpriteSet sprites) {
        super(level, x, y, z, dx, dy, dz);
        this.sprites = sprites;
        this.xd = dx + (random.nextDouble() - 0.5) * 0.01;
        this.yd = dy + 0.01 + random.nextDouble() * 0.01;
        this.zd = dz + (random.nextDouble() - 0.5) * 0.01;
        this.lifetime = 50 + random.nextInt(30);
        this.startSize = 0.12f + random.nextFloat() * 0.08f;
        this.quadSize = startSize;
        float grey = 0.82f + random.nextFloat() * 0.12f;
        this.setColor(grey, grey, grey);
        this.alpha = 0.55f;
        this.friction = 0.96f;
        this.gravity = -0.002f;
        this.roll = random.nextFloat() * 6.28f;
        this.oRoll = roll;
        setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (!removed) {
            setSpriteFromAge(sprites);
            float life = age / (float) lifetime;
            quadSize = startSize * (1f + life * 3.5f);
            alpha = 0.55f * (1f - life);
            oRoll = roll;
            roll += 0.01f;
            xd += (random.nextDouble() - 0.5) * 0.002;
            zd += (random.nextDouble() - 0.5) * 0.002;
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new SmokePuffParticle(level, x, y, z, dx, dy, dz, sprites);
        }
    }
}
