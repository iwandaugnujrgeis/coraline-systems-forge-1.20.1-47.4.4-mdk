package net.zharok01.coralinesystems.entity;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.zharok01.coralinesystems.registry.CoralineEffects;
import net.zharok01.coralinesystems.registry.IsotopicEntities;

import java.util.List;

public class OrbPulseEntity extends SmallFireball {

    // How long Haphazard lasts (in ticks). 5 seconds = 100 ticks.
    private static final int HAPHAZARD_DURATION_TICKS = 100;

    // The splash radius around the impact point, in blocks.
    private static final double SPLASH_RADIUS = 2.0D;

    public OrbPulseEntity(EntityType<? extends OrbPulseEntity> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * Constructor called by OrbEntity when shooting.
     * Mirrors the SmallFireball(Level, LivingEntity, double, double, double) pattern
     * but uses our registered entity type instead of EntityType.SMALL_FIREBALL.
     */
    public OrbPulseEntity(Level level, LivingEntity shooter,
                          double offsetX, double offsetY, double offsetZ) {
        // Call Projectile's base constructor with our registered type, then set owner.
        // We replicate what Fireball's LivingEntity constructor does manually here
        // because SmallFireball hard-codes EntityType.SMALL_FIREBALL.
        super(IsotopicEntities.ORB_PULSE.get(), level);
        this.setOwner(shooter);

        // Position the pulse at the shooter's eye level, offset slightly forward.
        this.setPos(
                shooter.getX() + offsetX,
                shooter.getEyeY() + offsetY,
                shooter.getZ() + offsetZ
        );

        // xPower / yPower / zPower are the direction fields on AbstractHurtingProjectile.
        // They drive movement each tick. We normalise them here.
        double length = Math.sqrt(offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ);
        this.xPower = (offsetX / length) * 0.2D;
        this.yPower = (offsetY / length) * 0.2D;
        this.zPower = (offsetZ / length) * 0.2D;
    }

    // -----------------------------------------------------------------------
    // Hit handling
    // -----------------------------------------------------------------------

    /**
     * Direct entity hit — apply splash effect centred on the struck entity.
     */
    @Override
    protected void onHitEntity(EntityHitResult result) {
        // Do NOT call super — SmallFireball.onHitEntity sets fire and deals
        // damage, which we absolutely do not want for the Pulse.
        if (!this.level().isClientSide) {
            applySplashEffect(result.getLocation());
        }
    }

    /**
     * Block hit — apply splash effect centred on the impact point.
     */
    @Override
    protected void onHitBlock(BlockHitResult result) {
        // Do NOT call super — SmallFireball.onHitBlock places fire blocks.
        if (!this.level().isClientSide) {
            applySplashEffect(result.getLocation());
        }
    }

    /**
     * Called for any hit type. Discards the projectile after impact,
     * matching SmallFireball's own onHit behaviour.
     */
    @Override
    protected void onHit(HitResult result) {
        super.onHit(result); // Routes to onHitEntity / onHitBlock above.
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    /**
     * Scans a 2-block AABB around the impact point and applies Haphazard
     * to every Player found within range.
     */
    private void applySplashEffect(net.minecraft.world.phys.Vec3 impactPos) {
        AABB splashBox = new AABB(
                impactPos.x - SPLASH_RADIUS, impactPos.y - SPLASH_RADIUS, impactPos.z - SPLASH_RADIUS,
                impactPos.x + SPLASH_RADIUS, impactPos.y + SPLASH_RADIUS, impactPos.z + SPLASH_RADIUS
        );

        List<Player> nearbyPlayers = this.level().getEntitiesOfClass(Player.class, splashBox);

        for (Player player : nearbyPlayers) {
            // Only apply if this player is actually within the sphere (AABB is a rough
            // over-approximation — distanceTo gives us the precise circle check).
            if (player.distanceToSqr(impactPos.x, impactPos.y, impactPos.z)
                    <= SPLASH_RADIUS * SPLASH_RADIUS) {
                player.addEffect(new MobEffectInstance(
                        CoralineEffects.HAPHAZARD.get(),
                        HAPHAZARD_DURATION_TICKS,
                        0,       // amplifier 0 — we only need one tier
                        false,   // not ambient (no reduced particles)
                        true     // visible particles
                ));
            }
        }
    }

    // -----------------------------------------------------------------------
    // Safety overrides (matching SmallFireball)
    // -----------------------------------------------------------------------

    /** The Pulse cannot be deflected by hitting it. */
    @Override
    public boolean isPickable() {
        return false;
    }

    /** The Pulse takes no damage and cannot be reflected. */
    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        return false;
    }
}