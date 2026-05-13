package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.zharok01.coralinesystems.util.CowEatAnimationDuck;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(QuadrupedModel.class)
public class CowModelMixin<T extends Entity> {

    // head is defined directly in QuadrupedModel — resolves cleanly here.
    @Final
    @Shadow
    protected ModelPart head;

    /**
     * setupAnim is overridden in QuadrupedModel, so Mixin finds it here.
     *
     * We apply both the head Y position (lowering to eat) and the head X rotation
     * (angling down) in a single inject at RETURN, after vanilla has already set
     * its own head pose — so the eat animation always wins when active.
     *
     * Note: setupAnim doesn't receive partialTick, so we pass 0.0F to the scale
     * methods. The animation runs over 40 ticks with gentle curves, so the missing
     * sub-tick interpolation is imperceptible in practice.
     *
     * The instanceof guard keeps this surgical — Sheep, Pig, and other
     * QuadrupedModel users are completely unaffected.
     */
    @Inject(method = "setupAnim", at = @At("RETURN"))
    private void coraline$applyEatAnimation(T entity, float limbSwing, float limbSwingAmount,
                                            float ageInTicks, float netHeadYaw, float headPitch,
                                            CallbackInfo ci) {
        if (entity instanceof CowEatAnimationDuck duck) {
            // Math.max clamps the negative "pre-dip" phase to zero so the head
            // starts at rest and lowers smoothly, without the upward flick.
            float posScale = Math.max(0.0F, duck.coraline$getHeadEatPositionScale(0.0F));
            float angleScale = duck.coraline$getHeadEatAngleScale(0.0F);
            if (posScale > 0.0F || angleScale > 0.0F) {
                this.head.y = 4.0F + posScale * 9.0F;
                this.head.xRot = angleScale;
            }
        }
    }
}