package net.zharok01.coralinesystems.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A tinted 1-pixel fizz dot for finished Kombucha's idle ambient effect.
 *
 * Visually distinct from {@link CauldronBubbleParticle}:
 *  - Much smaller (approaches a single pixel on screen).
 *  - Wobbly horizontal drift via a sine wave keyed off age, giving it a
 *    light carbonated "fizz" feel rather than a steady rise.
 *  - Shorter lifetime and faster upward speed than the brewing bubbles.
 *  - Rendered translucent so multiple overlapping dots blend naturally.
 *
 * Color encoding: same r/g/b-in-velocity-slots convention as
 * {@link CauldronBubbleParticle} and {@link CauldronSplashParticle}.
 * The Provider reads xSpeed/ySpeed/zSpeed as float RGB (0-1) and passes
 * them to the constructor; actual physics are generated internally.
 *
 * The wobble axis (X vs Z) is randomised per-particle so a cluster of
 * fizz dots doesn't all swing in the same direction.
 */
@OnlyIn(Dist.CLIENT)
public class CauldronFizzParticle extends TextureSheetParticle {

    public static SimpleParticleType TYPE;

    // Wobble parameters — computed once in the constructor.
    // The particle oscillates on either X or Z (chosen randomly) using:
    //   offset = wobbleAmplitude * sin(age * wobbleFrequency + wobblePhase)
    // applied as a delta to xd or zd each tick, keeping yd unaffected.
    private final float wobbleAmplitude;
    private final float wobbleFrequency;
    private final float wobblePhase;
    private final boolean wobbleOnX; // true = wobble X, false = wobble Z

    private final SpriteSet spriteSet;

    // ── Constructor ──────────────────────────────────────────────────────────

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

        // Upward drift — faster and more direct than the brewing bubble.
        this.xd = (rand.nextDouble() - 0.5) * 0.008;
        this.yd = 0.04 + rand.nextDouble() * 0.03;
        this.zd = (rand.nextDouble() - 0.5) * 0.008;

        // Very light drag — fizz dots should float freely.
        this.friction = 0.96f;

        // Short lifetime: gone before it travels far above the surface.
        this.lifetime = 14 + rand.nextInt(10);

        // 1-pixel scale: as small as the engine meaningfully supports.
        this.quadSize = 0.10f + rand.nextFloat() * 0.01f;

        // Wobble parameters — randomised per particle.
        this.wobbleAmplitude = 0.004f + rand.nextFloat() * 0.004f;
        this.wobbleFrequency = 0.5f  + rand.nextFloat() * 0.4f;
        this.wobblePhase     = rand.nextFloat() * (float) (Math.PI * 2);
        this.wobbleOnX       = rand.nextBoolean();

        this.setSpriteFromAge(sprites);
    }

    // ── TextureSheetParticle overrides ───────────────────────────────────────

    @Override
    public void tick() {
        // Apply sine-wave wobble to the chosen horizontal axis.
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
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    // ── Provider ─────────────────────────────────────────────────────────────

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        /**
         * @param xSpeed packed red   component 0-1
         * @param ySpeed packed green component 0-1
         * @param zSpeed packed blue  component 0-1
         */
        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {

            float r = (float) xSpeed;
            float g = (float) ySpeed;
            float b = (float) zSpeed;

            return new CauldronFizzParticle(level, x, y, z, r, g, b, sprites);
        }
    }
}