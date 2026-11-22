package net.zharok01.coralinesystems.client.gui.compat.controlify;

import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import javax.annotation.Nullable;
import mod.icanttellyou.picturemode.client.PictureModeClient;
import mod.icanttellyou.picturemode.client.PictureModeState;
import mod.icanttellyou.picturemode.client.gui.PictureModeScreen;
import mod.icanttellyou.picturemode.client.image.screenshot.ScreenshotHandler;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.sounds.SoundEvents;

public class CoralinePictureModeScreen extends PictureModeScreen {
    private static final String SCREENSHOT_BUTTON_KEY = "gui.picturemode.takeScreenshot";
    private static final float ROTATION_DEGREES_PER_TICK = 6.0F;
    private static final float PAN_PIXELS_PER_TICK = 14.0F;
    private static final float ZOOM_PER_TICK = 0.08F;
    private static final float MAX_FRAME_TICKS = 2.0F;

    @Nullable
    private final Screen coraline$parent;

    public CoralinePictureModeScreen(@Nullable Screen parent) {
        super(parent);
        this.coraline$parent = parent;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        ControllerEntity controller = CoralinePictureModeControls.coraline$activeController();
        if (controller == null || PictureModeClient.getScreenshotHandler().getStatus() != ScreenshotHandler.Status.IDLE) {
            return;
        }

        coraline$applyCameraInput(controller, partialTick);

        if (CoralinePictureModeControls.coraline$screenshot.on(controller).justPressed()) {
            coraline$pressScreenshotButton();
        }
        if (ControlifyBindings.GUI_BACK.on(controller).justPressed()) {
            onClose();
        }
    }

    private void coraline$applyCameraInput(ControllerEntity controller, float partialTick) {
        PictureModeState state = PictureModeClient.getState();
        float ticks = Math.min(minecraft.getDeltaFrameTime(), MAX_FRAME_TICKS);

        float rotation = CoralinePictureModeControls.coraline$analogue(CoralinePictureModeControls.coraline$rotateRight, controller)
                - CoralinePictureModeControls.coraline$analogue(CoralinePictureModeControls.coraline$rotateLeft, controller);
        if (rotation != 0.0F) {
            state.cameraRotation.addToGoal(rotation * ROTATION_DEGREES_PER_TICK * ticks, partialTick);
        }

        double zoomDivisor = Math.pow(2.0D, state.cameraZoom.getGoal()) / 3.0D;
        float panX = CoralinePictureModeControls.coraline$analogue(CoralinePictureModeControls.coraline$panLeft, controller)
                - CoralinePictureModeControls.coraline$analogue(CoralinePictureModeControls.coraline$panRight, controller);
        float panY = CoralinePictureModeControls.coraline$analogue(CoralinePictureModeControls.coraline$panUp, controller)
                - CoralinePictureModeControls.coraline$analogue(CoralinePictureModeControls.coraline$panDown, controller);
        if (panX != 0.0F) {
            state.cameraPanX.addToGoal(panX * PAN_PIXELS_PER_TICK * ticks / zoomDivisor, partialTick);
        }
        if (panY != 0.0F) {
            state.cameraPanY.addToGoal(panY * PAN_PIXELS_PER_TICK * ticks / zoomDivisor, partialTick);
        }

        float zoom = CoralinePictureModeControls.coraline$analogue(CoralinePictureModeControls.coraline$zoomIn, controller)
                - CoralinePictureModeControls.coraline$analogue(CoralinePictureModeControls.coraline$zoomOut, controller);
        if (zoom != 0.0F) {
            state.cameraZoom.addToGoal(zoom * ZOOM_PER_TICK * ticks, partialTick);
        }
    }

    private void coraline$pressScreenshotButton() {
        for (GuiEventListener child : children()) {
            if (child instanceof Button button && button.getMessage().getContents() instanceof TranslatableContents contents && contents.getKey().equals(SCREENSHOT_BUTTON_KEY)) {
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                button.onPress();
                return;
            }
        }
    }

    @Override
    public void onClose() {
        if (minecraft.screen == this) {
            minecraft.setScreen(coraline$parent);
        }
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent event) {
        ControllerEntity controller = CoralinePictureModeControls.coraline$activeController();
        if (controller != null && event instanceof FocusNavigationEvent.ArrowNavigation && CoralinePictureModeControls.coraline$isCameraStickActive(controller)) {
            return null;
        }
        return super.nextFocusPath(event);
    }
}