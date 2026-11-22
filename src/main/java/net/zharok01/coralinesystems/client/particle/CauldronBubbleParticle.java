package net.zharok01.coralinesystems.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class CauldronBubbleParticle extends TextureSheetParticle {

    public static SimpleParticleType TYPE;

    private static final int POP_FRAMES = 4;

    private static final int POP_DURATION = 6;

    private final SpriteSet spriteSet;
    private final int totalFrames;

    private CauldronBubbleParticle(
            ClientLevel level,
            double x, double y, double z,
            float r, float g, float b,
            SpriteSet sprites,
            int totalFrames) {
        super(level, x, y, z, 0.0, 0.0, 0.0);

        this.spriteSet = sprites;
        this.totalFrames = totalFrames;

        this.rCol = r;
        this.gCol = g;
        this.bCol = b;

        var rand = level.getRandom();
        this.xd = (rand.nextDouble() - 0.5) * 0.012;
        this.yd = 0.008 + rand.nextDouble() * 0.010;
        this.zd = (rand.nextDouble() - 0.5) * 0.012;

        this.friction = 0.96f;
        this.lifetime = 40 + rand.nextInt(20);
        this.quadSize = 0.25f;

        this.setSprite(sprites.get(0, totalFrames - 1));
    }

    @Override
    public void tick() {
        this.xd += (this.random.nextDouble() - 0.5) * 0.002;
        this.zd += (this.random.nextDouble() - 0.5) * 0.002;

        super.tick();

        int popStart = this.lifetime - POP_DURATION;
        if (this.age < popStart) {
            this.setSprite(this.spriteSet.get(0, totalFrames - 1));
        } else {
            int popAge = this.age - popStart;
            int firstPopFrame = totalFrames - POP_FRAMES;
            int frameIndex = firstPopFrame + (popAge * POP_FRAMES / POP_DURATION);
            int clampedIndex = Math.min(frameIndex, totalFrames - 1);
            this.setSprite(this.spriteSet.get(clampedIndex, totalFrames - 1));
        }
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

            return new CauldronBubbleParticle(level, x, y, z, r, g, b, sprites, 5);
        }
    }
}