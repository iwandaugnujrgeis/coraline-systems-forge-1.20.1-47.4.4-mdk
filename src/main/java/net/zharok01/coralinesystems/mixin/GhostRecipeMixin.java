package net.zharok01.coralinesystems.mixin;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.GhostRecipe;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import net.zharok01.coralinesystems.client.gui.recipe_book.RecipeBookLayout;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GhostRecipe.class)
public abstract class GhostRecipeMixin {
    @Shadow @Final private List<GhostRecipe.GhostIngredient> ingredients;
    @Shadow float time;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void coraline$renderFadedGhost(GuiGraphics guiGraphics, Minecraft minecraft, int leftPos, int topPos, boolean bl, float partialTick, CallbackInfo ci) {
        if (!Screen.hasControlDown()) {
            this.time += partialTick;
        }

        for (int i = 0; i < this.ingredients.size(); i++) {
            GhostRecipe.GhostIngredient ingredient = this.ingredients.get(i);
            int x = ingredient.getX() + leftPos;
            int y = ingredient.getY() + topPos;
            ItemStack itemStack = ingredient.getItem();

            guiGraphics.renderFakeItem(itemStack, x, y);
            guiGraphics.fill(RenderType.guiGhostRecipeOverlay(), x, y, x + 16, y + 16, RecipeBookLayout.UNCRAFTABLE_OVERLAY_COLOR);
            if (i == 0) {
                guiGraphics.renderItemDecorations(minecraft.font, itemStack, x, y);
            }
        }

        ci.cancel();
    }
}