package net.zharok01.coralinesystems.mixin.entity.alexs_mobs.model;

import com.github.alexthe666.alexsmobs.client.model.ModelFroststalker;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ModelFroststalker.class, remap = false)
public class ModelFroststalkerMixin {

    @Final
    @Shadow
    private AdvancedModelBox horn;

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;addBox(FFFFFFFZ)V"
            )
    )
    private void coraline$resizeHornCube(AdvancedModelBox instance, float offX, float offY, float offZ, float width, float height, float depth, float scale, boolean mirror) {
        if (instance == this.horn) {
            instance.addBox(-0.5F, -1.5F, offZ, 1.0F, 1.0F, depth, scale, mirror);
            return;
        }
        instance.addBox(offX, offY, offZ, width, height, depth, scale, mirror);
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityFroststalker;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityFroststalker;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelFroststalker;chainSwing([Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFDFF)V",
                    ordinal = 0
            )
    )
    private void coraline$cancelIdleTailChainSwing(ModelFroststalker instance, AdvancedModelBox[] boxes, float speed, float degree, double rootOffset, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityFroststalker;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityFroststalker;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelFroststalker;walk(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V",
                    ordinal = 0
            )
    )
    private void coraline$cancelIdleNeckSwaying(ModelFroststalker instance, AdvancedModelBox box, float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityFroststalker;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityFroststalker;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelFroststalker;walk(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V",
                    ordinal = 1
            )
    )
    private void coraline$cancelIdleHeadSwaying(ModelFroststalker instance, AdvancedModelBox box, float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityFroststalker;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityFroststalker;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelFroststalker;bob(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFF)V",
                    ordinal = 0
            )
    )
    private void coraline$cancelBodyBob(ModelFroststalker instance, AdvancedModelBox box, float speed, float degree, boolean bounce, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityFroststalker;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityFroststalker;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelFroststalker;flap(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V",
                    ordinal = 0
            )
    )
    private void coraline$cancelNeckLateralFlap(ModelFroststalker instance, AdvancedModelBox box, float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityFroststalker;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityFroststalker;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelFroststalker;flap(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V",
                    ordinal = 1
            )
    )
    private void coraline$cancelHeadLateralFlap(ModelFroststalker instance, AdvancedModelBox box, float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
    }
}