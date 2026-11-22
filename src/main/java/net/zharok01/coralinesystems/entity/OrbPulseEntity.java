package net.zharok01.coralinesystems.entity;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.registry.CoralineEffects;
import net.zharok01.coralinesystems.registry.CoralineParticles;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import net.zharok01.coralinesystems.registry.IsotopicEntities;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OrbPulseEntity extends SmallFireball {

    private static final int HAPHAZARD_DURATION_TICKS = 100;
    private static final double SPLASH_RADIUS = 5.0D;

    public OrbPulseEntity(EntityType<? extends OrbPulseEntity> entityType, Level level) {
        super(entityType, level);
    }

    public OrbPulseEntity(Level level, LivingEntity shooter,
                          double dirX, double dirY, double dirZ) {
        super(IsotopicEntities.ORB_PULSE.get(), level);
        this.setOwner(shooter);

        this.setPos(
                shooter.getX(),
                shooter.getY(0.5D),
                shooter.getZ()
        );

        double length = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        if (length != 0.0D) {
            this.xPower = (dirX / length) * 0.2D;
            this.yPower = (dirY / length) * 0.2D;
            this.zPower = (dirZ / length) * 0.2D;
        }
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected @NotNull ParticleOptions getTrailParticle() {
        return CoralineParticles.ORB_SPARKLE.get();
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        if (!this.level().isClientSide) {
            applySplashEffect(result.getLocation());
            spawnImpactParticles(result.getLocation());
        }
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        if (!this.level().isClientSide) {
            applySplashEffect(result.getLocation());
            spawnImpactParticles(result.getLocation());
        }
    }

    @Override
    protected void onHit(@NotNull HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide) {
            Vec3 impactPos = hitResult.getLocation();
            this.level().playSound(null, impactPos.x, impactPos.y, impactPos.z,
                    CoralineSounds.ORB_PULSE_IMPACT.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
            this.spawnImpactParticles(impactPos);
            this.discard();
        }
    }

    private void applySplashEffect(Vec3 impactPos) {
        AABB splashBox = new AABB(
                impactPos.x - SPLASH_RADIUS, impactPos.y - SPLASH_RADIUS, impactPos.z - SPLASH_RADIUS,
                impactPos.x + SPLASH_RADIUS, impactPos.y + SPLASH_RADIUS, impactPos.z + SPLASH_RADIUS
        );
        List<Player> nearbyPlayers = this.level().getEntitiesOfClass(Player.class, splashBox);
        for (Player player : nearbyPlayers) {
            if (player.distanceToSqr(impactPos.x, impactPos.y, impactPos.z) <= SPLASH_RADIUS * SPLASH_RADIUS) {
                player.addEffect(new MobEffectInstance(
                        CoralineEffects.HAPHAZARD.get(),
                        HAPHAZARD_DURATION_TICKS,
                        0,
                        false,
                        true
                ));
            }
        }
    }

    private void spawnImpactParticles(Vec3 pos) {
        net.minecraft.server.level.ServerLevel serverLevel =
                (net.minecraft.server.level.ServerLevel) this.level();
        serverLevel.sendParticles(
                CoralineParticles.ORB_SPARKLE.get(),
                pos.x, pos.y, pos.z,
                30,
                0.6D, 0.6D, 0.6D,
                0.15D
        );
        serverLevel.sendParticles(
                ParticleTypes.CLOUD,
                pos.x, pos.y, pos.z,
                16,
                0.3D, 0.3D, 0.3D,
                0.05D
        );
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.@NotNull DamageSource source, float amount) {
        return false;
    }
}