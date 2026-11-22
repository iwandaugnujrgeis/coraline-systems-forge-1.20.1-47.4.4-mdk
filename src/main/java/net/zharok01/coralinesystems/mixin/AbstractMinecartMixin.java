package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.util.MinecartGameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin {

    @Unique
    private static final double VANILLA_STEP_DISTANCE = 0.4;
    @Unique
    private static final double MIN_REMAINING_DISTANCE = 0.0001;
    @Unique
    private static final double MOMENTUM_CAP_MULTIPLIER = 5.0;

    @Shadow
    protected abstract void moveAlongTrack(BlockPos pos, BlockState state);

    @Shadow
    protected abstract void comeOffTrack();

    @Unique
    private double coraline$remainingDistance;

    @Unique
    private double coraline$getMaxSpeedPerTick() {
        AbstractMinecart cart = (AbstractMinecart) (Object) this;
        return cart.level().getGameRules().getInt(MinecartGameRules.MINECART_MAX_SPEED) / 20.0;
    }

    @Inject(method = "moveAlongTrack", at = @At("TAIL"))
    private void coraline$keepMovingAlongTrack(BlockPos pos, BlockState state, CallbackInfo ci) {
        AbstractMinecart cart = (AbstractMinecart) (Object) this;
        if (coraline$remainingDistance != 0 || cart.level().isClientSide || cart.getCurrentCartSpeedCapOnRail() <= VANILLA_STEP_DISTANCE) {
            return;
        }

        double speed = cart.getDeltaMovement().horizontalDistance();
        coraline$remainingDistance = Math.min(coraline$getMaxSpeedPerTick(), speed) - VANILLA_STEP_DISTANCE;
        while (coraline$remainingDistance > MIN_REMAINING_DISTANCE) {
            BlockPos railPos = cart.getCurrentRailPosition();
            BlockState railState = cart.level().getBlockState(railPos);
            if (!cart.canUseRail() || !BaseRailBlock.isRail(railState)) {
                this.comeOffTrack();
                break;
            }
            this.moveAlongTrack(railPos, railState);
            coraline$remainingDistance -= VANILLA_STEP_DISTANCE;
        }
        coraline$remainingDistance = 0;
    }

    @Inject(method = "getMaxSpeedWithRail", at = @At("HEAD"), cancellable = true, remap = false)
    private void coraline$overrideMaxSpeedWithRail(CallbackInfoReturnable<Double> cir) {
        AbstractMinecart cart = (AbstractMinecart) (Object) this;
        double stepDistance = Math.min(VANILLA_STEP_DISTANCE, coraline$getMaxSpeedPerTick());
        stepDistance = Math.min(stepDistance, cart.getCurrentCartSpeedCapOnRail());
        if (coraline$remainingDistance > 0) {
            stepDistance = Math.min(stepDistance, coraline$remainingDistance);
        }
        if (cart.isInWater()) {
            stepDistance /= 2;
        }
        cir.setReturnValue(stepDistance);
    }

    @ModifyConstant(method = "moveAlongTrack", constant = @Constant(doubleValue = 2.0, ordinal = 0))
    private double coraline$raiseMomentumCap(double vanillaCap) {
        return coraline$getMaxSpeedPerTick() * MOMENTUM_CAP_MULTIPLIER;
    }

    @Redirect(
            method = "moveAlongTrack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/Vec3;multiply(DDD)Lnet/minecraft/world/phys/Vec3;",
                    ordinal = 1
            )
    )
    private Vec3 coraline$clampSlopeCorrection(Vec3 velocity, double factorX, double factorY, double factorZ, BlockPos pos, BlockState state) {
        return velocity.multiply(Math.max(0.0, factorX), factorY, Math.max(0.0, factorZ));
    }
}