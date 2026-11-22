package net.zharok01.coralinesystems.mixin.compat.supplementaries;

import net.mehvahdjukaar.supplementaries.common.items.BombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.zharok01.coralinesystems.event.RarityTagHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BombItem.class)
public class BombItemRarityMixin {

    @Inject(method = "m_41460_", at = @At("HEAD"), cancellable = true, remap = false)
    private void coraline$overrideBombRarity(ItemStack stack, CallbackInfoReturnable<Rarity> cir) {
        Rarity override = RarityTagHandler.getOverrides()
                .get((Item) (Object) this);

        cir.setReturnValue(override != null ? override : Rarity.COMMON);
    }
}