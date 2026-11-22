package net.zharok01.coralinesystems.client.gui.recipe_book;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.client.gui.CoralineScrollable;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, value = Dist.CLIENT)
public class RecipeBookScrollHandler {

    @SubscribeEvent
    public static void coraline$onMouseScrolled(ScreenEvent.MouseScrolled.Pre event) {
        Screen screen = event.getScreen();

        if (!(screen instanceof RecipeUpdateListener listener)) {
            return;
        }

        CoralineScrollable scrollable = (CoralineScrollable) listener.getRecipeBookComponent();
        if (scrollable.coraline$scroll(event.getMouseX(), event.getMouseY(), event.getScrollDelta())) {
            event.setCanceled(true);
        }
    }
}