package net.zharok01.coralinesystems.mixin;

import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.monster.Drowned;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Drowned.class)
public abstract class DrownedEquipmentMixin {

    @Inject(
            method = "populateDefaultEquipmentSlots",
            at = @At("HEAD"),
            cancellable = true
    )
    private void coraline$cancelDefaultEquipment(RandomSource random, DifficultyInstance difficulty, CallbackInfo ci) {
        ci.cancel();
    }
}