package net.zharok01.coralinesystems.mixin.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.zharok01.coralinesystems.registry.CoralineTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Cat.class)
public class CatMixin {

    @Inject(method = "isFood", at = @At("HEAD"), cancellable = true)
    private void coraline$isFood(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.is(CoralineTags.CAT_FOOD_ITEMS)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = "mobInteract",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/animal/Cat;setCollarColor(Lnet/minecraft/world/item/DyeColor;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void coraline$playCollarDyeSound(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        Cat cat = (Cat) (Object) this;
        cat.level().playSound(null, cat, SoundEvents.DYE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}