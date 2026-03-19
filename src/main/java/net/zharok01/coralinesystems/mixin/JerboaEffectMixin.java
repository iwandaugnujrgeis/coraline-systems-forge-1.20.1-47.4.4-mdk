package net.zharok01.coralinesystems.mixin;

import com.github.alexthe666.alexsmobs.entity.EntityJerboa;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = EntityJerboa.class, remap = false)
public class JerboaEffectMixin {

    @Unique private static final int FLEET_FOOTED_DURATION = 200;

    @ModifyArg(
            method = "mobInteract",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/effect/MobEffectInstance;<init>(Lnet/minecraft/world/effect/MobEffect;I)V"
            ),
            index = 1
    )
    private int changeFleetFootedDuration(int duration) {
        return FLEET_FOOTED_DURATION;
    }

}