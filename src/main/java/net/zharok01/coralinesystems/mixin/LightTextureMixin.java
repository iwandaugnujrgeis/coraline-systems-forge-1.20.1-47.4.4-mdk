package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.zharok01.coralinesystems.util.interfaces.DarkeningTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Registers this LightTexture's backing {@link DynamicTexture} for darkening on upload.
 */
@Mixin(LightTexture.class)
public abstract class LightTextureMixin {

    @Shadow
    @Final
    private DynamicTexture lightTexture;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void coralineSystems$flagDarkeningTexture(GameRenderer renderer, Minecraft minecraft, CallbackInfo ci) {
        ((DarkeningTexture) this.lightTexture).coralineSystems$enableDarkening();
    }
}
