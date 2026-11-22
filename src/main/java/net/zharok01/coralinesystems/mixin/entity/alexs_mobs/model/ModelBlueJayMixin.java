package net.zharok01.coralinesystems.mixin.entity.alexs_mobs.model;

import com.github.alexthe666.alexsmobs.client.model.ModelBlueJay;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ModelBlueJay.class, remap = false)
public class ModelBlueJayMixin {

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityBlueJay;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityBlueJay;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelBlueJay;walk(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V",
                    ordinal = 7
            )
    )
    private void coraline$cancelIdleTailWag(ModelBlueJay instance, AdvancedModelBox box, float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityBlueJay;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityBlueJay;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelBlueJay;walk(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V",
                    ordinal = 8
            )
    )
    private void coraline$cancelIdleCrestWag(ModelBlueJay instance, AdvancedModelBox box, float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityBlueJay;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityBlueJay;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelBlueJay;progressRotationPrev(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFFFF)V",
                    ordinal = 7
            )
    )
    private void coraline$cancelCrestStateRotation(ModelBlueJay instance, AdvancedModelBox box, float progress, float rotX, float rotY, float rotZ, float maxProgress) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityBlueJay;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityBlueJay;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelBlueJay;bob(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFF)V",
                    ordinal = 0
            )
    )
    private void coraline$cancelFlapBodyBob(ModelBlueJay instance, AdvancedModelBox box, float speed, float degree, boolean bounce, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityBlueJay;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityBlueJay;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelBlueJay;bob(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFF)V",
                    ordinal = 1
            )
    )
    private void coraline$cancelFlapHeadBob(ModelBlueJay instance, AdvancedModelBox box, float speed, float degree, boolean bounce, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityBlueJay;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityBlueJay;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelBlueJay;bob(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFF)V",
                    ordinal = 2
            )
    )
    private void coraline$cancelWalkBodyBob(ModelBlueJay instance, AdvancedModelBox box, float speed, float degree, boolean bounce, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityBlueJay;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityBlueJay;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelBlueJay;bob(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFF)V",
                    ordinal = 3
            )
    )
    private void coraline$cancelIdleHeadBob(ModelBlueJay instance, AdvancedModelBox box, float speed, float degree, boolean bounce, float f, float f1) {
    }
}