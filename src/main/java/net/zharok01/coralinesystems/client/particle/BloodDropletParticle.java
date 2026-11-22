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
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class BloodDropletParticle extends TextureSheetParticle {

    private boolean fluidEffectsPlayed = false;
    private boolean hasLanded = false;
    private final SpriteSet sprites;

    private static final int TICKS_PER_INITIAL_FRAME = 2;

    private BloodDropletParticle(
            ClientLevel level,
            double x, double y, double z,
            double vx, double vy, double vz,
            SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz);
        this.sprites = sprites;

        this.gravity = 0.09f;
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;

        this.lifetime = 100 + level.getRandom().nextInt(60);
        this.quadSize = 0.16f + level.getRandom().nextFloat() * 0.04f;

        this.updateSprite();
    }

    private void updateSprite() {
        int maxFrameIndex = 3;
        int currentFrame = Math.min(this.age / TICKS_PER_INITIAL_FRAME, maxFrameIndex);

        this.setSprite(this.sprites.get(currentFrame, maxFrameIndex));
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.updateSprite();

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        this.yd -= this.gravity;
        this.move(this.xd, this.yd, this.zd);

        if (!this.removed) {
            BlockPos currentPos = BlockPos.containing(this.x, this.y, this.z);
            FluidState fluidState = this.level.getFluidState(currentPos);
            double fluidHeight = (double) currentPos.getY() + fluidState.getHeight(this.level, currentPos);

            if (!fluidState.isEmpty() && this.y <= fluidHeight) {
                if (!this.fluidEffectsPlayed) {
                    this.fluidEffectsPlayed = true;

                    this.level.playLocalSound(
                            this.x, this.y, this.z,
                            CoralineSounds.DRIP_BLOOD.get(),
                            SoundSource.BLOCKS,
                            0.5f,
                            0.8f + this.random.nextFloat() * 0.4f,
                            false);

                    if (ModConfigs.GENERAL.dropLandInFluidRipples) {
                        float rippleScale = Math.max(this.quadSize, Math.max(this.bbWidth, this.bbHeight)) + 0.50f;
                        this.level.addParticle(
                                new FloatParticleOptions(ModParticles.WATER_RIPPLE.get(), rippleScale),
                                this.x, fluidHeight, this.z,
                                0.0, 0.0, 0.0);
                    }
                }
                this.remove();
                return;
            }

            if (this.onGround) {
                if (!this.hasLanded) {
                    this.hasLanded = true;

                    this.level.playLocalSound(
                            this.x, this.y, this.z,
                            ModSounds.DRIP_WATER.get(),
                            SoundSource.BLOCKS,
                            0.3f,
                            0.8f + this.random.nextFloat() * 0.4f,
                            false);
                }

                this.xd = 0.0;
                this.zd = 0.0;

            } else {
                this.xd *= 0.95f;
                this.yd *= 0.95f;
                this.zd *= 0.95f;
            }
        }
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
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

            var rand = level.getRandom();

            double vx = (rand.nextDouble() - 0.5) * 0.4;
            double vy = 0.05 + rand.nextDouble() * 0.15;
            double vz = (rand.nextDouble() - 0.5) * 0.4;

            return new BloodDropletParticle(level, x, y, z, vx, vy, vz, sprites);
        }
    }
}