package net.zharok01.coralinesystems.mixin.accessor;

import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LightTexture.class)
public interface LightTextureAccessor {

    @Accessor("blockLightRedFlicker")
    float coraline$getBlockLightRedFlicker();

    @Accessor("updateLightTexture")
    boolean coraline$isLightTextureDirty();

    @Accessor("blockLightRedFlicker")
    void coraline$setBlockLightRedFlicker(float value);
}
