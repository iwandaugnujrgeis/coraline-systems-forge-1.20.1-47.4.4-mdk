package net.zharok01.coralinesystems.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.zharok01.coralinesystems.world.CoralineTrueDarkness;
import net.zharok01.coralinesystems.util.interfaces.DarkeningTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Darkens the 16x16 light-map texture in-place before it uploads to the GPU, if this
 * particular DynamicTexture instance was flagged by {@code LightTextureMixin}.
 */
@Mixin(DynamicTexture.class)
public abstract class DynamicTextureMixin implements DarkeningTexture {

    @Shadow
    @javax.annotation.Nullable
    private NativeImage pixels;

    @Unique
    private boolean coralineSystems$darkeningEnabled = false;

    @Override
    @Unique
    public void coralineSystems$enableDarkening() {
        this.coralineSystems$darkeningEnabled = true;
    }

    @Override
    @Unique
    public boolean coralineSystems$isDarkeningEnabled() {
        return this.coralineSystems$darkeningEnabled;
    }

    @Inject(method = "upload", at = @At("HEAD"))
    private void coralineSystems$darkenBeforeUpload(CallbackInfo ci) {
        if (!this.coralineSystems$darkeningEnabled || !CoralineTrueDarkness.enabled || this.pixels == null) {
            return;
        }

        NativeImage image = this.pixels;
        int width = image.getWidth();
        int height = image.getHeight();

        for (int skyIndex = 0; skyIndex < height; skyIndex++) {
            for (int blockIndex = 0; blockIndex < width; blockIndex++) {
                int color = image.getPixelRGBA(blockIndex, skyIndex);
                image.setPixelRGBA(blockIndex, skyIndex, CoralineTrueDarkness.darken(color, blockIndex, skyIndex));
            }
        }
    }
}
