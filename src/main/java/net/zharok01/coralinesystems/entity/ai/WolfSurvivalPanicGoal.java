package net.zharok01.coralinesystems.entity.ai;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.registry.CoralineSounds;

/**
 * Unified Fight-or-Flight survival goal.
 * Replaces both the old flee and roam goals to prevent stuttering.
 * * If the Wolf's HP drops below a threshold while fighting (or if on fire),
 * it enters a locked panic state. It will continuously path AWAY from the
 * threat until the threat is dead or completely out of tracking range (40+ blocks).
 * If the threat is lost, the Wolf enters a distressed roaming state until healed.
 */
public class WolfSurvivalPanicGoal extends Goal {

    private final Wolf wolf;
    private final float healthThreshold;
    private final float safeDistance;

    private final double fleeSpeed;
    private final double roamSpeed;

    @Nullable
    private LivingEntity currentThreat;
    @Nullable
    private Path currentPath;

    private int howlCooldown;
    private int repathCooldown;

    public WolfSurvivalPanicGoal(Wolf wolf, float healthThreshold, float safeDistance, double fleeSpeed, double roamSpeed) {
        this.wolf = wolf;
        this.healthThreshold = healthThreshold;
        this.safeDistance = safeDistance; // Should be ~40.0F to outrun Zombies
        this.fleeSpeed = fleeSpeed;
        this.roamSpeed = roamSpeed;
        // Flag.MOVE ensures we override following, wandering, and sitting
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // 1. Is the dog healthy? No need to panic.
        if (this.wolf.getHealth() > this.healthThreshold && !this.wolf.isOnFire()) {
            return false;
        }

        // 2. Identify a threat. Did we have a target, or did something just hit us?
        LivingEntity potentialThreat = this.wolf.getTarget();
        if (potentialThreat == null) {
            potentialThreat = this.wolf.getLastHurtByMob();
        }

        // 3. If no immediate threat exists, but we are still injured and were previously distressed,
        // we can start the goal in "roam/recover" mode.
        if (potentialThreat == null && !WolfDistressTracker.isDistressed(this.wolf.getUUID())) {
            return false;
        }

        this.currentThreat = potentialThreat;
        return tryFindEscapeRoute();
    }

    @Override
    public boolean canContinueToUse() {
        // Do NOT stop if the path is done!
        // We only stop panicking if we are healed above the threshold.
        return this.wolf.getHealth() <= this.healthThreshold || this.wolf.isOnFire();
    }

    @Override
    public void start() {
        WolfDistressTracker.setDistressed(this.wolf.getUUID());

        // Immediately drop aggro so the vanilla MeleeAttackGoal doesn't try to pull us back into the fight
        this.wolf.setTarget(null);
        this.howlCooldown = 20; // Howl shortly after panicking begins

        if (this.currentPath != null) {
            double speed = (this.currentThreat != null) ? this.fleeSpeed : this.roamSpeed;
            this.wolf.getNavigation().moveTo(this.currentPath, speed);
        }
    }

    @Override
    public void stop() {
        this.wolf.getNavigation().stop();
        this.currentThreat = null;

        // Only clear the distress state if we actually recovered our health.
        if (this.wolf.getHealth() > this.healthThreshold && !this.wolf.isOnFire()) {
            WolfDistressTracker.clearDistressed(this.wolf.getUUID());
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        // 1. Audio Feedback (Distress Howling)
        if (--this.howlCooldown <= 0) {
            this.wolf.playSound(CoralineSounds.DOG_HOWL.get(), 1.0F, 0.9F + this.wolf.getRandom().nextFloat() * 0.2F);
            this.howlCooldown = 60 + this.wolf.getRandom().nextInt(60); // Randomize next howl
        }

        // 2. Threat Evaluation
        if (this.currentThreat != null) {
            // If the threat died, despawned, or we finally outran its tracking range
            if (this.currentThreat.isDeadOrDying() || this.wolf.distanceToSqr(this.currentThreat) > (this.safeDistance * this.safeDistance)) {
                this.currentThreat = null;
            }
        } else {
            // While roaming, keep a lookout just in case a new threat wanders too close
            LivingEntity newThreat = this.wolf.getLastHurtByMob();
            if (newThreat != null && newThreat.isAlive() && this.wolf.distanceToSqr(newThreat) <= (this.safeDistance * this.safeDistance)) {
                this.currentThreat = newThreat;
            }
        }

        // 3. Movement & Repathing
        if (--this.repathCooldown <= 0 && (this.wolf.getNavigation().isDone() || this.wolf.getNavigation().isStuck())) {
            this.repathCooldown = 10; // Don't spam pathfinding math every single tick

            if (tryFindEscapeRoute() && this.currentPath != null) {
                double speed = (this.currentThreat != null) ? this.fleeSpeed : this.roamSpeed;
                this.wolf.getNavigation().moveTo(this.currentPath, speed);
            }
        }
    }

    /**
     * Calculates the best route. If a threat is present, it calculates a path AWAY from it.
     * If cornered or if no threat is present, it calculates a random path.
     */
    private boolean tryFindEscapeRoute() {
        Vec3 targetPos = null;

        if (this.currentThreat != null) {
            // Attempt to path directly away from the threat (16 blocks is pathfinding radius, not total flee distance)
            targetPos = DefaultRandomPos.getPosAway(this.wolf, 16, 7, this.currentThreat.position());
        }

        // Fallback: If we are cornered (targetPos is null) OR if we are just roaming (no threat)
        if (targetPos == null) {
            // Frantically scramble to a random nearby block (PanicGoal logic)
            targetPos = DefaultRandomPos.getPos(this.wolf, 10, 7);
        }

        if (targetPos != null) {
            this.currentPath = this.wolf.getNavigation().createPath(targetPos.x, targetPos.y, targetPos.z, 0);
            return this.currentPath != null;
        }

        return false;
    }
}