package net.zharok01.coralinesystems.client.gui.compat.controlify;

import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.controller.ControllerEntity;
import java.util.ArrayList;
import java.util.List;
import mod.icanttellyou.picturemode.client.PictureModeClient;
import mod.icanttellyou.picturemode.client.gui.PictureModeScreen;
import mod.icanttellyou.picturemode.client.image.screenshot.ScreenshotHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CoralinePictureModeCompat {
    private static final int HINT_TOP = 34;

    @SubscribeEvent
    public static void coraline$swapScreen(ScreenEvent.Opening event) {
        Screen screen = event.getNewScreen();
        if (screen instanceof PictureModeScreen && !(screen instanceof CoralinePictureModeScreen)) {
            event.setNewScreen(new CoralinePictureModeScreen(event.getCurrentScreen()));
        }
    }

    @SubscribeEvent
    public static void coraline$renderHints(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof PictureModeScreen) || PictureModeClient.getScreenshotHandler().getStatus() != ScreenshotHandler.Status.IDLE) {
            return;
        }

        ControllerEntity controller = CoralinePictureModeControls.coraline$activeController();
        if (controller == null || !controller.genericConfig().config().showScreenGuides) {
            return;
        }

        List<CoralineHintRenderer.Hint> hints = new ArrayList<>();
        coraline$addHint(hints, "zoom", controller, CoralinePictureModeControls.coraline$zoomOut, CoralinePictureModeControls.coraline$zoomIn);
        coraline$addHint(hints, "screenshot", controller, CoralinePictureModeControls.coraline$screenshot);

        CoralineHintRenderer.coraline$draw(event.getGuiGraphics(), hints, HINT_TOP);
    }

    private static void coraline$addHint(List<CoralineHintRenderer.Hint> hints, String key, ControllerEntity controller, InputBindingSupplier... suppliers) {
        MutableComponent icons = Component.empty();
        boolean anyBound = false;

        for (InputBindingSupplier supplier : suppliers) {
            InputBinding binding = supplier.on(controller);
            if (!binding.isUnbound()) {
                icons.append(binding.inputIcon());
                anyBound = true;
            }
        }

        if (anyBound) {
            hints.add(new CoralineHintRenderer.Hint(icons, Component.translatable("coraline_systems.controlify.hint.picture_mode." + key)));
        }
    }
}