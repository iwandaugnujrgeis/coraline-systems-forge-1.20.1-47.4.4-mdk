package net.zharok01.coralinesystems.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A tinted rising bubble particle for the Brewing Cauldron's active-brewing
 * ambient effect.
 *
 * Color encoding: same r/g/b-in-velocity-slots convention as
 * {@link CauldronSplashParticle}. The Provider reads xSpeed/ySpeed/zSpeed as
 * the float RGB components (0-1) and passes them to the constructor.
 * Actual physics velocity is generated internally so the slots are free to
 * carry color data.
 *
 * Physics: slow upward drift with gentle horizontal wobble, short lifetime
 * (~8-14 ticks), fades out by age.  Rendered translucent so multiple
 * overlapping bubbles blend naturally.
 */
@OnlyIn(Dist.CLIENT)
public class CauldronBubbleParticle extends TextureSheetParticle {

    /**
     * Convenience reference populated during {@code FMLClientSetupEvent} so
     * callers (e.g. {@code animateTick}) can reference the type without an
     * import of {@code CoralineParticles}.
     */
    public static SimpleParticleType TYPE;

    // Stored so tick() can animate through the sprite sheet frames.
    private final SpriteSet spriteSet;

    // ── Constructor ──────────────────────────────────────────────────────────

    private CauldronBubbleParticle(
            ClientLevel level,
            double x, double y, double z,
            float r, float g, float b,
            SpriteSet sprites) {
        super(level, x, y, z, 0.0, 0.0, 0.0);

        this.spriteSet = sprites;

        this.rCol = r;
        this.gCol = g;
        this.bCol = b;

        // Slow upward drift — feels like a bubble rising through liquid.
        var rand = level.getRandom();
        this.xd = (rand.nextDouble() - 0.5) * 0.018;
        this.yd = 0.02 + rand.nextDouble() * 0.025;
        this.zd = (rand.nextDouble() - 0.5) * 0.018;

        // Light drag so the bubble decelerates gently near the surface.
        this.friction = 0.92f;

        // Short lifetime: fades before it travels far above the surface.
        this.lifetime = 14 + rand.nextInt(7);

        // Particle size
        this.quadSize = 0.11f + rand.nextFloat() * 0.03f;

        this.setSpriteFromAge(sprites);
    }

    // ── TextureSheetParticle overrides ───────────────────────────────────────

    @Override
    public void tick() {
        super.tick();
        // Animate sprite sheet so the bubble appears to shimmer as it ages.
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

            var particle = new CauldronBubbleParticle(level, x, y, z, r, g, b, sprites);
            particle.setSpriteFromAge(sprites);
            return particle;
        }
    }
}