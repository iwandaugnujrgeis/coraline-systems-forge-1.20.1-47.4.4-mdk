package net.zharok01.coralinesystems.entity.ai;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.zharok01.coralinesystems.registry.CoralineSounds;

public class SprintingFollowOwnerGoal extends FollowOwnerGoal {

    private static final float  WALK_START_DISTANCE = 10.0F;
    private static final float  STOP_DISTANCE       = 2.0F;
    private static final float  SPRINT_THRESHOLD    = 12.0F;
    private static final float  TELEPORT_THRESHOLD  = 24.0F;

    private static final double WALK_SPEED          = 1.0;
    private static final double SPRINT_SPEED        = 2.0;

    /** Minimum squared distance moved in one tick to count as a teleport. */
    private static final double TELEPORT_DETECT_SQ  = 4.0; // 2 blocks — no natural movement crosses this in one tick

    private final TamableAnimal tamable;
    private final PathNavigation navigation;

    public SprintingFollowOwnerGoal(TamableAnimal tamable) {
        super(tamable, WALK_SPEED, WALK_START_DISTANCE, STOP_DISTANCE, false);
        this.tamable    = tamable;
        this.navigation = tamable.getNavigation();
    }

    @Override
    public void tick() {
        LivingEntity owner = this.tamable.getOwner();
        if (owner == null) return;

        this.tamable.getLookControl().setLookAt(owner, 10.0F, this.tamable.getMaxHeadXRot());

        double distanceSq        = this.tamable.distanceToSqr(owner);
        double teleportThresholdSq = TELEPORT_THRESHOLD * TELEPORT_THRESHOLD; // 576.0
        double sprintThresholdSq   = SPRINT_THRESHOLD   * SPRINT_THRESHOLD;   // 144.0

        if (distanceSq >= teleportThresholdSq) {
            // Snapshot position before vanilla teleports the pet
            double oldX = this.tamable.getX();
            double oldY = this.tamable.getY();
            double oldZ = this.tamable.getZ();

            super.tick(); // vanilla teleportToOwner() fires in here

            // Detect whether a teleport actually occurred by checking displacement
            double movedSq = this.tamable.distanceToSqr(oldX, oldY, oldZ);
            if (movedSq >= TELEPORT_DETECT_SQ) {
                playTeleportEffects(oldX, oldY, oldZ);
            }
        } else if (distanceSq >= sprintThresholdSq) {
            // 12–24 blocks — sprint
            this.navigation.moveTo(owner, SPRINT_SPEED);
        } else {
            // Under 12 blocks — walk
            this.navigation.moveTo(owner, WALK_SPEED);
        }
    }

    /**
     * Spawns cloud particles at both the origin and destination of the teleport,
     * and plays PET_TELEPORT at the destination.
     *
     * Particles are sent server-side via ServerLevel.sendParticles(), mirroring
     * the pattern used in CoralineUtils.randomTeleportStatic().
     */
    private void playTeleportEffects(double oldX, double oldY, double oldZ) {
        if (!(this.tamable.level() instanceof ServerLevel serverLevel)) return;

        double newX = this.tamable.getX();
        double newY = this.tamable.getY();
        double newZ = this.tamable.getZ();

        // Origin burst — where the pet vanished from
        serverLevel.sendParticles(
                ParticleTypes.CLOUD,
                oldX, oldY + 0.5, oldZ,
                10,          // count
                0.3, 0.4, 0.3, // spread XYZ
                0.02         // speed
        );

        // Destination burst — where the pet arrived
        serverLevel.sendParticles(
                ParticleTypes.CLOUD,
                newX, newY + 0.5, newZ,
                10,
                0.3, 0.4, 0.3,
                0.02
        );

        // Sound at the destination
        this.tamable.playSound(
                CoralineSounds.PET_TELEPORT.get(),
                1.0F,
                0.8F + this.tamable.getRandom().nextFloat() * 0.4F // slight pitch variation
        );
    }
}