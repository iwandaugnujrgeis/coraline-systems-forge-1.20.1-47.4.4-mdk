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

/**
 * Soft pinkish-white spark that drifts downward from Orbs and Orb Pulses.
 *
 * <p>Rendered using the PARTICLE_SHEET_LIT render type so it always appears
 * at minimum brightness — the same trick used by SparkleParticle — giving the
 * sparks a glowing appearance regardless of the surrounding light level.</p>
 *
 * <p>Texture: assets/coraline_systems/textures/particle/orb_sparkle.png
 * JSON:    assets/coraline_systems/particles/orb_sparkle.json</p>
 */
@OnlyIn(Dist.CLIENT)
public class OrbSparkleParticle extends TextureSheetParticle {

    private final SpriteSet sprites;

    protected OrbSparkleParticle(ClientLevel level,
                                 double x, double y, double z,
                                 double xd, double yd, double zd,
                                 SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;

        // Velocity — caller typically passes a tiny upward nudge so gravity arcs
        // the sparks back down, giving the "drizzle" feel.
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;

        // Size — small enough to look like embers, not blobs
        this.quadSize = 0.08f + this.random.nextFloat() * 0.04f;

        // Pinkish-white tint
        this.rCol = 1.0f;
        this.gCol = 0.75f + this.random.nextFloat() * 0.15f;
        this.bCol = 0.85f + this.random.nextFloat() * 0.15f;

        // Short lifetime so individual sparks don't linger
        this.lifetime = this.random.nextInt(12) + 6;

        // Gravity pulls sparks gently downward — matches the "raining" visual
        this.gravity = 0.06f;

        this.setSpriteFromAge(sprites);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        // LIT variant ensures sparks glow at full brightness even in dark areas
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    /**
     * Clamp the block light component to at least 11 so sparks always
     * appear bright, matching the glow aesthetic of the Orb.
     */
    @Override
    protected int getLightColor(float partialTick) {
        int packed = super.getLightColor(partialTick);
        int block = LightTexture.block(packed);
        int sky   = LightTexture.sky(packed);
        return LightTexture.pack(Math.max(block, 11), sky);
    }

    @Override
    public void tick() {
        super.tick();
        // Animate through the sprite sheet frames as the particle ages
        this.setSpriteFromAge(sprites);
    }

    // ------------------------------------------------------------------
    // Factory — registered in CoralineClientModEvents via RegisterParticleProvidersEvent
    // ------------------------------------------------------------------

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type,
                                       @NotNull ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new OrbSparkleParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}