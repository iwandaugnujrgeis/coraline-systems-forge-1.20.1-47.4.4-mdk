package net.zharok01.coralinesystems.mixin.entity.alexs_mobs;

import com.github.alexthe666.alexsmobs.entity.EntityMoose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityMoose.class, remap = false)
public abstract class EntityMooseMixin {

    @Shadow public abstract boolean isAntlered();
    @Shadow public abstract void setAntlered(boolean anters);

    @Inject(method = "m_8119_()V", at = @At("HEAD"))
    private void coraline$ensureAntlered(CallbackInfo ci) {
        if (!this.isAntlered()) {
            this.setAntlered(true);
        }
    }

    @Redirect(
            method = "m_8119_()V",
            at = @At(value = "INVOKE", target = "Lcom/github/alexthe666/alexsmobs/entity/EntityMoose;setAntlered(Z)V")
    )
    private void coraline$preventAntlerRemoval(EntityMoose instance, boolean antlered) {
        if (antlered) {
            instance.setAntlered(true);
        }
    }
}