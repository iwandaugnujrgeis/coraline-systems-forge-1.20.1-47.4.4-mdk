package net.zharok01.coralinesystems.mixin.client;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererNameTagMixin {

    @Unique
    private static final float NAME_TAG_DISTANCE = 128.0F;

    @ModifyConstant(
            method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;)Z",
            constant = @Constant(floatValue = 64.0F)
    )
    private float coraline$extendNameTagDistance(float vanillaDistance) {
        return NAME_TAG_DISTANCE;
    }

    @ModifyConstant(
            method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;)Z",
            constant = @Constant(floatValue = 32.0F)
    )
    private float coraline$extendDiscreteNameTagDistance(float vanillaDistance) {
        return NAME_TAG_DISTANCE;
    }
}