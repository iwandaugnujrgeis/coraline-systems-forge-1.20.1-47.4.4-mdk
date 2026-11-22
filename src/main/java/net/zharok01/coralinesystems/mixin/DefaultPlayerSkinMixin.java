package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.zharok01.coralinesystems.client.entity.skin.SkinVariant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(DefaultPlayerSkin.class)
public class DefaultPlayerSkinMixin {

    @Inject(method = "getDefaultSkin()Lnet/minecraft/resources/ResourceLocation;", at = @At("HEAD"), cancellable = true)
    private static void coraline$steveFallback(CallbackInfoReturnable<ResourceLocation> cir) {
        cir.setReturnValue(SkinVariant.STEVE.getTexture());
    }

    @Inject(method = "getDefaultSkin(Ljava/util/UUID;)Lnet/minecraft/resources/ResourceLocation;", at = @At("HEAD"), cancellable = true)
    private static void coraline$steveFallbackForPlayer(UUID playerUUID, CallbackInfoReturnable<ResourceLocation> cir) {
        cir.setReturnValue(SkinVariant.STEVE.getTexture());
    }

    @Inject(method = "getSkinModelName", at = @At("HEAD"), cancellable = true)
    private static void coraline$steveFallbackModel(UUID playerUUID, CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(SkinVariant.STEVE.getModelName());
    }
}