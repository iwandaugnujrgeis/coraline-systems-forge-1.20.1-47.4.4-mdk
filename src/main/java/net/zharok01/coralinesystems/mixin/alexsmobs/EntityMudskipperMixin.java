package net.zharok01.coralinesystems.mixin.alexsmobs;

import com.github.alexthe666.alexsmobs.entity.EntityMudskipper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = EntityMudskipper.class, remap = false)
public class EntityMudskipperMixin {

    @Redirect(
            method = "Lcom/github/alexthe666/alexsmobs/entity/EntityMudskipper;m_6071_(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
            at = @At(value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/entity/EntityMudskipper;setCommand(I)V")
    )
    private void coraline$skipWandering(EntityMudskipper instance, int command) {
        instance.setCommand(command == 0 ? 1 : command);
    }
}
