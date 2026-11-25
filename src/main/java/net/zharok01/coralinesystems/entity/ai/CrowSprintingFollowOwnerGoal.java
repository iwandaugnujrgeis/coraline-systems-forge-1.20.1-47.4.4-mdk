package net.zharok01.coralinesystems.entity.ai;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.entity.EntityCrow;
import com.github.alexthe666.alexsmobs.message.MessageCrowMountPlayer;
import com.github.alexthe666.alexsmobs.misc.AMBlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class CrowSprintingFollowOwnerGoal extends Goal {

    private static final int COMMAND_FOLLOW = 1;
    private static final float START_DISTANCE = 4.0F;
    private static final float SPRINT_THRESHOLD = 12.0F;
    private static final float SHOULDER_LANDING_DISTANCE = 2.0F;
    private static final double CIRCLE_SPEED = 0.7;
    private static final double SPRINT_SPEED = 1.3;
    private static final float CIRCLE_ANGLE_PER_TICK = 0.13962634F;

    private final EntityCrow crow;
    private LivingEntity owner;
    private float circlingTime;
    private float circleDistance;
    private int maxCircleTime;
    private boolean clockwise;

    public CrowSprintingFollowOwnerGoal(EntityCrow crow) {
        this.crow = crow;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity candidate = this.crow.getOwner();
        if (candidate == null || candidate.isSpectator()) return false;
        if (this.crow.isSitting() || this.crow.isPassenger()) return false;
        if (this.crow.getCommand() != COMMAND_FOLLOW) return false;
        if (this.crow.distanceToSqr(candidate) < START_DISTANCE * START_DISTANCE) return false;
        if (this.crow.getTarget() != null && this.crow.getTarget().isAlive()) return false;

        this.owner = candidate;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.crow.isSitting()
                && !this.crow.isPassenger()
                && this.crow.getCommand() == COMMAND_FOLLOW
                && (this.crow.getTarget() == null || !this.crow.getTarget().isAlive());
    }

    @Override
    public void start() {
        this.circlingTime = 0.0F;
        this.clockwise = this.crow.getRandom().nextBoolean();
        this.maxCircleTime = 20 + this.crow.getRandom().nextInt(100);
        this.circleDistance = 1.0F + this.crow.getRandom().nextFloat() * 2.0F;
    }

    @Override
    public void stop() {
        this.owner = null;
        this.circlingTime = 0.0F;
        this.crow.getNavigation().stop();
    }

    @Override
    public void tick() {
        this.crow.getLookControl().setLookAt(this.owner, 10.0F, this.crow.getMaxHeadXRot());
        if (this.crow.isLeashed() || this.crow.isPassenger() || this.crow.aiItemFlag) return;

        double distanceSq = this.crow.distanceToSqr(this.owner);
        double headY = this.owner.getY() + this.owner.getEyeHeight() + 0.2;

        if (distanceSq >= SPRINT_THRESHOLD * SPRINT_THRESHOLD) {
            this.crow.setFlying(true);
            this.crow.getMoveControl().setWantedPosition(this.owner.getX(), headY, this.owner.getZ(), SPRINT_SPEED);
            this.circlingTime = 0.0F;
            return;
        }

        if (this.crow.isFlying()) {
            this.circlingTime++;
        }

        if (this.circlingTime > this.maxCircleTime && this.crow.getRidingCrows(this.owner) < 2) {
            this.crow.getMoveControl().setWantedPosition(this.owner.getX(), headY, this.owner.getZ(), CIRCLE_SPEED);
            if (this.crow.distanceTo(this.owner) < SHOULDER_LANDING_DISTANCE) {
                this.crow.startRiding(this.owner, true);
                if (!this.crow.level().isClientSide()) {
                    AlexsMobs.sendMSGToAll(new MessageCrowMountPlayer(this.crow.getId(), this.owner.getId()));
                }
            }
        } else {
            Vec3 circlePos = this.getCirclePos();
            this.crow.setFlying(true);
            this.crow.getMoveControl().setWantedPosition(circlePos.x, circlePos.y + this.owner.getEyeHeight() + 0.2, circlePos.z, CIRCLE_SPEED);
        }
    }

    private Vec3 getCirclePos() {
        float angle = CIRCLE_ANGLE_PER_TICK * (this.clockwise ? -this.circlingTime : this.circlingTime);
        double extraX = this.circleDistance * Mth.sin(angle);
        double extraZ = this.circleDistance * Mth.cos(angle);
        Vec3 pos = new Vec3(this.owner.getX() + extraX, this.owner.getY(), this.owner.getZ() + extraZ);
        return this.crow.level().isEmptyBlock(AMBlockPos.fromVec3(pos)) ? pos : this.owner.position();
    }
}