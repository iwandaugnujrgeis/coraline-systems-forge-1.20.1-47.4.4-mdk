package net.zharok01.coralinesystems.entity.ai;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.entity.HelperEntity;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class HelperFleePlayerGoal extends Goal {

    private static final double TRIGGER_RADIUS = 32.0D;
    private static final double TRIGGER_RADIUS_SQ = TRIGGER_RADIUS * TRIGGER_RADIUS;

    private static final double DISENGAGE_RADIUS = 40.0D;
    private static final double DISENGAGE_RADIUS_SQ = DISENGAGE_RADIUS * DISENGAGE_RADIUS;

    private static final int SEARCH_HORIZONTAL = 16;
    private static final int SEARCH_VERTICAL = 7;

    private static final double WALK_SPEED = 1.0D;

    private final HelperEntity helper;

    @Nullable
    private Player threat;

    public HelperFleePlayerGoal(HelperEntity helper) {
        this.helper = helper;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    private static boolean isThreat(Player player) {
        return !player.isCreative() && !player.isSpectator();
    }

    @Override
    public boolean canUse() {
        if (!this.helper.isCowardly() || this.helper.isJamming()) return false;

        Player nearest = this.helper.level().getNearestPlayer(this.helper, TRIGGER_RADIUS);
        if (nearest == null || !isThreat(nearest)) return false;

        this.threat = nearest;
        return pickDestinationAndMove();
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.helper.isCowardly() || this.helper.isJamming()) return false;
        if (this.threat == null || !this.threat.isAlive() || !isThreat(this.threat)) return false;
        if (this.helper.distanceToSqr(this.threat) >= DISENGAGE_RADIUS_SQ) return false;
        if (!this.helper.getNavigation().isDone() && !this.helper.getNavigation().isStuck()) return true;
        return pickDestinationAndMove();
    }

    @Override
    public void start() {
        this.helper.setTarget(null);
    }

    @Override
    public void stop() {
        this.threat = null;
        this.helper.getNavigation().stop();
    }

    private boolean pickDestinationAndMove() {
        Vec3 awayFrom = this.threat != null ? this.threat.position() : this.helper.position();
        Vec3 destination = LandRandomPos.getPosAway(this.helper, SEARCH_HORIZONTAL, SEARCH_VERTICAL, awayFrom);
        if (destination == null) return false;

        Path path = this.helper.getNavigation().createPath(destination.x, destination.y, destination.z, 0);
        if (path == null) return false;

        this.helper.getNavigation().moveTo(path, WALK_SPEED);
        return true;
    }
}