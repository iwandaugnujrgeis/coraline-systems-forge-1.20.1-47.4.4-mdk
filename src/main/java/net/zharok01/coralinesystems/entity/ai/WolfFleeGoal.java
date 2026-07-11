package net.zharok01.coralinesystems.entity.ai;

import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;

/**
 * Fight-or-flight goal for tamed Wolves. Fires only when the Wolf is
 * actively in combat (has a target) AND has dropped to/below the flee
 * health threshold — so it never triggers from just walking around with
 * no danger present.
 *
 * Flees from the nearest hostile Monster, or from a Player/Wolf it is
 * currently fighting (PvP-tamed-wolf scenarios), by reusing vanilla
 * AvoidEntityGoal<LivingEntity>'s single-nearest-threat pathing and its
 * built-in tick() walk/sprint distance switching — per project decision,
 * we're not building multi-threat vector-averaging here.
 *
 * On start: forcibly un-sits the Wolf (sitting is a persistent boolean,
 * not a Goal.Flag, so out-prioritizing SitWhenOrderedToGoal alone would
 * not be enough — FollowOwnerGoal/SprintingFollowOwnerGoal and vanilla
 * itself both hard-check isOrderedToSit()), drops its combat target, and
 * flags the Wolf as distressed via WolfDistressTracker so
 * WolfDistressRoamGoal picks up once this goal's path is exhausted.
 *
 * Priority: must be registered ABOVE (lower number than) both
 * SitWhenOrderedToGoal (2) and SprintingFollowOwnerGoal (6) in
 * WolfGoalSwapHandler so it preempts them via GoalSelector's flag-locking.
 */
public class WolfFleeGoal extends AvoidEntityGoal<LivingEntity> {

    /** 3 hearts. Flee triggers at or below this HP. */
    private static final float FLEE_HEALTH_THRESHOLD = 6.0F;

    private static final float MAX_FLEE_SEARCH_DISTANCE = 12.0F;
    private static final double WALK_SPEED   = 1.0;
    private static final double SPRINT_SPEED = 1.8;

    private final Wolf wolf;

    public WolfFleeGoal(Wolf wolf) {
        super(
                wolf,
                LivingEntity.class,
                WolfFleeGoal::isValidThreat,
                MAX_FLEE_SEARCH_DISTANCE,
                WALK_SPEED,
                SPRINT_SPEED,
                EntitySelector.NO_CREATIVE_OR_SPECTATOR::test
        );
        this.wolf = wolf;
    }

    /**
     * A "threat" for flee purposes is either:
     *  - any hostile Monster within search range, or
     *  - the Wolf's current combat target specifically, if it's a Player
     *    or another Wolf (covers PvP-tamed-wolf-vs-wolf and wolf-vs-player
     *    fighting, which vanilla Monster-only avoidance would miss).
     *
     * We can't reference the fleeing Wolf's instance here since this is a
     * static predicate shared across construction; the target-check half
     * is intentionally broad (any Player/Wolf) since AvoidEntityGoal's
     * TargetingConditions.forCombat() already restricts candidates to
     * ones capable of engaging in combat with the mob.
     */
    private static boolean isValidThreat(LivingEntity candidate) {
        return candidate instanceof Monster
                || candidate instanceof Player
                || candidate instanceof Wolf;
    }

    @Override
    public boolean canUse() {
        if (this.wolf.getTarget() == null) {
            return false;
        }
        if (this.wolf.getHealth() > FLEE_HEALTH_THRESHOLD) {
            return false;
        }
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        // Stop as soon as we've reached the safe spot (path exhausted) —
        // AvoidEntityGoal.canContinueToUse() already reports that via
        // pathNav.isDone(). We don't re-check health here; that's Phase B's
        // job once WolfDistressRoamGoal takes over.
        return super.canContinueToUse();
    }

    @Override
    public void start() {
        this.wolf.setOrderedToSit(false);
        this.wolf.setTarget(null);
        WolfDistressTracker.setDistressed(this.wolf.getUUID());
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        // Distress flag is intentionally NOT cleared here — WolfFleeGoal
        // reaching a safe spot doesn't mean the Wolf is healed. Ownership
        // of clearing the flag belongs to WolfDistressRoamGoal, which is
        // the only goal that checks health-recovery.
    }
}