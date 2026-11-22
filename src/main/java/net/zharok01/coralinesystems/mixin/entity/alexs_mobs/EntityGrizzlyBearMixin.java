package net.zharok01.coralinesystems.mixin.entity.alexs_mobs;

import com.github.alexthe666.alexsmobs.entity.EntityGrizzlyBear;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.zharok01.coralinesystems.mixin.accessor.EntityAccessor;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(value = EntityGrizzlyBear.class, remap = false)
public abstract class EntityGrizzlyBearMixin implements PlayerRideableJumping {

    @Shadow private UUID salmonThrowerID;

    @Unique private int coraline$buckTimer = 0;
    @Unique private float coraline$playerJumpPendingScale = 0.0F;
    @Unique private int coraline$dashCooldown = 0;

    @Inject(method = "m_8119_", at = @At("TAIL"))
    private void coraline$tickBuckingAndDashCooldown(CallbackInfo ci) {
        EntityGrizzlyBear bear = (EntityGrizzlyBear)(Object)this;

        if (this.coraline$dashCooldown > 0) {
            this.coraline$dashCooldown--;
            if (this.coraline$dashCooldown == 0 && !bear.level().isClientSide()) {
                bear.playSound(SoundEvents.COW_STEP, 1.0F, 0.9F);
            }
        }

        if (bear.level().isClientSide()) return;

        if (bear.isVehicle()) {
            if (!bear.isHoneyed()) {
                if (this.coraline$buckTimer <= 0) {
                    this.coraline$buckTimer = 30 + bear.getRandom().nextInt(40);
                } else {
                    this.coraline$buckTimer--;
                    if (this.coraline$buckTimer == 0) {
                        bear.ejectPassengers();
                        bear.setStanding(true);
                        bear.playSound(CoralineSounds.BEAR_DISMOUNT.get(), 1.0F, 1.0F);
                        bear.level().broadcastEntityEvent(bear, (byte) 6);
                    }
                }
            } else {
                this.coraline$buckTimer = 0;
            }
        } else {
            this.coraline$buckTimer = 0;
        }
    }

    @Inject(method = "m_245547_", at = @At("HEAD"), cancellable = true)
    private void coraline$nerfBearSpeed(Player rider, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(0.2F);
    }

    @Inject(method = "canTargetItem", at = @At("HEAD"), cancellable = true)
    private void coraline$completelyDisableItemPickup(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "onGetItem", at = @At("TAIL"))
    private void coraline$preventDropTaming(ItemEntity targetEntity, CallbackInfo ci) {
        this.salmonThrowerID = null;
    }

    @Inject(method = "m_274498_", at = @At("TAIL"))
    private void coraline$handleBearDash(Player player, Vec3 vec3, CallbackInfo ci) {
        EntityGrizzlyBear bear = (EntityGrizzlyBear)(Object)this;
        if (bear.isControlledByLocalInstance() && bear.onGround()) {
            if (this.coraline$playerJumpPendingScale > 0.0F) {
                this.coraline$executeBearDash(this.coraline$playerJumpPendingScale);
                this.coraline$playerJumpPendingScale = 0.0F;
            }
        }
    }

    @Redirect(
            method = "m_8119_",
            at = @At(value = "INVOKE", target = "Lcom/github/alexthe666/alexsmobs/entity/EntityGrizzlyBear;m_19998_(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;")
    )
    private ItemEntity coraline$suppressFurShedding(EntityGrizzlyBear instance, ItemLike item) {
        return null;
    }

    @Unique private static final double DASH_SPEED_CAP = 0.09D * 1.15D;
    @Unique private static final double DASH_JUMP_CAP  = 0.42D * 1.15D;

    @Unique
    private void coraline$executeBearDash(float scale) {
        EntityGrizzlyBear bear = (EntityGrizzlyBear)(Object)this;
        EntityAccessor accessor = (EntityAccessor)(Object)bear;

        double rawJumpStrength = bear.getAttributes().hasAttribute(Attributes.JUMP_STRENGTH)
                ? bear.getAttributeValue(Attributes.JUMP_STRENGTH)
                : 0.45D;
        double rawSpeed = bear.getAttributeValue(Attributes.MOVEMENT_SPEED);

        double jumpStrength = Math.min(rawJumpStrength, DASH_JUMP_CAP);
        double speedBase = Math.min(rawSpeed, DASH_SPEED_CAP);

        double jumpPower = jumpStrength * accessor.coraline$invokeGetBlockJumpFactor() + bear.getJumpBoostPower();
        double speed = speedBase * accessor.coraline$invokeGetBlockSpeedFactor();

        Vec3 dashVec = bear.getLookAngle()
                .multiply(1.0D, 0.0D, 1.0D)
                .normalize()
                .scale(22.2222D * scale * speed)
                .add(0.0D, 1.4285F * scale * jumpPower, 0.0D);

        bear.addDeltaMovement(dashVec);
        this.coraline$dashCooldown = 55;
        bear.hasImpulse = true;
        ForgeHooks.onLivingJump(bear);
    }

    @Override
    public void onPlayerJump(int jumpPower) {
        EntityGrizzlyBear bear = (EntityGrizzlyBear)(Object)this;
        if (bear.isHoneyed() && this.coraline$dashCooldown <= 0 && bear.onGround()) {
            int power = Math.max(0, jumpPower);
            this.coraline$playerJumpPendingScale = power >= 90
                    ? 1.0F
                    : 0.4F + 0.4F * (float) power / 90.0F;
        }
    }

    @Override
    public boolean canJump() {
        return ((EntityGrizzlyBear)(Object)this).isHoneyed();
    }

    @Override
    public void handleStartJump(int jumpPower) {
        ((EntityGrizzlyBear)(Object)this)
                .playSound(SoundEvents.COW_STEP,
                        1.0F,
                        0.8F);
    }

    @Override
    public void handleStopJump() {}

    @Override
    public int getJumpCooldown() {
        return this.coraline$dashCooldown;
    }

    @Inject(
            method = "m_6048_()D",
            at = @At("HEAD"),
            cancellable = true
    )
    private void coraline$stabilizePassengerSeat(CallbackInfoReturnable<Double> cir) {
        EntityGrizzlyBear bear = (EntityGrizzlyBear) (Object) this;
        float sitAdd = 0.01F * bear.sitProgress;
        float standAdd = 0.07F * bear.standProgress;
        double stableSeatHeight = (double) bear.getBbHeight() - 0.3D + (double) sitAdd + (double) standAdd;
        cir.setReturnValue(stableSeatHeight);
    }
}