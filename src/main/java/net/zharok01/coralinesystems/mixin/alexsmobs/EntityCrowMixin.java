package net.zharok01.coralinesystems.mixin.alexsmobs;

import com.github.alexthe666.alexsmobs.entity.EntityCrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = EntityCrow.class, remap = false)
public class EntityCrowMixin {

    @Redirect(
            method = "Lcom/github/alexthe666/alexsmobs/entity/EntityCrow;m_6071_(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
            at = @At(value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/entity/EntityCrow;setCommand(I)V")
    )
    private void coraline$skipWandering(EntityCrow instance, int command) {
        // Crow has 4 states: 0=wandering, 1=follow, 2=sit, 3=perch.
        // We skip 0 the same way — if the cycle wraps to wandering, redirect to follow.
        instance.setCommand(command == 0 ? 1 : command);
    }
}
