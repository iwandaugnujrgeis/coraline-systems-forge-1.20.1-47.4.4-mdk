package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LivingEntity.class)
public abstract class GiantStompMixin {

    @Unique
    private int coraline_systems$stompCooldown = 0;

    @Unique
    private boolean coraline_systems$isJumping = false;

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void onAiStep(CallbackInfo ci) {
        // MAGIC CHECK: If this LivingEntity is NOT a Giant, do absolutely nothing!
        if (!((Object) this instanceof Giant giant)) return;

        if (giant.level().isClientSide()) return;

        if (this.coraline_systems$stompCooldown > 0) {
            this.coraline_systems$stompCooldown--;
        }

        LivingEntity target = giant.getTarget();

        // Initiation: Target is alive, Giant is on the ground, and cooldown is ready
        if (target != null && target.isAlive() && giant.onGround() && this.coraline_systems$stompCooldown <= 0) {
            double distance = giant.distanceToSqr(target);

            // If player is within ~10 blocks (100 squared)
            if (distance < 100.0D) {
                // Launch the giant upward!
                giant.setDeltaMovement(giant.getDeltaMovement().add(0, 1.2D, 0));
                this.coraline_systems$isJumping = true;
                this.coraline_systems$stompCooldown = 100; // 5 seconds before the next stomp
            }
        }

        // Landing: Was jumping, is now on the ground, and is falling (y <= 0)
        if (this.coraline_systems$isJumping && giant.onGround() && giant.getDeltaMovement().y <= 0) {
            this.coraline_systems$isJumping = false;
            coraline_systems$createShockwave(giant);
        }
    }

    @Unique
    private void coraline_systems$createShockwave(Giant giant) {
        // Play a massive booming sound and spawn explosion/smoke particles
        giant.playSound(SoundEvents.GENERIC_EXPLODE, 4.0F, 0.5F);

        if (giant.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, giant.getX(), giant.getY(), giant.getZ(), 10, 2.0D, 0.5D, 2.0D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, giant.getX(), giant.getY(), giant.getZ(), 40, 3.0D, 1.0D, 3.0D, 0.1D);
        }

        // Define the blast radius (8 blocks away from the giant's massive hitbox)
        AABB area = giant.getBoundingBox().inflate(8.0D, 4.0D, 8.0D);
        List<LivingEntity> entities = giant.level().getEntitiesOfClass(LivingEntity.class, area, e -> e != giant && e.isAlive());

        for (LivingEntity entity : entities) {
            // Calculate direction pointing away from the giant
            double dx = entity.getX() - giant.getX();
            double dz = entity.getZ() - giant.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);

            if (distance > 0) {
                dx /= distance;
                dz /= distance;
            }

            // Calculate knockback (stronger the closer you are to the giant)
            double knockbackStrength = 2.5D * (1.0D - (distance / 12.0D));

            if (knockbackStrength > 0) {
                // Apply the velocity: Push away on X/Z, and throw them UP into the air
                entity.setDeltaMovement(entity.getDeltaMovement().add(dx * knockbackStrength, 0.9D, dz * knockbackStrength));

                // Deal some heavy damage for getting crushed
                entity.hurt(giant.damageSources().mobAttack(giant), 15.0F);

                // CRITICAL: This tells the server to update the player's client with the new velocity!
                entity.hurtMarked = true;
            }
        }
    }
}