package net.zharok01.coralinesystems.entity.ai;

import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.LivingEntity;

public class SprintingFollowOwnerGoal extends FollowOwnerGoal {

    private static final float WALK_START_DISTANCE  = 10.0F; // canUse() threshold (vanilla = 10)
    private static final float STOP_DISTANCE         = 2.0F;  // stop following threshold
    private static final float SPRINT_THRESHOLD      = 12.0F; // blocks — start sprinting beyond this
    private static final float TELEPORT_THRESHOLD    = 24.0F; // blocks — teleport beyond this

    private static final double WALK_SPEED           = 1.0;   // normal follow speed multiplier
    private static final double SPRINT_SPEED         = 1.4;   // sprint speed multiplier

    private final TamableAnimal tamable;
    private final PathNavigation navigation;

    public SprintingFollowOwnerGoal(TamableAnimal tamable) {
        // Pass TELEPORT_THRESHOLD as startDistance so canUse() fires at the right time,
        // and vanilla's internal teleport check (144.0 = 12^2) is effectively overridden below.
        super(tamable, WALK_SPEED, WALK_START_DISTANCE, STOP_DISTANCE, false);
        this.tamable  = tamable;
        this.navigation = tamable.getNavigation();
    }

    @Override
    public void tick() {
        LivingEntity owner = this.tamable.getOwner();
        if (owner == null) return;

        // Always keep looking at the owner
        this.tamable.getLookControl().setLookAt(owner, 10.0F, this.tamable.getMaxHeadXRot());

        double distanceSq = this.tamable.distanceToSqr(owner);
        double teleportThresholdSq = TELEPORT_THRESHOLD * TELEPORT_THRESHOLD; // 576.0
        double sprintThresholdSq   = SPRINT_THRESHOLD   * SPRINT_THRESHOLD;   // 144.0

        if (distanceSq >= teleportThresholdSq) {
            // Beyond 24 blocks — let vanilla teleport logic handle it
            super.tick();
        } else if (distanceSq >= sprintThresholdSq) {
            // Between 12–24 blocks — sprint
            this.navigation.moveTo(owner, SPRINT_SPEED);
        } else {
            // Under 12 blocks — walk normally
            this.navigation.moveTo(owner, WALK_SPEED);
        }
    }
}