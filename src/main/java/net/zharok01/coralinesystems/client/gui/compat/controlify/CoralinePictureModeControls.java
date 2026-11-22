package net.zharok01.coralinesystems.client.gui.compat.controlify;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.bind.ControlifyBindApi;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.bindings.BindContext;
import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.network.chat.Component;
import net.zharok01.coralinesystems.CoralineSystems;

public final class CoralinePictureModeControls {
    private static final float STICK_ACTIVE_THRESHOLD = 0.1F;

    public static InputBindingSupplier coraline$panUp;
    public static InputBindingSupplier coraline$panDown;
    public static InputBindingSupplier coraline$panLeft;
    public static InputBindingSupplier coraline$panRight;
    public static InputBindingSupplier coraline$rotateLeft;
    public static InputBindingSupplier coraline$rotateRight;
    public static InputBindingSupplier coraline$screenshot;
    public static InputBindingSupplier coraline$zoomIn;
    public static InputBindingSupplier coraline$zoomOut;

    public static void coraline$registerBindings(ControlifyBindApi api) {
        coraline$panUp = coraline$bind(api, "picture_mode_pan_up");
        coraline$panDown = coraline$bind(api, "picture_mode_pan_down");
        coraline$panLeft = coraline$bind(api, "picture_mode_pan_left");
        coraline$panRight = coraline$bind(api, "picture_mode_pan_right");
        coraline$rotateLeft = coraline$bind(api, "picture_mode_rotate_left");
        coraline$rotateRight = coraline$bind(api, "picture_mode_rotate_right");
        coraline$screenshot = coraline$bind(api, "picture_mode_screenshot");
        coraline$zoomIn = coraline$bind(api, "picture_mode_zoom_in");
        coraline$zoomOut = coraline$bind(api, "picture_mode_zoom_out");
    }

    private static InputBindingSupplier coraline$bind(ControlifyBindApi api, String path) {
        return api.registerBinding(builder -> builder
                .id(CoralineSystems.MOD_ID, path)
                .category(Component.translatable("coraline_systems.controlify.category"))
                .allowedContexts(BindContext.REGULAR_SCREEN));
    }

    public static ControllerEntity coraline$activeController() {
        if (!ControlifyApi.get().currentInputMode().isController()) {
            return null;
        }
        return ControlifyApi.get().getCurrentController().filter(c -> c.input().isPresent()).orElse(null);
    }

    public static float coraline$analogue(InputBindingSupplier supplier, ControllerEntity controller) {
        return supplier.on(controller).analogueNow();
    }

    public static boolean coraline$isCameraStickActive(ControllerEntity controller) {
        return coraline$analogue(coraline$panUp, controller) > STICK_ACTIVE_THRESHOLD
                || coraline$analogue(coraline$panDown, controller) > STICK_ACTIVE_THRESHOLD
                || coraline$analogue(coraline$panLeft, controller) > STICK_ACTIVE_THRESHOLD
                || coraline$analogue(coraline$panRight, controller) > STICK_ACTIVE_THRESHOLD
                || coraline$analogue(coraline$rotateLeft, controller) > STICK_ACTIVE_THRESHOLD
                || coraline$analogue(coraline$rotateRight, controller) > STICK_ACTIVE_THRESHOLD;
    }
}