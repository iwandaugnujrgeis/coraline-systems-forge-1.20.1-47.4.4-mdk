package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = ForgeHooksClient.class, remap = false)
public class ForgeHooksClientMixin {

    /**
     * @author MyServerIsCompatible
     * @reason Disable Forge's enhanced server list
     */
    @Overwrite
    public static void drawForgePingInfo(JoinMultiplayerScreen gui, ServerData target, GuiGraphics guiGraphics, int x, int y, int width, int relativeMouseX, int relativeMouseY) {}

}