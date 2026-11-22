package net.zharok01.coralinesystems.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.zharok01.coralinesystems.worldgen.CoralineTrueDarkness;
import net.zharok01.coralinesystems.util.interfaces.DarkeningTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DynamicTexture.class)
public abstract class DynamicTextureMixin implements DarkeningTexture {

    @Shadow
    @javax.annotation.Nullable
    private NativeImage pixels;

    @Unique
    private boolean coraline$darkeningEnabled = false;

    @Override
    @Unique
    public void coraline$enableDarkening() {
        this.coraline$darkeningEnabled = true;
    }

    @Override
    @Unique
    public boolean coraline$isDarkeningEnabled() {
        return this.coraline$darkeningEnabled;
    }

    @Inject(method = "upload", at = @At("HEAD"))
    private void coraline$darkenBeforeUpload(CallbackInfo ci) {
        if (!this.coraline$darkeningEnabled || !CoralineTrueDarkness.enabled || this.pixels == null) {
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
