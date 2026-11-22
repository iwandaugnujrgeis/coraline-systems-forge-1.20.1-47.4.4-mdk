package net.zharok01.coralinesystems.entity.ai;

import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.util.CoralineFishingTuning;

public class FishLureGoal extends Goal {
    private final AbstractFish fish;
    private int repathCooldown;
    private boolean hesitationRolled;
    private int hesitationTicks;

    public FishLureGoal(AbstractFish fish) {
        this.fish = fish;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return ((LurableFish) this.fish).coraline$getLureHook() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.repathCooldown = 0;
        this.hesitationRolled = false;
        this.hesitationTicks = 0;
        ((LurableFish) this.fish).coraline$setExtraSwimAcceleration(CoralineFishingTuning.LURE_EXTRA_SWIM_ACCELERATION);
    }

    @Override
    public void stop() {
        this.fish.getNavigation().stop();
        ((LurableFish) this.fish).coraline$setExtraSwimAcceleration(0.0);
    }

    @Override
    public void tick() {
        FishingHook hook = ((LurableFish) this.fish).coraline$getLureHook();
        if (hook == null) {
            return;
        }
        if (hook.getHookedIn() == this.fish) {
            this.fish.getNavigation().stop();
            Entity angler = hook.getOwner();
            if (angler == null) {
                return;
            }
            double awayX = this.fish.getX() - angler.getX();
            double awayZ = this.fish.getZ() - angler.getZ();
            double awayLength = Math.max(Math.sqrt(awayX * awayX + awayZ * awayZ), 1.0E-4);
            Vec3 motion = this.fish.getDeltaMovement();
            Vec3 desiredMotion = new Vec3(awayX / awayLength * CoralineFishingTuning.HOOKED_FLEE_SPEED, motion.y, awayZ / awayLength * CoralineFishingTuning.HOOKED_FLEE_SPEED);
            this.fish.setDeltaMovement(motion.lerp(desiredMotion, CoralineFishingTuning.HOOKED_FLEE_RESPONSIVENESS));
            this.coraline$turnTowards(awayX, awayZ);
            return;
        }
        this.fish.getLookControl().setLookAt(hook, 30.0F, 30.0F);
        Vec3 target = new Vec3(hook.getX(), hook.getY() - CoralineFishingTuning.LURE_DEPTH_OFFSET, hook.getZ());
        Vec3 toTarget = target.subtract(this.fish.getEyePosition());
        if (toTarget.length() > CoralineFishingTuning.DIRECT_STEER_RANGE) {
            if (--this.repathCooldown <= 0) {
                this.repathCooldown = this.adjustedTickDelay(CoralineFishingTuning.LURE_REPATH_INTERVAL_TICKS);
                this.fish.getNavigation().moveTo(target.x, target.y, target.z, CoralineFishingTuning.LURE_SPEED_MODIFIER);
            }
            return;
        }
        this.fish.getNavigation().stop();
        if (!this.hesitationRolled) {
            this.hesitationRolled = true;
            if (this.fish.getRandom().nextFloat() < CoralineFishingTuning.HESITATION_CHANCE) {
                this.hesitationTicks = Mth.nextInt(this.fish.getRandom(), CoralineFishingTuning.HESITATION_MIN_TICKS, CoralineFishingTuning.HESITATION_MAX_TICKS);
            }
        }
        if (this.hesitationTicks > 0) {
            --this.hesitationTicks;
            this.fish.setDeltaMovement(this.fish.getDeltaMovement().scale(CoralineFishingTuning.HESITATION_DRAG));
            return;
        }
        Vec3 desiredMotion = toTarget.normalize().scale(CoralineFishingTuning.FINAL_APPROACH_SPEED);
        this.fish.setDeltaMovement(this.fish.getDeltaMovement().lerp(desiredMotion, CoralineFishingTuning.FINAL_APPROACH_RESPONSIVENESS));
        this.coraline$turnTowards(toTarget.x, toTarget.z);
    }

    private void coraline$turnTowards(double dx, double dz) {
        float targetYaw = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
        this.fish.setYRot(Mth.rotLerp(0.3F, this.fish.getYRot(), targetYaw));
        this.fish.yBodyRot = this.fish.getYRot();
    }
}
