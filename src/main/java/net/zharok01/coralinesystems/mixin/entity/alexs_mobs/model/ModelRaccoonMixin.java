package net.zharok01.coralinesystems.mixin.entity.alexs_mobs.model;

import com.github.alexthe666.alexsmobs.client.model.ModelRaccoon;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ModelRaccoon.class, remap = false)
public class ModelRaccoonMixin {

    @Shadow public AdvancedModelBox tail;

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "setupAnim(Lcom/github/alexthe666/alexsmobs/entity/EntityRaccoon;FFFFF)V",
                    "m_6973_(Lcom/github/alexthe666/alexsmobs/entity/EntityRaccoon;FFFFF)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/client/model/ModelRaccoon;swing(Lcom/github/alexthe666/citadel/client/model/AdvancedModelBox;FFZFFFF)V"
            )
    )
    private void coraline$cancelAmbientTailSwing(ModelRaccoon instance, AdvancedModelBox box, float speed, float degree,
                                                 boolean invert, float offset, float weight, float swing, float swingAmount) {

        if (box == this.tail && offset == 2.0F && swingAmount == 1.0F) {
            return;
        }

        instance.swing(box, speed, degree, invert, offset, weight, swing, swingAmount);
    }
}