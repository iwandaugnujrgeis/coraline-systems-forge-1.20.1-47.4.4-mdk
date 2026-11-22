package net.zharok01.coralinesystems.mixin;

import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Monster.class)
public abstract class SleepCheckMixin {

    @Inject(method = "isPreventingPlayerRest", at = @At("HEAD"), cancellable = true)
    private void allowSleepNearMonsters(Player player, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}