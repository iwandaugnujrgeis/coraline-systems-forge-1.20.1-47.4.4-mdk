package net.zharok01.coralinesystems.entity.ai;

import einstein.subtle_effects.networking.clientbound.ClientBoundEntitySpawnSprintingDustCloudsPacket;
import einstein.subtle_effects.platform.Services;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.entity.CryptidEntity;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class CryptidFleePlayerGoal extends Goal {

    private static final double TRIGGER_RADIUS = 48.0D;
    private static final double TRIGGER_RADIUS_SQ = TRIGGER_RADIUS * TRIGGER_RADIUS;

    private static final double DISENGAGE_RADIUS = 56.0D;
    private static final double DISENGAGE_RADIUS_SQ = DISENGAGE_RADIUS * DISENGAGE_RADIUS;

    private static final int SEARCH_HORIZONTAL = 24;
    private static final int SEARCH_VERTICAL = 7;

    private static final double WALK_SPEED = 1.0D;
    private static final double SPRINT_SPEED = 1.5D;
    private static final double SPRINT_THRESHOLD_SQ = 20.0D * 20.0D;

    private static final int SPRINT_DUST_INTERVAL = 4;

    private final CryptidEntity cryptid;
    private int dustCooldown;

    @Nullable
    private Player threat;

    public CryptidFleePlayerGoal(CryptidEntity cryptid) {
        this.cryptid = cryptid;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    private static boolean isThreat(Player player) {
        return !player.isCreative() && !player.isSpectator();
    }

    @Override
    public boolean canUse() {
        Player nearest = this.cryptid.level().getNearestPlayer(this.cryptid, TRIGGER_RADIUS);
        if (nearest == null || !isThreat(nearest)) return false;

        this.threat = nearest;
        return pickDestinationAndMove();
    }

    @Override
    public boolean canContinueToUse() {
        if (this.threat == null || !this.threat.isAlive() || !isThreat(this.threat)) return false;
        if (this.cryptid.distanceToSqr(this.threat) >= DISENGAGE_RADIUS_SQ) return false;

        if (!this.cryptid.getNavigation().isDone() && !this.cryptid.getNavigation().isStuck()) {
            tickSpeedAndDust();
            return true;
        }

        return pickDestinationAndMove();
    }

    @Override
    public void start() {
        this.cryptid.setTarget(null);
        this.cryptid.setFleeing(true);
    }

    @Override
    public void stop() {
        this.threat = null;
        this.dustCooldown = 0;
        this.cryptid.setFleeing(false);
        this.cryptid.getNavigation().stop();
    }

    private boolean pickDestinationAndMove() {
        Vec3 awayFrom = this.threat != null ? this.threat.position() : this.cryptid.position();
        Vec3 destination = LandRandomPos.getPosAway(this.cryptid, SEARCH_HORIZONTAL, SEARCH_VERTICAL, awayFrom);
        if (destination == null) return false;

        Path path = this.cryptid.getNavigation().createPath(destination.x, destination.y, destination.z, 0);
        if (path == null) return false;

        double speed = (this.threat != null && this.cryptid.distanceToSqr(this.threat) < SPRINT_THRESHOLD_SQ)
                ? SPRINT_SPEED : WALK_SPEED;
        this.cryptid.getNavigation().moveTo(path, speed);
        return true;
    }

    private void tickSpeedAndDust() {
        if (this.threat == null) return;

        double distSq = this.cryptid.distanceToSqr(this.threat);
        double activeSpeed = distSq < SPRINT_THRESHOLD_SQ ? SPRINT_SPEED : WALK_SPEED;
        this.cryptid.getNavigation().setSpeedModifier(activeSpeed);

        if (activeSpeed == SPRINT_SPEED && this.cryptid.onGround()) {
            if (this.dustCooldown > 0) {
                this.dustCooldown--;
            } else {
                this.dustCooldown = SPRINT_DUST_INTERVAL;
                if (this.cryptid.level() instanceof ServerLevel serverLevel) {
                    Services.NETWORK.sendToClientsTracking(
                            serverLevel,
                            this.cryptid.blockPosition(),
                            new ClientBoundEntitySpawnSprintingDustCloudsPacket(this.cryptid.getId())
                    );
                }
            }
        }
    }
}
