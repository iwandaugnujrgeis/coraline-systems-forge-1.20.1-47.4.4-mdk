package net.zharok01.coralinesystems.mixin;

import net.minecraft.world.item.ItemStack;
import net.zharok01.coralinesystems.registry.CoralineItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.inventory.BrewingStandMenu$FuelSlot")
public class FuelSlotMixin {

    @Inject(method = "mayPlaceItem", at = @At("HEAD"), cancellable = true)
    private static void coralineSystems$replaceFuelItem(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(itemStack.is(CoralineItems.TIMEDUST.get()));
    }
}