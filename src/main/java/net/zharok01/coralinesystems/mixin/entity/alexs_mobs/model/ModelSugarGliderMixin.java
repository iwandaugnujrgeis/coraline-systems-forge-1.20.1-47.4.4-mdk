package net.zharok01.coralinesystems.mixin.entity.alexs_mobs.model;

import com.github.alexthe666.alexsmobs.client.model.ModelSugarGlider;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ModelSugarGlider.class, remap = false)
public class ModelSugarGliderMixin {

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelSugarGlider;flap(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V",
                    ordinal = 0
            )
    )
    private void coraline$cancelIdleRightEarFlap(ModelSugarGlider instance, AdvancedModelBox box, float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelSugarGlider;flap(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V",
                    ordinal = 1
            )
    )
    private void coraline$cancelIdleLeftEarFlap(ModelSugarGlider instance, AdvancedModelBox box, float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelSugarGlider;swing(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V",
                    ordinal = 0
            )
    )
    private void coraline$replaceLeftArmSwingWithWalk(ModelSugarGlider instance, AdvancedModelBox box, float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
        instance.walk(box, speed, degree, false, offset, weight, f, f1);
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelSugarGlider;swing(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V",
                    ordinal = 1
            )
    )
    private void coraline$replaceLeftLegSwingWithWalk(ModelSugarGlider instance, AdvancedModelBox box, float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
        instance.walk(box, speed, degree, true, offset, weight, f, f1);
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelSugarGlider;swing(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V",
                    ordinal = 2
            )
    )
    private void coraline$replaceRightArmSwingWithWalk(ModelSugarGlider instance, AdvancedModelBox box, float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
        instance.walk(box, speed, degree, true, offset, weight, f, f1);
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelSugarGlider;swing(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V",
                    ordinal = 3
            )
    )
    private void coraline$replaceRightLegSwingWithWalk(ModelSugarGlider instance, AdvancedModelBox box, float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
        instance.walk(box, speed, degree, false, offset, weight, f, f1);
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelSugarGlider;swing(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V",
                    ordinal = 4
            )
    )
    private void coraline$cancelTailWalkSwing(ModelSugarGlider instance, AdvancedModelBox box, float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelSugarGlider;bob(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFF)V",
                    ordinal = 0
            )
    )
    private void coraline$cancelHeadWalkBob(ModelSugarGlider instance, AdvancedModelBox box, float speed, float degree, boolean bounce, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelSugarGlider;flap(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V",
                    ordinal = 2
            )
    )
    private void coraline$cancelLeftArmGlideFlap(ModelSugarGlider instance, AdvancedModelBox box, float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelSugarGlider;flap(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V",
                    ordinal = 3
            )
    )
    private void coraline$cancelLeftLegGlideFlap(ModelSugarGlider instance, AdvancedModelBox box, float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelSugarGlider;flap(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V",
                    ordinal = 4
            )
    )
    private void coraline$cancelRightArmGlideFlap(ModelSugarGlider instance, AdvancedModelBox box, float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelSugarGlider;flap(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V",
                    ordinal = 5
            )
    )
    private void coraline$cancelRightLegGlideFlap(ModelSugarGlider instance, AdvancedModelBox box, float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelSugarGlider;swing(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V",
                    ordinal = 5
            )
    )
    private void coraline$cancelHeadGlideSwing(ModelSugarGlider instance, AdvancedModelBox box, float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelSugarGlider;swing(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V",
                    ordinal = 6
            )
    )
    private void coraline$cancelBodyGlideSwing(ModelSugarGlider instance, AdvancedModelBox box, float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelSugarGlider;swing(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V",
                    ordinal = 7
            )
    )
    private void coraline$cancelTailGlideSwing(ModelSugarGlider instance, AdvancedModelBox box, float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelSugarGlider;bob(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFF)V",
                    ordinal = 1
            )
    )
    private void coraline$cancelHeadForageBob(ModelSugarGlider instance, AdvancedModelBox box, float speed, float degree, boolean bounce, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelSugarGlider;swing(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V",
                    ordinal = 8
            )
    )
    private void coraline$cancelHeadForageSwing(ModelSugarGlider instance, AdvancedModelBox box, float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
    }
}