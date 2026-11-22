package net.zharok01.coralinesystems.mixin.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.zharok01.coralinesystems.client.gui.TabIconCycle;
import net.zharok01.coralinesystems.client.gui.recipe_book.RecipeBookLayout;
import net.zharok01.coralinesystems.client.gui.recipe_book.RecipeTabRowAccess;
import net.zharok01.coralinesystems.client.gui.recipe_book.RecipeTabStyle;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeBookTabButton.class)
public abstract class RecipeBookTabButtonMixin extends StateSwitchingButton implements RecipeTabRowAccess {
    @Shadow @Final private RecipeBookCategories category;
    @Shadow private float animationTime;

    @Unique private TabIconCycle coraline$iconCycle = new TabIconCycle();
    @Unique private int coraline$spriteColumn = RecipeTabStyle.SPRITE_COLUMN_MIDDLE;
    @Unique private boolean coraline$behindPanelPass;

    protected RecipeBookTabButtonMixin() {
        super(0, 0, 0, 0, false);
    }

    @Override
    public void coraline$setSpriteColumn(int spriteColumn) {
        this.coraline$spriteColumn = spriteColumn;
    }

    @Override
    public void coraline$setBehindPanelPass(boolean behindPanelPass) {
        this.coraline$behindPanelPass = behindPanelPass;
    }

    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    private void coraline$skipOtherPass(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (this.isStateTriggered == this.coraline$behindPanelPass) {
            ci.cancel();
        }
    }

    @Redirect(method = "renderWidget", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"))
    private void coraline$keepTabFrameStill(PoseStack poseStack, float x, float y, float z) {
    }

    @Redirect(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"))
    private void coraline$drawCreativeStyleTab(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int u, int v, int width, int height) {
        int spriteU = RecipeTabStyle.SPRITE_CELL_WIDTH * this.coraline$spriteColumn;
        int spriteV = this.isStateTriggered ? RecipeTabStyle.SPRITE_V_SELECTED : RecipeTabStyle.SPRITE_V_UNSELECTED;
        guiGraphics.blit(RecipeTabStyle.CREATIVE_TABS_TEXTURE, this.getX(), this.getY(), spriteU, spriteV, RecipeTabStyle.TAB_WIDTH, RecipeTabStyle.TAB_HEIGHT);
    }

    @Inject(method = "renderIcon", at = @At("HEAD"), cancellable = true)
    private void coraline$renderCyclingIcon(GuiGraphics guiGraphics, ItemRenderer itemRenderer, CallbackInfo ci) {
        if (!RecipeTabStyle.isAllRecipesTab(this.category) && this.coraline$iconCycle.isDue()) {
            this.coraline$iconCycle.showOneOf(this.coraline$collectUnlockedResults());
        }

        ItemStack shown = this.coraline$iconCycle.shown();
        ItemStack iconStack = shown.isEmpty() ? this.category.getIconItems().get(0) : shown;
        int iconX = this.getX() + RecipeTabStyle.ICON_OFFSET_X;
        int iconY = this.getY() + RecipeTabStyle.ICON_OFFSET_Y;

        float iconScale = this.animationTime > 0.0F
                ? 1.0F + RecipeBookLayout.HIGHLIGHT_ICON_POP * (float) Math.sin(this.animationTime / RecipeBookLayout.HIGHLIGHT_BOUNCE_TICKS * (float) Math.PI)
                : 1.0F;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(iconX + 8, iconY + 8, 0.0F);
        guiGraphics.pose().scale(iconScale, iconScale, 1.0F);
        guiGraphics.pose().translate(-(iconX + 8), -(iconY + 8), 0.0F);
        guiGraphics.renderFakeItem(iconStack, iconX, iconY);
        guiGraphics.pose().popPose();
        ci.cancel();
    }

    @Unique
    private List<ItemStack> coraline$collectUnlockedResults() {
        assert Minecraft.getInstance().player != null;
        ClientRecipeBook recipeBook = Minecraft.getInstance().player.getRecipeBook();
        List<ItemStack> results = new ArrayList<>();

        for (RecipeCollection collection : recipeBook.getCollection(this.category)) {
            if (collection.hasFitting()) {
                for (Recipe<?> recipe : collection.getRecipes()) {
                    if (recipeBook.contains(recipe)) {
                        ItemStack result = recipe.getResultItem(collection.registryAccess());
                        if (!result.isEmpty()) {
                            results.add(result);
                        }
                    }
                }
            }
        }

        return results;
    }
}