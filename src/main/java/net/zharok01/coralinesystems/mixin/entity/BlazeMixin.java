package net.zharok01.coralinesystems.mixin.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.mixin.accessor.BlazeAccessor;
import net.zharok01.coralinesystems.registry.CoralineParticles;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.EnumSet;

@Mixin(Blaze.class)
public abstract class BlazeMixin extends Monster {

    protected BlazeMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "getAmbientSound", at = @At("HEAD"), cancellable = true)
    private void coraline$replaceSong(CallbackInfoReturnable<SoundEvent> cir) {
        cir.setReturnValue(CoralineSounds.BLAZE_SONG.get());
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        boolean wasHurt = super.hurt(source, amount);

        if (wasHurt && !this.level().isClientSide && this.getHealth() > 0.0F) {
            double boost = 1.4D + (this.random.nextDouble() * 0.2D);
            Vec3 vec3 = this.getDeltaMovement();
            this.setDeltaMovement(vec3.x, boost, vec3.z);
            this.hasImpulse = true;
        }

        return wasHurt;
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void coraline$spawnCyanSparkles(CallbackInfo ci) {
        if (!this.level().isClientSide) return;
        if (this.random.nextInt(4) != 0) return;
        int count = 1 + this.random.nextInt(2);
        for (int i = 0; i < count; i++) {
            double ox = (this.random.nextDouble() - 0.5D) * 0.6D;
            double oy = this.random.nextDouble() * 1.2D;
            double oz = (this.random.nextDouble() - 0.5D) * 0.6D;
            this.level().addParticle(
                    CoralineParticles.BLAZE_SPARKLE.get(),
                    this.getX() + ox, this.getY() + oy, this.getZ() + oz,
                    0.0D, 0.02D, 0.0D
            );
        }
    }

    @Unique private static final double PLAYER_TARGET_RANGE = 24.0D;

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void coraline$replaceGoals(CallbackInfo ci) {
        this.goalSelector.getAvailableGoals().removeIf(w -> w.getPriority() == 4);
        this.targetSelector.getAvailableGoals().removeIf(w -> w.getPriority() == 2);

        this.goalSelector.addGoal(4, new CoralineBlazeAttackGoal((Blaze) (Object) this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                this, Player.class, 10, true, false,
                entity -> entity.distanceToSqr(this) <= PLAYER_TARGET_RANGE * PLAYER_TARGET_RANGE
        ));
    }

    @Redirect(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"
            ),
            require = 1,
            allow = 1
    )
    private void coraline$suppressSmokeParticles(Level instance, net.minecraft.core.particles.ParticleOptions particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
    }

    @Unique private static final int STRAFE_INTERVAL = 12;
    @Unique private static final float STRAFE_STRENGTH = 0.22F;
    @Unique private static final float BURST_CHANCE = 0.18F;

    @Inject(method = "customServerAiStep", at = @At("TAIL"))
    private void coraline$zigZagStrafe(CallbackInfo ci) {
        LivingEntity target = this.getTarget();
        if (target == null) return;
        if (this.tickCount % STRAFE_INTERVAL != 0) return;

        double angle = this.random.nextDouble() * Math.PI * 2.0D;
        double strafeX = Math.cos(angle) * STRAFE_STRENGTH;
        double strafeZ = Math.sin(angle) * STRAFE_STRENGTH;

        if (this.random.nextFloat() < BURST_CHANCE) {
            Vec3 toTarget = target.position().subtract(this.position()).normalize();
            double burst = this.random.nextBoolean() ? -0.3D : 0.25D;
            strafeX += toTarget.x * burst;
            strafeZ += toTarget.z * burst;
        }

        this.setDeltaMovement(this.getDeltaMovement().add(strafeX, 0.0D, strafeZ));
    }

    @Unique
    private static final class CoralineBlazeAttackGoal extends Goal {

        private static final int CHARGE_DURATION = 25;
        private static final int SHOTS_PER_VOLLEY = 8;
        private static final int TICKS_BETWEEN_SHOTS = 3;
        private static final int TICKS_AFTER_VOLLEY = 60;

        private final Blaze blaze;
        private final BlazeAccessor accessor;
        private int attackStep;
        private int attackTime;
        private int lastSeen;

        CoralineBlazeAttackGoal(Blaze blaze) {
            this.blaze = blaze;
            this.accessor = (BlazeAccessor) blaze;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.blaze.getTarget();
            return target != null && target.isAlive() && this.blaze.canAttack(target);
        }

        @Override
        public void start() {
            this.attackStep = 0;
        }

        @Override
        public void stop() {
            this.accessor.coraline$setCharged(false);
            this.lastSeen = 0;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            --this.attackTime;
            LivingEntity target = this.blaze.getTarget();
            if (target == null) return;

            boolean hasLos = this.blaze.getSensing().hasLineOfSight(target);
            if (hasLos) {
                this.lastSeen = 0;
            } else {
                ++this.lastSeen;
            }

            double distSqr = this.blaze.distanceToSqr(target);

            if (distSqr < 4.0D) {
                if (hasLos && this.attackTime <= 0) {
                    this.attackTime = 20;
                    this.blaze.doHurtTarget(target);
                }
                this.blaze.getMoveControl().setWantedPosition(
                        target.getX(), target.getY(), target.getZ(), 1.0D);

            } else if (distSqr < this.followDistanceSqr() && hasLos) {
                double dX = target.getX() - this.blaze.getX();
                double dY = target.getY(0.5D) - this.blaze.getY(0.5D);
                double dZ = target.getZ() - this.blaze.getZ();

                if (this.attackTime <= 0) {
                    ++this.attackStep;

                    if (this.attackStep == 1) {
                        this.attackTime = CHARGE_DURATION;
                        this.accessor.coraline$setCharged(true);

                    } else if (this.attackStep <= SHOTS_PER_VOLLEY) {
                        this.attackTime = TICKS_BETWEEN_SHOTS;

                    } else {
                        this.attackTime = TICKS_AFTER_VOLLEY;
                        this.attackStep = 0;
                        this.accessor.coraline$setCharged(false);
                    }

                    if (this.attackStep > 1 && this.attackStep <= SHOTS_PER_VOLLEY) {
                        double spread = Math.sqrt(Math.sqrt(distSqr)) * 0.5D;
                        if (!this.blaze.isSilent()) {
                            this.blaze.level().levelEvent(null, 1018, this.blaze.blockPosition(), 0);
                        }
                        SmallFireball fireball = new SmallFireball(
                                this.blaze.level(),
                                this.blaze,
                                this.blaze.getRandom().triangle(dX, 2.297D * spread),
                                dY,
                                this.blaze.getRandom().triangle(dZ, 2.297D * spread)
                        );
                        fireball.setPos(fireball.getX(), this.blaze.getY(0.5D) + 0.5D, fireball.getZ());
                        this.blaze.level().addFreshEntity(fireball);
                    }
                }

                this.blaze.getLookControl().setLookAt(target, 10.0F, 10.0F);

            } else if (this.lastSeen < 5) {
                this.blaze.getMoveControl().setWantedPosition(
                        target.getX(), target.getY(), target.getZ(), 1.0D);
            }

            super.tick();
        }

        private double followDistanceSqr() {
            double range = this.blaze.getAttributeValue(Attributes.FOLLOW_RANGE);
            return range * range;
        }
    }
}