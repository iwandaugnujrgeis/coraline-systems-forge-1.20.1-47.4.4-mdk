package net.zharok01.coralinesystems.registry;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class CoralineKeyMappings {

    public static final KeyMapping TOGGLE_RECIPE_BOOK = new KeyMapping(
            "key.coraline_systems.toggle_recipe_book",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.inventory"
    );
}