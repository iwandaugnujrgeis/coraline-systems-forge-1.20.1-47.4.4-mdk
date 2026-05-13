package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(DefaultPlayerSkin.class)
public class DefaultPlayerSkinMixin {

    @Unique
    private static final ResourceLocation CORALINE_FALLBACK_SKIN =
            new ResourceLocation("coraline_systems", "textures/entity/player/fallback.png");

    // 1. Block the UUID texture lookup
    @Inject(method = "getDefaultSkin(Ljava/util/UUID;)Lnet/minecraft/resources/ResourceLocation;", at = @At("HEAD"), cancellable = true)
    private static void forceUUIDSkin(UUID playerUUID, CallbackInfoReturnable<ResourceLocation> cir) {
        cir.setReturnValue(CORALINE_FALLBACK_SKIN);
    }

    // 2. Block the no-argument texture lookup (This is what likely caught you!)
    @Inject(method = "getDefaultSkin()Lnet/minecraft/resources/ResourceLocation;", at = @At("HEAD"), cancellable = true)
    private static void forceNoArgsSkin(CallbackInfoReturnable<ResourceLocation> cir) {
        cir.setReturnValue(CORALINE_FALLBACK_SKIN);
    }

    // 3. Force the wide arm model for both
    @Inject(method = "getSkinModelName(Ljava/util/UUID;)Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
    private static void forceWideArms(UUID playerUUID, CallbackInfoReturnable<String> cir) {
        cir.setReturnValue("default");
    }
}