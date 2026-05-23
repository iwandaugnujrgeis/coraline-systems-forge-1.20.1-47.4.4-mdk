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

    /**
     * Cancels the dynamic generation of soap-cleaning recipe entries that
     * SpecialRecipeDisplays feeds into JEI / REI / EMI and the vanilla
     * recipe book.  Returning an empty list means no soap-clean display
     * recipes are ever registered, so they won't appear anywhere in any
     * recipe viewer.
     *
     * Note: this targets a private static method, which Mixin handles fine.
     * The descriptor ()Ljava/util/List; matches
     *   private static List<CraftingRecipe> createSoapCleanRecipe()
     */
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