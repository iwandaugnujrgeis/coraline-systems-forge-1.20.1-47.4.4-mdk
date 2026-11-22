package net.zharok01.coralinesystems.client.gui.compat.picture_mode;

import mod.icanttellyou.picturemode.client.PictureModeClient;
import mod.icanttellyou.picturemode.client.image.screenshot.ScreenshotHandler;
import mod.icanttellyou.picturemode.forgelike.client.event.RenderTargetBlitEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class CoralinePictureModeBlitFix {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void coraline$blitDuringCapture(RenderTargetBlitEvent.Pre event) {
        if (PictureModeClient.getScreenshotHandler().getStatus() == ScreenshotHandler.Status.IDLE) {
            return;
        }

        int[] width = new int[1];
        int[] height = new int[1];
        GLFW.glfwGetFramebufferSize(Minecraft.getInstance().getWindow().getWindow(), width, height);
        event.getTarget().blitToScreen(width[0], height[0]);
    }
}