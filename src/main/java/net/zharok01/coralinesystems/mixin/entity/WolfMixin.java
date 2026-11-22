package net.zharok01.coralinesystems.mixin.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zharok01.coralinesystems.entity.ai.WolfEatItemsGoal;
import net.zharok01.coralinesystems.item.CoralineNameTagItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Wolf.class)
public abstract class WolfMixin extends TamableAnimal {

    protected WolfMixin(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void coraline$applyNameTagBeforeSitCommand(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (this.isTame() && itemStack.hasCustomHoverName() && itemStack.getItem() instanceof CoralineNameTagItem coralineNameTag) {
            cir.setReturnValue(coralineNameTag.interactLivingEntity(itemStack, player, this, hand));
        }
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void coraline$addEatItemsGoal(CallbackInfo ci) {
        this.goalSelector.addGoal(6, new WolfEatItemsGoal((Wolf) (Object) this));
    }

    @Inject(
            method = "mobInteract",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/animal/Wolf;setCollarColor(Lnet/minecraft/world/item/DyeColor;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void coraline$playCollarDyeSound(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        this.level().playSound(null, this, SoundEvents.DYE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}