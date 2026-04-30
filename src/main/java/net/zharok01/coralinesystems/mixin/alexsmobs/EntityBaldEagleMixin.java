package net.zharok01.coralinesystems.mixin.alexsmobs;

import com.github.alexthe666.alexsmobs.entity.EntityBaldEagle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = EntityBaldEagle.class, remap = false)
public class EntityBaldEagleMixin {

    @Redirect(
            method = "Lcom/github/alexthe666/alexsmobs/entity/EntityBaldEagle;m_6071_(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
            at = @At(value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/entity/EntityBaldEagle;setCommand(I)V")
    )
    private void coraline$skipWandering(EntityBaldEagle instance, int command) {
        instance.setCommand(command == 0 ? 1 : command);
    }
}
