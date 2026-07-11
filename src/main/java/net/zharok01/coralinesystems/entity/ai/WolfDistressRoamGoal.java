package net.zharok01.coralinesystems.entity.ai;

import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.registry.CoralineSounds;

/**
 * Phase B of the Wolf fight-or-flight sequence. Takes over once
 * WolfFleeGoal has reached a safe spot (its path exhausted) while the
 * Wolf is still flagged distressed. Wanders randomly — deliberately NOT
 * toward the owner, since a distressed Wolf isn't trying to follow, it's
 * trying to survive — and periodically howls (CoralineSounds.DOG_HOWL) so
 * the owner can locate it by ear.
 *
 * Exits (and clears the distress flag) once health regenerates back
 * above the flee threshold, at which point SprintingFollowOwnerGoal
 * naturally resumes since this goal shares the MOVE flag and stops
 * claiming it.
 *
 * Priority: same tier as WolfFleeGoal (both above sit/follow), since only
 * one of the two will ever have canUse() == true at a given moment —
 * flee requires an active target, roam requires the distress flag with
 * no target requirement.
 */
public class WolfDistressRoamGoal extends Goal {

    /** Must match WolfFleeGoal.FLEE_HEALTH_THRESHOLD. */
    private static final float RECOVERY_HEALTH_THRESHOLD = 6.0F;

    private static final double ROAM_SPEED = 1.0;
    private static final int ROAM_INTERVAL_TICKS = 100;

    /** How often to howl while roaming injured (10 seconds). */
    private static final int HOWL_INTERVAL_TICKS = 200;

    private final Wolf wolf;
    private int roamCooldown;
    private int howlCooldown;

    public WolfDistressRoamGoal(Wolf wolf) {
        this.wolf = wolf;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return WolfDistressTracker.isDistressed(this.wolf.getUUID())
                && this.wolf.getHealth() <= RECOVERY_HEALTH_THRESHOLD;
    }

    @Override
    public boolean canContinueToUse() {
        return WolfDistressTracker.isDistressed(this.wolf.getUUID())
                && this.wolf.getHealth() <= RECOVERY_HEALTH_THRESHOLD;
    }

    @Override
    public void start() {
        this.wolf.setOrderedToSit(false);
        this.roamCooldown = 0;
        this.howlCooldown = HOWL_INTERVAL_TICKS;
    }

    @Override
    public void stop() {
        WolfDistressTracker.clearDistressed(this.wolf.getUUID());
        this.wolf.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.howlCooldown > 0) {
            this.howlCooldown--;
        } else {
            this.wolf.playSound(
                    CoralineSounds.DOG_HOWL.get(),
                    1.0F,
                    0.9F + this.wolf.getRandom().nextFloat() * 0.2F
            );
            this.howlCooldown = HOWL_INTERVAL_TICKS;
        }

        if (this.roamCooldown > 0) {
            this.roamCooldown--;
            return;
        }

        Vec3 target = DefaultRandomPos.getPos(this.wolf, 10, 7);
        if (target != null) {
            this.wolf.getNavigation().moveTo(target.x, target.y, target.z, ROAM_SPEED);
        }
        this.roamCooldown = ROAM_INTERVAL_TICKS + this.wolf.getRandom().nextInt(40);
    }
}