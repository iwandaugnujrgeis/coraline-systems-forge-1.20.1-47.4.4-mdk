package net.zharok01.coralinesystems.mixin;

import net.mehvahdjukaar.supplementaries.common.items.crafting.SpecialRecipeDisplays;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Mixin(value = SpecialRecipeDisplays.class, remap = false)
public class SpecialRecipeDisplaysMixin {

    @Inject(
            method = "createSoapCleanRecipe",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void cancelSoapCleanRecipe(
            CallbackInfoReturnable<List<CraftingRecipe>> cir) {
        cir.setReturnValue(Collections.emptyList());
    }
}