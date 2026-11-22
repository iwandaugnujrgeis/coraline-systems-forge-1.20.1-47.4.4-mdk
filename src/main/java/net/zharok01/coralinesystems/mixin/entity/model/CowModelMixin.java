package net.zharok01.coralinesystems.mixin.entity.model;

import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.zharok01.coralinesystems.util.interfaces.CowEatAnimationDuck;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(QuadrupedModel.class)
public class CowModelMixin<T extends Entity> {

    @Final
    @Shadow
    protected ModelPart head;

    @Inject(method = "setupAnim", at = @At("RETURN"))
    private void coraline$applyEatAnimation(T entity, float limbSwing, float limbSwingAmount,
                                            float ageInTicks, float netHeadYaw, float headPitch,
                                            CallbackInfo ci) {
        if (entity instanceof CowEatAnimationDuck duck) {
            float posScale = Math.max(0.0F, duck.coraline$getHeadEatPositionScale(0.0F));
            float angleScale = duck.coraline$getHeadEatAngleScale(0.0F);

            if (posScale > 0.0F || angleScale > 0.0F) {
                this.head.y = 4.0F + posScale * 9.0F;
                this.head.xRot = angleScale;
            } else {
                this.head.y = 4.0F;
            }
        }
    }
}