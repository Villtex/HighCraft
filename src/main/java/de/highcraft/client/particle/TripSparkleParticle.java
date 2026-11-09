package de.highcraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

/** Hallucination: glowing, colour-changing sparks slowly drifting through the air. */
public class TripSparkleParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final float hueOffset;
    private final float baseSize;

    protected TripSparkleParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, SpriteSet sprites) {
        super(level, x, y, z, dx, dy, dz);
        this.sprites = sprites;
        this.xd = dx;
        this.yd = dy;
        this.zd = dz;
        this.lifetime = 40 + random.nextInt(40);
        this.hueOffset = random.nextFloat();
        this.baseSize = 0.06f + random.nextFloat() * 0.1f;
        this.quadSize = baseSize;
        this.friction = 0.98f;
        this.gravity = 0f;
        this.hasPhysics = false;
        setSpriteFromAge(sprites);
        updateColor();
    }

    private void updateColor() {
        float hue = (hueOffset + age * 0.02f) % 1f;
        int rgb = Mth.hsvToRgb(hue, 0.75f, 1f);
        setColor(((rgb >> 16) & 255) / 255f, ((rgb >> 8) & 255) / 255f, (rgb & 255) / 255f);
    }

    @Override
    public void tick() {
        super.tick();
        if (removed) return;
        setSpriteFromAge(sprites);
        updateColor();
        float life = age / (float) lifetime;
        quadSize = baseSize * (0.6f + 0.4f * Mth.sin(age * 0.4f)) * (life < 0.2f ? life * 5 : 1f);
        alpha = life > 0.7f ? (1f - life) / 0.3f : 1f;
        xd += Mth.sin(age * 0.15f + hueOffset * 6f) * 0.002;
        zd += Mth.cos(age * 0.15f + hueOffset * 6f) * 0.002;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
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
            return new TripSparkleParticle(level, x, y, z, dx, dy, dz, sprites);
        }
    }
}
