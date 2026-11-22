package net.zharok01.coralinesystems.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.registry.CoralineKeyMappings;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class CoralineClientForgeEvents {

    @SubscribeEvent
    public static void onScreenKeyPressed(ScreenEvent.KeyPressed.Post event) {
        Screen screen = event.getScreen();
        if (!(screen instanceof AbstractContainerScreen<?>) || !(screen instanceof RecipeUpdateListener listener)) {
            return;
        }
        if (!CoralineKeyMappings.TOGGLE_RECIPE_BOOK.matches(event.getKeyCode(), event.getScanCode())) {
            return;
        }

        listener.getRecipeBookComponent().toggleVisibility();

        Minecraft minecraft = Minecraft.getInstance();
        screen.resize(minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        Entity entity = event.getEntity();
        if (entity.hasCustomName()) {
            event.setResult(Event.Result.ALLOW);
        }
    }
}