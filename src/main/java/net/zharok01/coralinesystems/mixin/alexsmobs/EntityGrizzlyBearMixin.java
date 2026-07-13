package net.zharok01.coralinesystems.mixin.alexsmobs;

import com.github.alexthe666.alexsmobs.entity.EntityGrizzlyBear;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(value = EntityGrizzlyBear.class, remap = false)
public class EntityGrizzlyBearMixin {

    @Shadow private UUID salmonThrowerID;

    // 1. Skip Wandering Commands
    @Redirect(
            method = "m_6071_(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
            at = @At(value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/entity/EntityGrizzlyBear;setCommand(I)V")
    )
    private void coraline$skipWandering(EntityGrizzlyBear instance, int command) {
        instance.setCommand(command == 0 ? 1 : command);
    }

    // 2. Intercept Riding: Require Honey to mount
    // Note: We use SRG name m_20329_ for player.startRiding() to ensure it targets correctly with remap=false
    @Redirect(
            method = "m_6071_(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;m_20329_(Lnet/minecraft/world/entity/Entity;)Z")
    )
    private boolean coraline$requireHoneyToRide(Player player, Entity entity) {
        EntityGrizzlyBear bear = (EntityGrizzlyBear) entity;
        if (bear.isHoneyed()) {
            return player.startRiding(bear);
        }
        // Prevents mounting if not honeyed; the player will just swing their hand
        return false;
    }

    // 3. Balance Ridden Speed (0.11F is slightly faster than a Camel's 0.09F)
    // Method SRG m_245547_ is `getRiddenSpeed(Player rider)`
    @Inject(method = "m_245547_", at = @At("HEAD"), cancellable = true)
    private void coraline$nerfBearSpeed(Player rider, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(0.11F);
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
}