package net.zharok01.coralinesystems.client.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, value = Dist.CLIENT)
public class CoralineDragReleaseHandler {

    @SubscribeEvent
    public static void coraline$onMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
        if (event.getButton() != 0) {
            return;
        }

        Screen screen = event.getScreen();
        if (screen instanceof CoralineDragEnd dragEnd) {
            dragEnd.coraline$endDrag();
        } else if (screen instanceof RecipeUpdateListener listener) {
            ((CoralineDragEnd) listener.getRecipeBookComponent()).coraline$endDrag();
        }
    }
}
