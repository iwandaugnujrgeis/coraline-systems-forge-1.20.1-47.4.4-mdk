package net.zharok01.coralinesystems.mixin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.zharok01.coralinesystems.registry.CoralineRarity;
import net.zharok01.coralinesystems.event.RarityTagHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemRarityMixin {

    @Inject(method = "getRarity", at = @At("HEAD"), cancellable = true)
    private void coraline$overrideRarity(ItemStack stack, CallbackInfoReturnable<Rarity> cir) {
        Rarity override = RarityTagHandler.getOverrides().get((Item) (Object) this);
        if (override == null) return;

        if (!stack.isEnchanted()) {
            cir.setReturnValue(override);
            return;
        }

        cir.setReturnValue(coraline$bumpRarity(override));
    }

    @Unique
    private static Rarity coraline$bumpRarity(Rarity base) {
        if (base == Rarity.COMMON || base == Rarity.UNCOMMON) return Rarity.RARE;
        if (base == CoralineRarity.SPECIAL) return CoralineRarity.LEGENDARY;
        if (base == Rarity.RARE || base == CoralineRarity.LEGENDARY) return Rarity.EPIC;
        return base;
    }
}