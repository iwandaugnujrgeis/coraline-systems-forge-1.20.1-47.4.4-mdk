package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.phys.AABB;
import net.zharok01.coralinesystems.registry.CoralineSounds;
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
        if (!((Object) this instanceof Giant giant)) return;

        if (giant.level().isClientSide()) return;

        if (this.coraline_systems$stompCooldown > 0) {
            this.coraline_systems$stompCooldown--;
        }

        LivingEntity target = giant.getTarget();

        if (target != null && target.isAlive() && giant.onGround() && this.coraline_systems$stompCooldown <= 0) {
            double distance = giant.distanceToSqr(target);

            if (distance < 100.0D) {
                giant.setDeltaMovement(giant.getDeltaMovement().add(0, 1.2D, 0));
                this.coraline_systems$isJumping = true;
                this.coraline_systems$stompCooldown = 200;
            }
        }

        // PREVENT FALL DAMAGE: If the giant is jumping, reset its fall distance every tick
        if (this.coraline_systems$isJumping) {
            giant.fallDistance = 0.0F;

            // Landing detection
            if (giant.onGround() && giant.getDeltaMovement().y <= 0) {
                this.coraline_systems$isJumping = false;
                coraline_systems$createShockwave(giant);
            }
        }
    }

    @Unique
    private void coraline_systems$createShockwave(Giant giant) {
        giant.playSound(CoralineSounds.GIANT_STOMP.get(), 1.7F, 1.0F);

        if (giant.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, giant.getX(), giant.getY(), giant.getZ(), 10, 2.0D, 0.5D, 2.0D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, giant.getX(), giant.getY(), giant.getZ(), 40, 3.0D, 1.0D, 3.0D, 0.1D);
        }

        AABB area = giant.getBoundingBox().inflate(8.0D, 4.0D, 8.0D);
        List<LivingEntity> entities = giant.level().getEntitiesOfClass(LivingEntity.class, area, e -> e != giant && e.isAlive());

        for (LivingEntity entity : entities) {
            double dx = entity.getX() - giant.getX();
            double dz = entity.getZ() - giant.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);

            if (distance > 0) {
                dx /= distance;
                dz /= distance;
            }

            double knockbackStrength = 2.5D * (1.0D - (distance / 12.0D));

            if (knockbackStrength > 0) {
                entity.setDeltaMovement(entity.getDeltaMovement().add(dx * knockbackStrength, 0.9D, dz * knockbackStrength));
                entity.hurt(giant.damageSources().mobAttack(giant), 10.0F);
                entity.hurtMarked = true;
            }
        }
    }
}