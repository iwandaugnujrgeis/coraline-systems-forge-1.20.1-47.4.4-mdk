package net.zharok01.coralinesystems.mixin.alexsmobs;

import com.github.alexthe666.alexsmobs.entity.EntityRaccoon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = EntityRaccoon.class, remap = false)
public class EntityRaccoonMixin {

    @Redirect(
            method = "Lcom/github/alexthe666/alexsmobs/entity/EntityRaccoon;m_6071_(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
            at = @At(value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/entity/EntityRaccoon;setCommand(I)V")
    )
    private void coraline$skipWandering(EntityRaccoon instance, int command) {
        instance.setCommand(command == 0 ? 1 : command);
    }
}
