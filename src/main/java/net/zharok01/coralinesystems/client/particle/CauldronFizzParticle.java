package net.zharok01.coralinesystems.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class CauldronFizzParticle extends TextureSheetParticle {

    public static SimpleParticleType TYPE;

    private final float wobbleAmplitude;
    private final float wobbleFrequency;
    private final float wobblePhase;
    private final boolean wobbleOnX;

    private final SpriteSet spriteSet;

    private CauldronFizzParticle(
            ClientLevel level,
            double x, double y, double z,
            float r, float g, float b,
            SpriteSet sprites) {
        super(level, x, y, z, 0.0, 0.0, 0.0);

        this.spriteSet = sprites;

        this.rCol = r;
        this.gCol = g;
        this.bCol = b;

        var rand = level.getRandom();

        this.xd = (rand.nextDouble() - 0.5) * 0.008;
        this.yd = 0.04 + rand.nextDouble() * 0.03;
        this.zd = (rand.nextDouble() - 0.5) * 0.008;

        this.friction = 0.96f;

        this.lifetime = 14 + rand.nextInt(10);

        this.quadSize = 0.10f + rand.nextFloat() * 0.01f;

        this.wobbleAmplitude = 0.004f + rand.nextFloat() * 0.004f;
        this.wobbleFrequency = 0.5f  + rand.nextFloat() * 0.4f;
        this.wobblePhase = rand.nextFloat() * (float) (Math.PI * 2);
        this.wobbleOnX = rand.nextBoolean();

        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        float wobbleDelta = wobbleAmplitude
                * (float) Math.sin(this.age * wobbleFrequency + wobblePhase);
        if (wobbleOnX) {
            this.xd += wobbleDelta;
        } else {
            this.zd += wobbleDelta;
        }

        super.tick();
        this.setSpriteFromAge(this.spriteSet);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                @NotNull SimpleParticleType type,
                @NotNull ClientLevel level,
                double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {

            float r = (float) xSpeed;
            float g = (float) ySpeed;
            float b = (float) zSpeed;

            return new CauldronFizzParticle(level, x, y, z, r, g, b, sprites);
        }
    }
}