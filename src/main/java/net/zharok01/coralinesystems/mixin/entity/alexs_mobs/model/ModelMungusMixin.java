package net.zharok01.coralinesystems.mixin.entity.alexs_mobs.model;

import com.github.alexthe666.alexsmobs.client.model.ModelMungus;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ModelMungus.class, remap = false)
public class ModelMungusMixin {

    @Final @Shadow public AdvancedModelBox body;
    @Final @Shadow public AdvancedModelBox hair;
    @Final @Shadow public AdvancedModelBox nose;
    @Final @Shadow public AdvancedModelBox leg_left;
    @Final @Shadow public AdvancedModelBox leg_right;

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityMungus;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityMungus;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelMungus;walk(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V"
            )
    )
    private void coraline$filterWalk(ModelMungus instance, AdvancedModelBox box, float speed, float degree,
                                     boolean invert, float offset, float weight, float walk, float walkAmount) {
        if (box == this.leg_left || box == this.leg_right) {
            instance.walk(box, speed, degree * 1.5F, invert, offset, weight, walk, walkAmount);
        }
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityMungus;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityMungus;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelMungus;flap(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V"
            )
    )
    private void coraline$cancelAllFlaps(ModelMungus instance, AdvancedModelBox box, float speed, float degree,
                                         boolean invert, float offset, float weight, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityMungus;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityMungus;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelMungus;bob(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFF)V"
            )
    )
    private void coraline$cancelAllBobbing(ModelMungus instance, AdvancedModelBox box, float speed, float degree,
                                           boolean bounce, float f, float f1) {
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityMungus;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityMungus;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelMungus;progressRotationPrev(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFFFF)V"
            )
    )
    private void coraline$cancelHairLean(ModelMungus instance, AdvancedModelBox box, float progress,
                                         float rotX, float rotY, float rotZ, float divisor) {
    }
}