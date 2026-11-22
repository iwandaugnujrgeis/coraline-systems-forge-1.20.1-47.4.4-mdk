package net.zharok01.coralinesystems.mixin;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.zharok01.coralinesystems.client.entity.skin.ClientSkinOverrideCache;
import net.zharok01.coralinesystems.client.entity.skin.SkinVariant;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerClientSkinMixin {

    @Inject(method = "getHurtSound", at = @At("HEAD"), cancellable = true)
    private void coraline$overrideHurtSound(DamageSource damageSource, CallbackInfoReturnable<SoundEvent> cir) {
        Player player = (Player) (Object) this;
        SkinVariant variant = ClientSkinOverrideCache.get(player.getUUID());

        if (variant == SkinVariant.STEVE || variant == SkinVariant.GREEN_STEVE) {
            cir.setReturnValue(CoralineSounds.STEVE_HURT.get());
        }
    }
}
