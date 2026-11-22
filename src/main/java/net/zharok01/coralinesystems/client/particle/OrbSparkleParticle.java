package net.zharok01.coralinesystems.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class OrbSparkleParticle extends TextureSheetParticle {

    private final SpriteSet sprites;

    protected OrbSparkleParticle(ClientLevel level, double x, double y, double z,
                                 double xd, double yd, double zd,
                                 SpriteSet sprites, float baseR, float baseG, float baseB) {
        super(level, x, y, z);
        this.sprites = sprites;

        this.xd = xd;
        this.yd = yd;
        this.zd = zd;

        this.quadSize = 0.08f + this.random.nextFloat() * 0.04f;

        this.rCol = baseR;
        this.gCol = Math.min(1.0f, baseG + this.random.nextFloat() * 0.15f);
        this.bCol = Math.min(1.0f, baseB + this.random.nextFloat() * 0.15f);

        this.lifetime = this.random.nextInt(12) + 6;
        this.gravity = 0.06f;

        this.setSpriteFromAge(sprites);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    @Override
    protected int getLightColor(float partialTick) {
        int packed = super.getLightColor(partialTick);
        int block = LightTexture.block(packed);
        int sky = LightTexture.sky(packed);
        return LightTexture.pack(Math.max(block, 11), sky);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(sprites);
    }

    // Pink
    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level,
                                       double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new OrbSparkleParticle(level, x, y, z, xSpeed, ySpeed, zSpeed,
                    sprites, 1.0f, 0.75f, 0.85f);
        }
    }

    // Cyan (Blaze)
    @OnlyIn(Dist.CLIENT)
    public static class BlazeFactory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public BlazeFactory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level,
                                       double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new OrbSparkleParticle(level, x, y, z, xSpeed, ySpeed, zSpeed,
                    sprites, 0.5f, 0.9f, 1.0f);
        }
    }

    // Timedust (Red Dragon Egg)
    @OnlyIn(Dist.CLIENT)
    public static class TimeDustFactory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public TimeDustFactory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level,
                                       double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new OrbSparkleParticle(level, x, y, z, xSpeed, ySpeed, zSpeed,
                    sprites, 0.2f, 0.85f, 1.0f);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class StarFactory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public StarFactory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level,
                                       double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new OrbSparkleParticle(level, x, y, z, xSpeed, ySpeed, zSpeed,
                    sprites, 0.55f, 0.82f, 1.0f);
        }
    }
}