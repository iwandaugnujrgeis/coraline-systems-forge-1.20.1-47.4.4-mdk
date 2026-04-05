package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Giant;
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
    private void coraline$giantLegKick(T entity, float ageInTicks, CallbackInfo ci) {
        if (!(entity instanceof Giant)) return;

        float attackTime = entity.getAttackAnim(1.0F);
        if (attackTime <= 0.0F) return;

        // Switched from quartic (x^4) to quadratic (x^2) ease-in.
        // Quadratic is gentler — the kick accelerates more slowly so it
        // feels heavier and less snappy.
        float f = attackTime;
        f = 1.0F - f;
        f *= f;         // x^2 instead of x^4
        f = 1.0F - f;

        // Sine arc over the full swing window — unchanged, still peaks at
        // mid-swing and cleanly returns to 0 at the end.
        float arc = Mth.sin(f * (float) Math.PI);

        // Kick height halved: was 2.4 (~138°), now 1.2 (~69°).
        // Still clearly visible on a Giant but much more grounded.
        this.rightLeg.xRot -= arc * 1.1F;

        // Outward splay scaled down proportionally.
        this.rightLeg.zRot += arc * 0.08F;

        // Supporting leg bend scaled down proportionally.
        this.leftLeg.xRot += arc * 0.15F;

        // Body lean scaled down proportionally.
        this.body.xRot += arc * 0.2F;

        ci.cancel();
    }
}