package net.zharok01.coralinesystems.mixin.alexsmobs;

import com.github.alexthe666.alexsmobs.entity.EntitySugarGlider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = EntitySugarGlider.class, remap = false)
public class EntitySugarGliderMixin {

    @Redirect(
            method = "Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;m_6071_(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
            at = @At(value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;setCommand(I)V")
    )
    private void coraline$skipWandering(EntitySugarGlider instance, int command) {
        instance.setCommand(command == 0 ? 1 : command);
    }
}
