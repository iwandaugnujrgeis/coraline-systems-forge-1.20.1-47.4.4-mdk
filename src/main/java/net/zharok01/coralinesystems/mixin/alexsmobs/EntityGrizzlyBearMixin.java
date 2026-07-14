package net.zharok01.coralinesystems.mixin.alexsmobs;

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
import net.zharok01.coralinesystems.mixin.accessors.EntityAccessor;
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

    // 1. Skip Wandering Commands
    @Redirect(
            method = "m_6071_(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
            at = @At(value = "INVOKE", target = "Lcom/github/alexthe666/alexsmobs/entity/EntityGrizzlyBear;setCommand(I)V")
    )
    private void coraline$skipWandering(EntityGrizzlyBear instance, int command) {
        instance.setCommand(command == 0 ? 1 : command);
    }

    // 2. Horse-Style Bucking & Camel Dash Cooldown Ticker (aiStep -> SRG m_8119_)
    // Note: We removed the previous mount-blocking redirect so owners can mount anytime!
    @Inject(method = "m_8119_", at = @At("TAIL"))
    private void coraline$tickBuckingAndDashCooldown(CallbackInfo ci) {
        EntityGrizzlyBear bear = (EntityGrizzlyBear)(Object)this;

        // Decrement Camel dash cooldown on both client & server
        if (this.coraline$dashCooldown > 0) {
            this.coraline$dashCooldown--;
            if (this.coraline$dashCooldown == 0 && !bear.level().isClientSide()) {
                bear.playSound(SoundEvents.CAMEL_DASH_READY, 1.0F, 1.0F); // TODO: Change to Bear custom
            }
        }

        if (bear.level().isClientSide()) return;

        // Requirement 2: Horse-style bucking when ridden without active honey
        if (bear.isVehicle()) {
            if (!bear.isHoneyed()) {
                if (this.coraline$buckTimer <= 0) {
                    // Start randomized Horse-style test interval (~1.5 to 3.5 seconds)
                    this.coraline$buckTimer = 30 + bear.getRandom().nextInt(40);
                } else {
                    this.coraline$buckTimer--;
                    if (this.coraline$buckTimer == 0) {
                        // Forcibly dismount all riders
                        bear.ejectPassengers();
                        // Rear up on hind legs like an angry horse!
                        bear.setStanding(true);
                        // Play custom dismount sound
                        bear.playSound(CoralineSounds.BEAR_DISMOUNT.get(), 1.0F, 1.0F);
                        // Broadcast byte 6 to trigger vanilla smoke rejection particles
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

    // 3. Balance Ridden Speed (SRG m_245547_ is getRiddenSpeed)
    @Inject(method = "m_245547_", at = @At("HEAD"), cancellable = true)
    private void coraline$nerfBearSpeed(Player rider, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(0.2F);
    }

    // 4. Disable Item Pickup Mechanic completely
    @Inject(method = "canTargetItem", at = @At("HEAD"), cancellable = true)
    private void coraline$completelyDisableItemPickup(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    // 5. Cleanup the item thrower ID just in case
    @Inject(method = "onGetItem", at = @At("TAIL"))
    private void coraline$preventDropTaming(ItemEntity targetEntity, CallbackInfo ci) {
        this.salmonThrowerID = null;
    }

    // 6. Execute Camel-Style Dash in tickRidden (SRG m_274498_)
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

    // 7. Suppress Bear Hair Drops (SRG m_19998_ is spawnAtLocation(ItemLike))
    // Requirement 4: Intercepts fur drop when timeUntilNextFur hits 0; returns null so nothing drops!
    @Redirect(
            method = "m_8119_",
            at = @At(value = "INVOKE", target = "Lcom/github/alexthe666/alexsmobs/entity/EntityGrizzlyBear;m_19998_(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;")
    )
    private ItemEntity coraline$suppressFurShedding(EntityGrizzlyBear instance, ItemLike item) {
        return null;
    }

    // --- CAMEL DASH HELPER & PLAYERRIDEABLEJUMPING IMPLEMENTATION ---

    // Camel's own baseline attributes (see Camel.createAttributes()): 0.09 speed, 0.42 jump strength.
    // We deliberately do NOT use the bear's own (much higher) attributes here, since the
    // 22.2222 / 1.4285 dash constants were tuned around Camel's specific numbers. Instead we
    // clamp the effective inputs to just slightly above Camel's baseline so the bear dash reads
    // as "a bit punchier than a Camel," not an entirely different scale of movement.
    @Unique private static final double CORALINE_DASH_SPEED_CAP = 0.09D * 1.15D; // ~15% over Camel
    @Unique private static final double CORALINE_DASH_JUMP_CAP = 0.42D * 1.15D;  // ~15% over Camel

    @Unique
    private void coraline$executeBearDash(float scale) {
        EntityGrizzlyBear bear = (EntityGrizzlyBear)(Object)this;
        // Cast to our accessor interface to reach protected methods
        EntityAccessor accessor = (EntityAccessor)(Object)bear;

        double rawJumpStrength = bear.getAttributes().hasAttribute(Attributes.JUMP_STRENGTH)
                ? bear.getAttributeValue(Attributes.JUMP_STRENGTH)
                : 0.45D;
        double rawSpeed = bear.getAttributeValue(Attributes.MOVEMENT_SPEED);

        // Clamp to just above Camel's own baseline so the dash can't scale with the bear's
        // (much larger) native attributes.
        double jumpStrength = Math.min(rawJumpStrength, CORALINE_DASH_JUMP_CAP);
        double speedBase = Math.min(rawSpeed, CORALINE_DASH_SPEED_CAP);

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
            if (power >= 90) {
                this.coraline$playerJumpPendingScale = 1.0F;
            } else {
                this.coraline$playerJumpPendingScale = 0.4F + 0.4F * (float)power / 90.0F;
            }
        }
    }

    @Override
    public boolean canJump() {
        return ((EntityGrizzlyBear)(Object)this).isHoneyed();
    }

    @Override
    public void handleStartJump(int jumpPower) {
        ((EntityGrizzlyBear)(Object)this).playSound(SoundEvents.CAMEL_DASH, 1.0F, 1.0F);
    } // TODO: Change to Bear custom

    @Override
    public void handleStopJump() {
    }

    @Override
    public int getJumpCooldown() {
        return this.coraline$dashCooldown;
    }
}