package net.zharok01.coralinesystems.mixin;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityLadderMixin extends Entity {
    public LivingEntityLadderMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Unique private int coraline$climbDownTicks = 0;
    @Unique private int coraline$climbUpTicks = 0;
    @Unique private boolean coraline$climbingUpThisTick = false;

    @Shadow
    public abstract boolean onClimbable();

    @Redirect(
            method = "handleOnClimbable(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(DDD)D")
    )
    private double coraline$modifyHorizontalMovementWhenClimbing(double speed, double vanillaSpeedMin, double vanillaSpeedMax) {
        if (!level().isClientSide()) return Mth.clamp(speed, vanillaSpeedMin, vanillaSpeedMax);

        if (!this.onGround() && this.isCrouching()) {
            return Mth.clamp(speed, vanillaSpeedMin, vanillaSpeedMax);
        } else {
            return speed;
        }
    }

    @Redirect(
            method = "handleOnClimbable(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;",
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(DD)D")
    )
    private double coraline$modifyVerticalMovementWhenClimbing(double currentYSpeed, double vanillaDownSpeed) {
        if (!level().isClientSide()) return Math.max(currentYSpeed, vanillaDownSpeed);

        double maxDownSpeed = Mth.clampedMap(getXRot(), 20, 90, vanillaDownSpeed, -0.4);
        if (maxDownSpeed < -0.15) {
            maxDownSpeed = Mth.clampedMap(coraline$climbDownTicks, 0, 60, maxDownSpeed, maxDownSpeed * 1.5);
        }
        return Math.max(currentYSpeed, maxDownSpeed);
    }

    @Inject(
            method = "handleRelativeFrictionAndCalculateMovement(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;",
            at = @At("RETURN"), cancellable = true
    )
    private void coraline$incrementClimbTimer(CallbackInfoReturnable<Vec3> cir) {
        if (!level().isClientSide()) return;

        Vec3 movement = cir.getReturnValue();
        if (onClimbable() && movement.y < 0 && getXRot() > 20) {
            coraline$climbDownTicks++;
        } else {
            coraline$climbDownTicks = 0;
        }
        if (coraline$climbingUpThisTick) {
            coraline$climbUpTicks++;
            coraline$climbingUpThisTick = false;
        } else {
            coraline$climbUpTicks = 0;
        }
        cir.setReturnValue(movement);
    }

    @ModifyArg(
            method = "handleRelativeFrictionAndCalculateMovement(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;<init>(DDD)V"), index = 1
    )
    private double coraline$allowJumpingInLadderAndSpeedUpClimbing(double vanillaClimbSpeed) {
        if (!level().isClientSide()) return vanillaClimbSpeed;

        coraline$climbingUpThisTick = true;
        double increasedVanillaClimbSpeed = vanillaClimbSpeed * 1.25;
        double climbYSpeed = Mth.clampedMap(coraline$climbUpTicks, 0, 60, increasedVanillaClimbSpeed, increasedVanillaClimbSpeed * 2);
        return Math.max(this.getDeltaMovement().y, climbYSpeed);
    }

    @Redirect(
            method = "handleRelativeFrictionAndCalculateMovement(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;horizontalCollision:Z", opcode = 180)
    )
    private boolean coraline$cancelNonDeliberateCollision(LivingEntity livingEntity) {
        if (livingEntity instanceof Player player && player.isLocalPlayer()) {
            return livingEntity.horizontalCollision && (livingEntity.xxa != 0.0F || livingEntity.zza != 0.0F);
        }

        return livingEntity.horizontalCollision;
    }
}