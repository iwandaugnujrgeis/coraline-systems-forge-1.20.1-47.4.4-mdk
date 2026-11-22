package net.zharok01.coralinesystems.mixin.entity;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.zharok01.coralinesystems.util.interfaces.CowMilkDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MushroomCow.class)
public abstract class MushroomCowMixin {

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void coraline$limitStewMilking(Player player, InteractionHand hand,
                                           CallbackInfoReturnable<InteractionResult> cir) {
        MushroomCow mushroomCow = (MushroomCow) (Object) this;
        if (player.getItemInHand(hand).is(Items.BOWL) && !mushroomCow.isBaby()) {
            CowMilkDuck duck = (CowMilkDuck) (Object) this;
            if (!duck.coraline$canReceiveStewServing()) {
                cir.setReturnValue(InteractionResult.FAIL);
            } else if (!mushroomCow.level().isClientSide) {
                duck.coraline$recordStewServing();
            }
        }
    }
}
