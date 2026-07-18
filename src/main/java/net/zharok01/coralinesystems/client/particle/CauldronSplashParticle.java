package net.zharok01.coralinesystems.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.zharok01.coralinesystems.registry.CoralineParticles;

/**
 * A short-lived, tinted water-drop splash particle for the Brewing Cauldron's
 * entity-inside effect.
 *
 * Color encoding: the packet passes r/g/b (0-1 floats) through the
 * xSpeed/ySpeed/zSpeed slots of Level#addParticle.  The Provider reads
 * them and calls setColor() before the first tick, so the particle
 * renders with the correct fluid tint from frame 1.  This is the same
 * pattern used by vanilla's DustParticleOptions and Amendments'
 * BoilingParticle.Provider — no extra fields or NBT needed.
 *
 * Physics: a slight upward impulse + drag, fades out over ~10-16 ticks.
 */
@OnlyIn(Dist.CLIENT)
public class CauldronSplashParticle extends TextureSheetParticle {

    /**
     * Convenience reference so CauldronSplashPacket can call
     * {@code level.addParticle(CauldronSplashParticle.TYPE, ...)}
     * without an import of CoralineParticles.
     */
    public static SimpleParticleType TYPE;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * @param r red   component 0-1  (packed through xSpeed by the Provider)
     * @param g green component 0-1
     * @param b blue  component 0-1
     * @param vx actual X velocity (computed by the Provider from a rand)
     * @param vy actual Y velocity
     * @param vz actual Z velocity
     */
    private CauldronSplashParticle(
            ClientLevel level,
            double x, double y, double z,
            float r, float g, float b,
            double vx, double vy, double vz,
            SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz);

        // Apply the tint color supplied by the packet.
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;

        // Physical velocity: slight upward burst, random horizontal scatter.
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;

        // Drag so particles decelerate naturally.
        this.friction = 0.85f;

        // Short lifetime: 10-16 ticks.
        this.lifetime = 10 + level.getRandom().nextInt(7);

        // Size: a little smaller than a normal splash.
        this.quadSize = 0.08f + level.getRandom().nextFloat() * 0.04f;

        // Pick the first sprite in the set and let it age through the sheet.
        this.setSpriteFromAge(sprites);
    }

    // ── TextureSheetParticle overrides ───────────────────────────────────────

    @Override
    public void tick() {
        super.tick();
        // Update sprite to match current age (animates through the sprite sheet).
        // We use setSpriteFromAge so the particle fades through its frames.
    }

    @Override
    public ParticleRenderType getRenderType() {
        // PARTICLE_SHEET_TRANSLUCENT gives us alpha-blended rendering so the
        // tinted drop looks like a water splash rather than a solid blob.
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
         * Vanilla calls this with the values passed to Level#addParticle.
         * CauldronSplashPacket packs the tint color into xSpeed/ySpeed/zSpeed
         * (i.e. the last three doubles in the call), so we read color from
         * there and generate true physics velocity from randomness here.
         *
         * @param xSpeed packed red   (0-1)
         * @param ySpeed packed green (0-1)
         * @param zSpeed packed blue  (0-1)
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

            // Generate actual physics velocity: upward burst + horizontal scatter.
            var rand = level.getRandom();
            double vx = (rand.nextDouble() - 0.5) * 0.15;
            double vy = 0.1 + rand.nextDouble() * 0.15;
            double vz = (rand.nextDouble() - 0.5) * 0.15;

            var particle = new CauldronSplashParticle(
                    level, x, y, z,
                    r, g, b,
                    vx, vy, vz,
                    sprites);
            particle.setSpriteFromAge(sprites);
            return particle;
        }
    }
}
