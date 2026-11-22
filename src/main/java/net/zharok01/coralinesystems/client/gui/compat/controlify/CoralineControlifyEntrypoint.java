package net.zharok01.coralinesystems.client.gui.compat.controlify;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.bind.ControlifyBindApi;
import dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint;
import dev.isxander.controlify.api.entrypoint.InitContext;
import dev.isxander.controlify.api.entrypoint.PreInitContext;
import dev.isxander.controlify.bindings.BindContext;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;
import net.zharok01.coralinesystems.CoralineSystems;

public class CoralineControlifyEntrypoint implements ControlifyEntrypoint {

    @Override
    public void onControlifyPreInit(PreInitContext context) {
        CoralineControlifyCompat.coraline$toggleRecipeBook = ControlifyBindApi.get().registerBinding(builder -> builder
                .id(CoralineSystems.MOD_ID, "toggle_recipe_book")
                .category(Component.translatable("coraline_systems.controlify.category"))
                .allowedContexts(BindContext.CONTAINER, BindContext.V_MOUSE_CURSOR));

        if (ModList.get().isLoaded("picturemode")) {
            CoralinePictureModeControls.coraline$registerBindings(ControlifyBindApi.get());
        }
    }

    @Override
    public void onControlifyInit(InitContext context) {
    }

    @Override
    public void onControllersDiscovered(ControlifyApi controlify) {
    }
}