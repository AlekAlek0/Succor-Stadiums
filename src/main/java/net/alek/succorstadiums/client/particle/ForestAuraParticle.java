package net.alek.succorstadiums.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.NonNull;

public class ForestAuraParticle extends SingleQuadParticle {

    private final boolean small;
    private final SpriteSet sprites;

    protected ForestAuraParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double dx,
            double dy,
            double dz,
            SpriteSet sprites,
            boolean small
    ) {
        super(level, x, y, z, dx, dy, dz, sprites.get(0, 1));

        this.sprites = sprites;
        this.small = small;

        this.friction = 1.0F;
        this.gravity = 0.0F;
        this.hasPhysics = false;

        this.lifetime = 16 + this.random.nextInt(8);

        this.quadSize *= small ? 0.4F : 0.7F;

        this.xd = 0.0;
        this.yd = 0.01 + this.random.nextDouble() * 0.01;
        this.zd = 0.0;

        this.setSpriteFromAge(sprites);
        this.setAlpha(small ? 0.55F : 0.8F);
    }

    @Override
    public void tick() {
        super.tick();

        float lifeRatio = (float) this.age / (float) this.lifetime;

        // Fade in briefly, hold, then fade out near the end — matches a
        // gentle "appear, float up, vanish" arc rather than instant fade
        float baseAlpha = small ? 0.55F : 0.8F;
        if (lifeRatio < 0.15F) {
            this.setAlpha(baseAlpha * (lifeRatio / 0.15F));
        } else if (lifeRatio > 0.7F) {
            this.setAlpha(baseAlpha * (1.0F - (lifeRatio - 0.7F) / 0.3F));
        } else {
            this.setAlpha(baseAlpha);
        }

        this.setSpriteFromAge(this.sprites);
    }

    @Override
    protected @NonNull Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;
        private final boolean small;

        public Provider(SpriteSet sprites, boolean small) {
            this.sprites = sprites;
            this.small = small;
        }

        @Override
        public Particle createParticle(@NonNull SimpleParticleType type, @NonNull ClientLevel level, double x, double y, double z,
                                       double dx, double dy, double dz, @NonNull RandomSource random) {
            return new ForestAuraParticle(level, x, y, z,
                    dx, dy, dz, sprites, small
            );
        }
    }
}