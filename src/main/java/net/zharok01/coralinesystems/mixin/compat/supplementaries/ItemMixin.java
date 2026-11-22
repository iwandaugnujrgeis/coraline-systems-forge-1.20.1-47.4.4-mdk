package net.zharok01.coralinesystems.mixin.compat.supplementaries;

import net.mehvahdjukaar.supplementaries.common.items.SackItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin {

    @Inject(method = "getRarity", at = @At("RETURN"), cancellable = true)
    private void coraline$colorFilledSacks(ItemStack stack, CallbackInfoReturnable<Rarity> cir) {
        if (cir.getReturnValue() == Rarity.COMMON && stack.getItem() instanceof SackItem && SackItem.getEncumber(stack) > 0.0F) {
            cir.setReturnValue(Rarity.RARE);
        }
    }
}