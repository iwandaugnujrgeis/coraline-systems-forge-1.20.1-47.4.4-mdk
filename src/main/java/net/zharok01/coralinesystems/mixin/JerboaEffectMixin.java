package net.zharok01.coralinesystems.mixin;

import com.github.alexthe666.alexsmobs.entity.EntityJerboa;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = EntityJerboa.class, remap = false)
public class JerboaEffectMixin {

    @Redirect(
            method = "Lcom/github/alexthe666/alexsmobs/entity/EntityJerboa;m_6071_(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/world/effect/MobEffectInstance"
            )
    )
    private net.minecraft.world.effect.MobEffectInstance coraline$changeFleetFootedDuration(
            net.minecraft.world.effect.MobEffect effect,
            int originalDuration
    ) {
        return new net.minecraft.world.effect.MobEffectInstance(
                com.github.alexthe666.alexsmobs.effect.AMEffectRegistry.FLEET_FOOTED.get(),
                200
        );
    }
}