package net.zharok01.coralinesystems.mixin.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.phys.AABB;
import net.zharok01.coralinesystems.entity.GiantAttackCooldownAccess;
import net.zharok01.coralinesystems.entity.GiantJammingAccess;
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
    private boolean coraline$isJumping = false;

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void onAiStep(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!(entity instanceof Giant giant)) return;

        if (giant.level().isClientSide()) return;

        GiantAttackCooldownAccess attackCooldown = (GiantAttackCooldownAccess) giant;
        attackCooldown.coraline$tickAttackCooldown();

        GiantJammingAccess jamming = (GiantJammingAccess) giant;
        jamming.coraline$tickMusicJamming();

        LivingEntity target = giant.getTarget();

        if (!jamming.coraline$isJamming() && !this.coraline$isJumping && target != null && target.isAlive()
                && giant.onGround() && !attackCooldown.coraline$hasAttackCooldown()) {
            double distance = giant.distanceToSqr(target);

            if (distance < 100.0D) {
                giant.setDeltaMovement(giant.getDeltaMovement().add(0, 1.2D, 0));
                this.coraline$isJumping = true;
            }
        }

        if (this.coraline$isJumping) {
            giant.fallDistance = 0.0F;

            if (giant.onGround() && giant.getDeltaMovement().y <= 0) {
                this.coraline$isJumping = false;
                coraline$createShockwave(giant);
                attackCooldown.coraline$startStompCooldown();
            }
        }
    }

    @Unique
    private void coraline$createShockwave(Giant giant) {
        giant.playSound(CoralineSounds.GIANT_STOMP.get(), 1.7F, 1.0F);

        if (giant.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, giant.getX(), giant.getY(), giant.getZ(), 10, 2.0D, 0.5D, 2.0D, 0.0D);
        }

        AABB area = giant.getBoundingBox().inflate(8.0D, 4.0D, 8.0D);
        List<LivingEntity> entities = giant.level().getEntitiesOfClass(LivingEntity.class, area, e -> e != giant && e.isAlive());

        for (LivingEntity targetEntity : entities) {
            double dx = targetEntity.getX() - giant.getX();
            double dz = targetEntity.getZ() - giant.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);

            if (distance > 0) {
                dx /= distance;
                dz /= distance;
            }

            double knockbackStrength = 2.5D * (1.0D - (distance / 8.0D));

            if (knockbackStrength > 0) {
                targetEntity.setDeltaMovement(targetEntity.getDeltaMovement().add(dx * knockbackStrength, 0.9D, dz * knockbackStrength));

                targetEntity.hurt(giant.damageSources().mobAttack(giant), 5.0F);
                targetEntity.hurtMarked = true;
            }
        }
    }
}