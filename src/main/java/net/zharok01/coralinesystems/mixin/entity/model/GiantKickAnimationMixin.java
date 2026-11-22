package net.zharok01.coralinesystems.mixin.entity.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Giant;
import net.zharok01.coralinesystems.entity.GiantAttackCooldownAccess;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(HumanoidModel.class)
public abstract class GiantKickAnimationMixin<T extends LivingEntity> {

    @Final @Shadow public ModelPart rightLeg;
    @Final @Shadow public ModelPart leftLeg;
    @Final @Shadow public ModelPart rightArm;
    @Final @Shadow public ModelPart leftArm;
    @Final @Shadow public ModelPart body;

    @Inject(method = "setupAttackAnimation", at = @At("HEAD"), cancellable = true)
    private void coraline$giantLegKick(T livingEntity, float ageInTicks, CallbackInfo ci) {
        if (!(livingEntity instanceof Giant giant)
                || ((GiantAttackCooldownAccess) giant).coraline$isKickAnimationBlocked()) return;

        float attackTime = livingEntity.getAttackAnim(1.0F);
        if (attackTime <= 0.0F) return;

        float f = attackTime;
        f = 1.0F - f;
        f *= f;
        f = 1.0F - f;

        float arc = Mth.sin(f * (float) Math.PI);

        this.rightLeg.xRot -= arc * 1.1F;
        this.rightLeg.zRot += arc * 0.08F;
        this.leftLeg.xRot += arc * 0.15F;
        this.body.xRot += arc * 0.2F;

        ci.cancel();
    }
}