package net.zharok01.coralinesystems.mixin.entity;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.entity.ai.FishFleePlayerGoal;
import net.zharok01.coralinesystems.entity.ai.FishLureGoal;
import net.zharok01.coralinesystems.entity.ai.LurableFish;
import net.zharok01.coralinesystems.util.CoralineFishingTuning;
import net.zharok01.coralinesystems.registry.CoralineTags;
import net.zharok01.coralinesystems.registry.CoralineTriggers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFish.class)
public abstract class AbstractFishMixin extends WaterAnimal implements LurableFish {
    @Unique
    @Nullable
    private FishingHook coraline$lureHook;
    @Unique
    private double coraline$extraSwimAcceleration;
    @Unique
    @Nullable
    private Entity coraline$reelTarget;
    @Unique
    private int coraline$reelTicks;
    @Unique
    private boolean coraline$reelLaunched;
    @Unique
    private int coraline$flightTicks;
    @Unique
    private double coraline$flightSpeedX;
    @Unique
    private double coraline$flightSpeedZ;

    protected AbstractFishMixin(EntityType<? extends WaterAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void coraline$registerFishGoals(CallbackInfo ci) {
        if (!this.getType().is(CoralineTags.FISH)) {
            return;
        }
        AbstractFish fish = (AbstractFish) (Object) this;
        this.goalSelector.getAvailableGoals().removeIf(wrappedGoal -> wrappedGoal.getGoal() instanceof AvoidEntityGoal);
        this.goalSelector.addGoal(1, new FishLureGoal(fish));
        this.goalSelector.addGoal(2, new FishFleePlayerGoal(fish));
    }

    @Inject(method = "travel", at = @At("HEAD"))
    private void coraline$boostSwimming(Vec3 travelVector, CallbackInfo ci) {
        if (this.coraline$extraSwimAcceleration > 0.0 && this.isEffectiveAi() && this.isInWater() && !this.getNavigation().isDone()) {
            Vec3 heading = this.calculateViewVector(0.0F, this.getYRot());
            this.setDeltaMovement(this.getDeltaMovement().add(heading.scale(this.coraline$extraSwimAcceleration)));
        }
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void coraline$applyReelPull(CallbackInfo ci) {
        if (this.coraline$reelTicks <= 0) {
            return;
        }
        --this.coraline$reelTicks;
        if (!this.coraline$reelTarget.isAlive()) {
            this.coraline$reelTicks = 0;
            return;
        }
        if (!this.coraline$reelLaunched && !this.isInWater()) {
            this.coraline$launchTowardsAngler();
        }
        if (this.coraline$reelLaunched) {
            ++this.coraline$flightTicks;
            boolean landed = this.coraline$flightTicks >= CoralineFishingTuning.REEL_LANDING_GRACE_TICKS && this.onGround() && !this.isInWater();
            if (landed) {
                if (this.coraline$reelTarget instanceof ServerPlayer player) {
                    player.awardStat(Stats.FISH_CAUGHT);
                    CoralineTriggers.FISH_REELED_OUT.trigger(player);
                }
                this.coraline$reelTicks = 0;
                return;
            }
            if (!this.horizontalCollision) {
                this.setDeltaMovement(this.coraline$flightSpeedX, this.getDeltaMovement().y, this.coraline$flightSpeedZ);
            }
            this.hasImpulse = true;
            return;
        }
        Vec3 motion = this.getDeltaMovement();
        double dx = this.coraline$reelTarget.getX() - this.getX();
        double dz = this.coraline$reelTarget.getZ() - this.getZ();
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        double newX = motion.x;
        double newZ = motion.z;
        if (horizontalDistance > CoralineFishingTuning.REEL_STOP_DISTANCE) {
            newX = Mth.lerp(CoralineFishingTuning.REEL_RESPONSIVENESS, motion.x, dx / horizontalDistance * CoralineFishingTuning.REEL_SPEED);
            newZ = Mth.lerp(CoralineFishingTuning.REEL_RESPONSIVENESS, motion.z, dz / horizontalDistance * CoralineFishingTuning.REEL_SPEED);
        }
        this.setDeltaMovement(newX, Math.max(motion.y, CoralineFishingTuning.REEL_WATER_LIFT), newZ);
        this.hasImpulse = true;
    }

    @Unique
    private void coraline$launchTowardsAngler() {
        double dx = this.coraline$reelTarget.getX() - this.getX();
        double dz = this.coraline$reelTarget.getZ() - this.getZ();
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        double landingDistance = Mth.clamp(horizontalDistance - CoralineFishingTuning.REEL_LANDING_OFFSET, 0.0, CoralineFishingTuning.REEL_MAX_RANGE);
        double rise = Mth.clamp(this.coraline$reelTarget.getY() - this.getY(), -CoralineFishingTuning.REEL_MAX_DROP, CoralineFishingTuning.REEL_MAX_RISE);
        double flightTime = CoralineFishingTuning.REEL_MIN_FLIGHT_TICKS + landingDistance * CoralineFishingTuning.REEL_FLIGHT_TICKS_PER_BLOCK + Math.max(0.0, rise) * CoralineFishingTuning.REEL_FLIGHT_TICKS_PER_RISE_BLOCK;
        int flightTicks = (int) Math.round(Math.min(flightTime, CoralineFishingTuning.REEL_MAX_FLIGHT_TICKS));
        double terminalFall = CoralineFishingTuning.AIR_GRAVITY * CoralineFishingTuning.AIR_VERTICAL_DRAG / (1.0 - CoralineFishingTuning.AIR_VERTICAL_DRAG);
        double launchSpeedY = (rise + terminalFall * flightTicks) * (1.0 - CoralineFishingTuning.AIR_VERTICAL_DRAG) / (1.0 - Math.pow(CoralineFishingTuning.AIR_VERTICAL_DRAG, flightTicks)) - terminalFall;
        double horizontalScale = landingDistance / flightTicks / Math.max(horizontalDistance, 1.0E-4);
        this.coraline$flightSpeedX = dx * horizontalScale;
        this.coraline$flightSpeedZ = dz * horizontalScale;
        this.setDeltaMovement(this.coraline$flightSpeedX, launchSpeedY, this.coraline$flightSpeedZ);
        float yaw = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.hasImpulse = true;
        this.coraline$reelLaunched = true;
    }

    @Override
    @Nullable
    public FishingHook coraline$getLureHook() {
        return this.coraline$lureHook;
    }

    @Override
    public void coraline$setLureHook(@Nullable FishingHook hook) {
        this.coraline$lureHook = hook;
    }

    @Override
    public void coraline$setExtraSwimAcceleration(double acceleration) {
        this.coraline$extraSwimAcceleration = acceleration;
    }

    @Override
    public void coraline$startReel(Entity target) {
        this.coraline$reelTarget = target;
        this.coraline$reelTicks = CoralineFishingTuning.REEL_MAX_TICKS;
        this.coraline$reelLaunched = false;
        this.coraline$flightTicks = 0;
    }

    @Override
    public boolean coraline$isReeling() {
        return this.coraline$reelTicks > 0;
    }
}
