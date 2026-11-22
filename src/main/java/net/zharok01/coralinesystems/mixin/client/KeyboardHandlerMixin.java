package net.zharok01.coralinesystems.mixin.client;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.zharok01.coralinesystems.registry.CoralineEffects;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {

    private static final int DEBUG_KEY_COPY_LOCATION = 67;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "handleDebugKeys", at = @At("HEAD"), cancellable = true)
    private void coraline$restrictCopyLocation(int key, CallbackInfoReturnable<Boolean> cir) {
        if (key != DEBUG_KEY_COPY_LOCATION) {
            return;
        }

        Player player = this.minecraft.player;
        boolean canCopyLocation = player.isCreative()
                || player.isSpectator()
                || player.hasEffect(CoralineEffects.CLARITY.get());

        if (!canCopyLocation) {
            cir.setReturnValue(true);
        }
    }
}