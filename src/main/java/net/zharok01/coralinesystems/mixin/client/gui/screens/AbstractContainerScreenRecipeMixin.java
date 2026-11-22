package net.zharok01.coralinesystems.mixin.client.gui.screens;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.zharok01.coralinesystems.client.gui.recipe_book.RecipeControllerAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenRecipeMixin {

    @Inject(method = "onClose", at = @At("HEAD"), cancellable = true)
    private void coraline$closeStripFirst(CallbackInfo ci) {
        if ((Object) this instanceof RecipeUpdateListener listener) {
            RecipeControllerAccess book = (RecipeControllerAccess) listener.getRecipeBookComponent();
            if (book.coraline$isStripOpen()) {
                book.coraline$closeStrip();
                ci.cancel();
            }
        }
    }
}