package net.zharok01.coralinesystems.mixin;

import net.mehvahdjukaar.supplementaries.common.items.crafting.SoapClearRecipe;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SoapClearRecipe.class, remap = false)
public class SoapClearRecipeMixin {

    @Inject(
            method = "matches",
            at = @At("HEAD"),
            cancellable = true
    )
    public void matches(CraftingContainer craftingContainer, Level level,
                        CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(
            method = "assemble",
            at = @At("HEAD"),
            cancellable = true
    )
    public void assemble(CraftingContainer craftingContainer, RegistryAccess registryAccess,
                         CallbackInfoReturnable<ItemStack> cir) {
        cir.setReturnValue(ItemStack.EMPTY);
    }
}