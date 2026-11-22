package net.zharok01.coralinesystems.entity.ai;

import java.util.EnumSet;
import javax.annotation.Nullable;

import einstein.subtle_effects.networking.clientbound.ClientBoundEntitySpawnSprintingDustCloudsPacket;
import einstein.subtle_effects.platform.Services;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import net.zharok01.coralinesystems.registry.CoralineTags;

public class WolfSurvivalPanicGoal extends Goal {

    private static final double WALK_SPEED = 1.0D;
    private static final double SPRINT_SPEED = 1.3D;
    private static final double SPRINT_THRESHOLD = 16.0D;
    private static final double SPRINT_THRESHOLD_SQ = SPRINT_THRESHOLD * SPRINT_THRESHOLD;
    private static final int REPATH_INTERVAL = 20;
    private static final int SPRINT_DUST_INTERVAL = 4;

    private final Wolf wolf;
    private final float healthThreshold;
    private final float safeDistance;
    private final double roamSpeed;

    @Nullable private LivingEntity currentThreat;
    @Nullable private Path currentPath;

    private int howlCooldown;
    private int repathCooldown;
    private int dustCooldown;

    public WolfSurvivalPanicGoal(Wolf wolf, float healthThreshold, float safeDistance, double fleeSpeed, double roamSpeed) {
        this.wolf = wolf;
        this.healthThreshold = healthThreshold;
        this.safeDistance = safeDistance;
        this.roamSpeed = roamSpeed;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.wolf.isTame()) return false;
        if (coraline$shouldPauseForDroppedMeat()) return false;

        if (this.wolf.getHealth() >= this.healthThreshold && !this.wolf.isOnFire()) {
            WolfDistressTracker.clearDistressed(this.wolf.getUUID());
            return false;
        }

        LivingEntity potentialThreat = this.wolf.getTarget();
        if (potentialThreat == null) {
            potentialThreat = this.wolf.getLastHurtByMob();
        }

        if (potentialThreat == null && !WolfDistressTracker.isDistressed(this.wolf.getUUID())) {
            return false;
        }

        this.currentThreat = potentialThreat;
        return tryFindEscapeRoute();
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.wolf.isTame()) return false;

        if (this.wolf.getTarget() != null) {
            this.wolf.setTarget(null);
        }

        if (coraline$shouldPauseForDroppedMeat()) return false;
        return this.wolf.getHealth() < this.healthThreshold || this.wolf.isOnFire();
    }

    private boolean coraline$shouldPauseForDroppedMeat() {
        if (this.wolf.getHealth() >= this.healthThreshold
                || this.wolf.isOrderedToSit()
                || this.wolf.getTarget() != null) {
            return false;
        }

        return !this.wolf.level().getEntitiesOfClass(
                ItemEntity.class,
                this.wolf.getBoundingBox().inflate(10.0D),
                item -> item.getItem().is(CoralineTags.WOLF_MEAT_DROPPED)
        ).isEmpty();
    }

    @Override
    public void start() {
        WolfDistressTracker.setDistressed(this.wolf.getUUID());
        this.wolf.setTarget(null);
        this.howlCooldown = 120;
        this.repathCooldown = REPATH_INTERVAL;
        this.dustCooldown = 0;

        if (this.currentPath != null) {
            double speed = currentSpeed();
            this.wolf.getNavigation().moveTo(this.currentPath, speed);
        }
    }

    @Override
    public void stop() {
        this.wolf.getNavigation().stop();
        this.currentThreat = null;
        this.currentPath = null;

        if (this.wolf.getHealth() >= this.healthThreshold && !this.wolf.isOnFire()) {
            WolfDistressTracker.clearDistressed(this.wolf.getUUID());
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (--this.howlCooldown <= 0) {
            this.wolf.playSound(CoralineSounds.DOG_HOWL.get(), 1.0F, 0.9F + this.wolf.getRandom().nextFloat() * 0.2F);
            this.howlCooldown = 60 + this.wolf.getRandom().nextInt(60);
        }

        if (this.currentThreat != null) {
            if (this.currentThreat.isDeadOrDying()
                    || this.wolf.distanceToSqr(this.currentThreat) > this.safeDistance * this.safeDistance) {
                this.currentThreat = null;
            }
        } else {
            LivingEntity reacquired = this.wolf.getTarget();
            if (reacquired == null) {
                reacquired = this.wolf.getLastHurtByMob();
            }
            if (reacquired != null && reacquired.isAlive()
                    && this.wolf.distanceToSqr(reacquired) <= this.safeDistance * this.safeDistance) {
                this.currentThreat = reacquired;
            }
        }

        boolean pathFinished = this.wolf.getNavigation().isDone() || this.wolf.getNavigation().isStuck();
        boolean cooldownElapsed = --this.repathCooldown <= 0;

        if (pathFinished || cooldownElapsed) {
            this.repathCooldown = REPATH_INTERVAL;

            if (tryFindEscapeRoute() && this.currentPath != null) {
                this.wolf.getNavigation().moveTo(this.currentPath, currentSpeed());
            }
        } else {
            this.wolf.getNavigation().setSpeedModifier(currentSpeed());
        }

        if (currentSpeed() == SPRINT_SPEED) {
            trySpawnSprintDust();
        }
    }

    private double currentSpeed() {
        if (this.currentThreat != null
                && this.wolf.distanceToSqr(this.currentThreat) < SPRINT_THRESHOLD_SQ) {
            return SPRINT_SPEED;
        }
        return this.currentThreat != null ? WALK_SPEED : this.roamSpeed;
    }

    private boolean tryFindEscapeRoute() {
        Vec3 targetPos = null;

        if (this.currentThreat != null) {
            targetPos = DefaultRandomPos.getPosAway(this.wolf, 16, 7, this.currentThreat.position());
        }

        if (targetPos == null) {
            targetPos = DefaultRandomPos.getPos(this.wolf, 10, 7);
        }

        if (targetPos != null) {
            this.currentPath = this.wolf.getNavigation().createPath(targetPos.x, targetPos.y, targetPos.z, 0);
            return this.currentPath != null;
        }

        return false;
    }

    private void trySpawnSprintDust() {
        if (!(this.wolf.level() instanceof ServerLevel serverLevel)) return;
        if (!this.wolf.onGround()) return;

        if (this.dustCooldown > 0) {
            this.dustCooldown--;
            return;
        }
        this.dustCooldown = SPRINT_DUST_INTERVAL;

        Services.NETWORK.sendToClientsTracking(
                serverLevel,
                this.wolf.blockPosition(),
                new ClientBoundEntitySpawnSprintingDustCloudsPacket(this.wolf.getId())
        );
    }
}