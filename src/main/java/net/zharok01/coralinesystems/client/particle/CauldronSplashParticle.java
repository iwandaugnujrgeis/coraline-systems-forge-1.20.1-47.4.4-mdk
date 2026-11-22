package net.zharok01.coralinesystems.client.particle;

import einstein.subtle_effects.init.ModConfigs;
import einstein.subtle_effects.init.ModParticles;
import einstein.subtle_effects.init.ModSounds;
import einstein.subtle_effects.particle.option.FloatParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class CauldronSplashParticle extends TextureSheetParticle {

    public static SimpleParticleType TYPE;

    private boolean dripIntoFluidEffectsPlayed = false;

    private CauldronSplashParticle(
            ClientLevel level,
            double x, double y, double z,
            float r, float g, float b,
            double vx, double vy, double vz,
            SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz);

        this.rCol = r;
        this.gCol = g;
        this.bCol = b;

        this.gravity = 0.06f;
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;

        this.lifetime = 20 + level.getRandom().nextInt(21);
        this.quadSize = 0.10f + level.getRandom().nextFloat() * 0.08f;

        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.yd -= this.gravity;
        this.move(this.xd, this.yd, this.zd);

        if (!this.removed) {
            BlockPos currentPos = BlockPos.containing(this.x, this.y, this.z);
            FluidState fluidState = this.level.getFluidState(currentPos);
            double fluidHeight = (double) currentPos.getY()
                    + fluidState.getHeight(this.level, currentPos);

            if (!fluidState.isEmpty() && this.y <= fluidHeight) {
                if (!this.dripIntoFluidEffectsPlayed) {
                    dripIntoFluidEffectsPlayed = true;

                    this.level.playLocalSound(
                            this.x, this.y, this.z,
                            ModSounds.DRIP_WATER.get(),
                            SoundSource.BLOCKS,
                            Mth.clamp(this.random.nextFloat(), 0.3f, 1.0f),
                            1.0f,
                            false);

                    if (ModConfigs.GENERAL.dropLandInFluidRipples) {
                        float rippleScale = Math.max(this.quadSize, Math.max(this.bbWidth, this.bbHeight)) + 0.90f;
                        this.level.addParticle(
                                new FloatParticleOptions(
                                        ModParticles.WATER_RIPPLE.get(),
                                        rippleScale),
                                this.x, fluidHeight, this.z,
                                0.0, 0.0, 0.0);
                    }
                }
                this.remove();
                return;
            }

            if (this.onGround) {
                this.level.playLocalSound(
                        this.x, this.y, this.z,
                        ModSounds.DRIP_WATER.get(),
                        SoundSource.BLOCKS,
                        Mth.clamp(this.random.nextFloat(), 0.3f, 1.0f),
                        1.0f,
                        false);
                this.remove();
                return;
            }

            this.xd *= 0.98f;
            this.yd *= 0.98f;
            this.zd *= 0.98f;

            if (this.age++ >= this.lifetime) {
                this.remove();
            }
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
                ClientLevel level,
                double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {

            float r = (float) xSpeed;
            float g = (float) ySpeed;
            float b = (float) zSpeed;

            var rand = level.getRandom();
            double vx = (rand.nextDouble() - 0.5) * 0.3;
            double vy = 0.1 + rand.nextDouble() * 0.2;
            double vz = (rand.nextDouble() - 0.5) * 0.3;

            return new CauldronSplashParticle(level, x, y, z, r, g, b, vx, vy, vz, sprites);
        }
    }
}