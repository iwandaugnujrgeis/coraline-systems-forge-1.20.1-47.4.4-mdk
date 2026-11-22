package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.zharok01.coralinesystems.client.entity.skin.ClientSkinOverrideCache;
import net.zharok01.coralinesystems.client.entity.skin.SkinVariant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerSkinMixin {

    @Inject(method = "getSkinTextureLocation", at = @At("HEAD"), cancellable = true)
    private void coraline$overrideSkinTexture(CallbackInfoReturnable<ResourceLocation> cir) {
        AbstractClientPlayer self = (AbstractClientPlayer) (Object) this;
        SkinVariant variant = ClientSkinOverrideCache.get(self.getUUID());

        if (variant == null || variant.getTexture() == null) return;

        cir.setReturnValue(variant.getTexture());
    }

    @Inject(method = "getModelName", at = @At("HEAD"), cancellable = true)
    private void coraline$overrideModelName(CallbackInfoReturnable<String> cir) {
        AbstractClientPlayer self = (AbstractClientPlayer) (Object) this;
        SkinVariant variant = ClientSkinOverrideCache.get(self.getUUID());

        if (variant == null || variant.getTexture() == null) return;

        cir.setReturnValue(variant.getModelName());
    }
}