package net.zharok01.coralinesystems.mixin.client.gui.screens;

import com.mojang.blaze3d.platform.Window;
import dev.isxander.controlify.api.vmousesnapping.SnapPoint;
import dev.isxander.controlify.controller.ControllerEntity;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.zharok01.coralinesystems.client.gui.compat.controlify.CoralineControlifyCompat;
import net.zharok01.coralinesystems.client.gui.recipe_book.RecipeArrowButton;
import net.zharok01.coralinesystems.client.gui.recipe_book.RecipeControllerAccess;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "dev.isxander.controlify.virtualmouse.VirtualMouseHandler", remap = false)
public abstract class VirtualMouseHandlerMixin {

    @Shadow
    private Set<SnapPoint> snapPoints;

    @Shadow
    public abstract void snapToPoint(SnapPoint snapPoint, Vector2dc scaleFactor);

    @Inject(method = "snapToClosestPoint", at = @At("HEAD"), require = 1)
    private void coraline$snapToBookOnStickRelease(CallbackInfo ci) {
        RecipeControllerAccess book = coraline$currentBook();
        if (book != null) {
            this.coraline$mergeBookPoints(book);
        }
    }

    @Inject(method = "snapInDirection", at = @At("HEAD"), cancellable = true, require = 1)
    private void coraline$navigateRecipeBook(ScreenDirection direction, CallbackInfo ci) {
        RecipeControllerAccess book = coraline$currentBook();
        if (book == null) {
            return;
        }

        this.coraline$mergeBookPoints(book);
        if (!book.coraline$isStripOpen()) {
            return;
        }

        ci.cancel();
        if (direction.getAxis() != ScreenAxis.VERTICAL) {
            return;
        }

        book.coraline$stepStrip(direction == ScreenDirection.UP ? -1 : 1);
        int[] center = book.coraline$collectSnapTargets().get(0);
        Window window = Minecraft.getInstance().getWindow();
        Vector2d scaleFactor = new Vector2d((double) window.getGuiScaledWidth() / window.getScreenWidth(), (double) window.getGuiScaledHeight() / window.getScreenHeight());
        this.snapToPoint(new SnapPoint(center[0], center[1], center[2]), scaleFactor);
    }

    @Inject(method = "handleControllerInput", at = @At("HEAD"), require = 1)
    private void coraline$toggleRecipeBook(ControllerEntity controller, CallbackInfo ci) {
        Screen screen = Minecraft.getInstance().screen;
        if (!(screen instanceof RecipeUpdateListener) || !CoralineControlifyCompat.coraline$toggleRecipeBook.on(controller).justPressed()) {
            return;
        }

        for (GuiEventListener child : screen.children()) {
            if (child instanceof RecipeArrowButton button) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                button.onPress();
                return;
            }
        }
    }

    @Unique
    private void coraline$mergeBookPoints(RecipeControllerAccess book) {
        Set<SnapPoint> merged = book.coraline$isStripOpen() ? new HashSet<>() : new HashSet<>(this.snapPoints);
        for (int[] target : book.coraline$collectSnapTargets()) {
            merged.add(new SnapPoint(target[0], target[1], target[2]));
        }
        this.snapPoints = merged;
    }

    @Unique
    private static RecipeControllerAccess coraline$currentBook() {
        return Minecraft.getInstance().screen instanceof RecipeUpdateListener listener ? (RecipeControllerAccess) listener.getRecipeBookComponent() : null;
    }
}