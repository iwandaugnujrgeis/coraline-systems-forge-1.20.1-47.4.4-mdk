package net.zharok01.coralinesystems.client.gui.compat.controlify;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.zharok01.coralinesystems.client.gui.recipe_book.RecipeControllerAccess;

public class CoralineRecipeBookHints {

    @SubscribeEvent
    public static void coraline$renderHints(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof RecipeUpdateListener listener) || !ControlifyApi.get().currentInputMode().isController()) {
            return;
        }

        ControllerEntity controller = ControlifyApi.get().getCurrentController().filter(c -> c.input().isPresent()).orElse(null);
        if (controller == null || !controller.genericConfig().config().showScreenGuides) {
            return;
        }

        RecipeBookComponent book = listener.getRecipeBookComponent();
        RecipeControllerAccess access = (RecipeControllerAccess) book;
        List<CoralineHintRenderer.Hint> hints = new ArrayList<>();

        if (!CoralineControlifyCompat.coraline$toggleRecipeBook.on(controller).isUnbound()) {
            hints.add(new CoralineHintRenderer.Hint(CoralineControlifyCompat.coraline$toggleRecipeBook.on(controller).inputIcon(),
                    Component.translatable(book.isVisible() ? "coraline_systems.controlify.hint.close_recipe_book" : "coraline_systems.controlify.hint.open_recipe_book")));
        }

        if (access.coraline$isStripOpen()) {
            Component upAndDown = Component.empty()
                    .append(ControlifyBindings.VMOUSE_SNAP_UP.on(controller).inputIcon())
                    .append(ControlifyBindings.VMOUSE_SNAP_DOWN.on(controller).inputIcon());
            hints.add(new CoralineHintRenderer.Hint(upAndDown, Component.translatable("coraline_systems.controlify.hint.browse_variants")));
            hints.add(new CoralineHintRenderer.Hint(ControlifyBindings.VMOUSE_LCLICK.on(controller).inputIcon(), Component.translatable("coraline_systems.controlify.hint.select_variant")));
            hints.add(new CoralineHintRenderer.Hint(ControlifyBindings.GUI_BACK.on(controller).inputIcon(), Component.translatable("coraline_systems.controlify.hint.close_variants")));
        } else if (book.isVisible() && access.coraline$isOverVariantCell(event.getMouseX(), event.getMouseY())) {
            hints.add(new CoralineHintRenderer.Hint(ControlifyBindings.VMOUSE_RCLICK.on(controller).inputIcon(), Component.translatable("coraline_systems.controlify.hint.show_variants")));
        }

        CoralineHintRenderer.coraline$draw(event.getGuiGraphics(), hints, CoralineHintRenderer.MARGIN);
    }
}