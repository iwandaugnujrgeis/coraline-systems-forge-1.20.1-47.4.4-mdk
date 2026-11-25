package net.zharok01.coralinesystems.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityRaccoon;
import einstein.subtle_effects.networking.clientbound.ClientBoundEntitySpawnSprintingDustCloudsPacket;
import einstein.subtle_effects.platform.Services;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;

public class SprintingFollowOwnerGoal extends FollowOwnerGoal {

    private static final float WALK_START_DISTANCE = 10.0F;
    private static final float STOP_DISTANCE = 2.0F;
    private static final float SPRINT_THRESHOLD = 12.0F;

    private static final double WALK_SPEED = 1.0;
    private static final double SPRINT_SPEED = 1.3;

    private static final int SPRINT_DUST_INTERVAL_TICKS = 4;

    private final TamableAnimal tamable;
    private final PathNavigation navigation;
    private int sprintDustCooldown;

    public SprintingFollowOwnerGoal(TamableAnimal tamable) {
        this(tamable, false);
    }

    public SprintingFollowOwnerGoal(TamableAnimal tamable, boolean canFly) {
        super(tamable, WALK_SPEED, WALK_START_DISTANCE, STOP_DISTANCE, canFly);
        this.tamable = tamable;
        this.navigation = tamable.getNavigation();
    }

    @Override
    public boolean canUse() {
        if (this.tamable instanceof EntityRaccoon raccoon && raccoon.isSitting()) {
            return false;
        }
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        if (this.tamable instanceof EntityRaccoon raccoon && raccoon.isSitting()) {
            return false;
        }
        return super.canContinueToUse();
    }

    @Override
    public void stop() {
        super.stop();
        this.tamable.setSprinting(false);
    }

    @Override
    public void tick() {
        LivingEntity owner = this.tamable.getOwner();
        if (owner == null) {
            this.tamable.setSprinting(false);
            return;
        }

        this.tamable.getLookControl().setLookAt(owner, 10.0F, this.tamable.getMaxHeadXRot());

        double distanceSq = this.tamable.distanceToSqr(owner);
        double sprintThresholdSq = SPRINT_THRESHOLD * SPRINT_THRESHOLD;
        boolean sprinting = distanceSq >= sprintThresholdSq;
        this.tamable.setSprinting(sprinting);

        if (sprinting) {
            this.navigation.moveTo(owner, SPRINT_SPEED);
            trySpawnSprintDust();
        } else {
            this.navigation.moveTo(owner, WALK_SPEED);
        }
    }

    private void trySpawnSprintDust() {
        if (!(this.tamable.level() instanceof ServerLevel serverLevel)) return;

        if (!this.tamable.onGround()) return;

        if (this.sprintDustCooldown > 0) {
            this.sprintDustCooldown--;
            return;
        }
        this.sprintDustCooldown = SPRINT_DUST_INTERVAL_TICKS;

        Services.NETWORK.sendToClientsTracking(
                serverLevel,
                this.tamable.blockPosition(),
                new ClientBoundEntitySpawnSprintingDustCloudsPacket(this.tamable.getId())
        );
    }
}