package net.zharok01.coralinesystems.mixin.compat.rediscovered;

import com.legacy.rediscovered.item.util.QuiverData;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = QuiverData.class, remap = false)
public abstract class QuiverDataMixin {
    @Inject(method = "getFromChestplate", at = @At("HEAD"), cancellable = true)
    private static void coraline$disableRediscoveredQuiver(ItemStack chestplate, CallbackInfoReturnable<Optional<QuiverData>> callback) {
        callback.setReturnValue(Optional.empty());
    }
}