package net.zharok01.coralinesystems.mixin.accessors;

import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LightTexture.class)
public interface LightTextureAccessor {

    @Accessor("blockLightRedFlicker")
    float coralineSystems$getBlockLightRedFlicker();

    @Accessor("updateLightTexture")
    boolean coralineSystems$isLightTextureDirty();
}
