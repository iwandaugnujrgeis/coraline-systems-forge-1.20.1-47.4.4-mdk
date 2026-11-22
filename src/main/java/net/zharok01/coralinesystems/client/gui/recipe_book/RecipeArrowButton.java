package net.zharok01.coralinesystems.client.gui.recipe_book;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.IntSupplier;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("removal")
public class RecipeArrowButton extends Button {
    private static final int VANILLA_BUTTON_WIDTH = 20;
    private static final int VANILLA_BUTTON_HEIGHT = 18;

    private static final ResourceLocation INVENTORY_TEXTURE = new ResourceLocation("coraline_systems", "textures/gui/inventory_recipe_toggle.png");
    private static final ResourceLocation WORKSTATION_TEXTURE = new ResourceLocation("coraline_systems", "textures/gui/workstation_recipe_toggle.png");

    public static final Layout INVENTORY_LAYOUT = new Layout(INVENTORY_TEXTURE, 16, 13, 135, 29);
    public static final Layout CRAFTING_LAYOUT = new Layout(WORKSTATION_TEXTURE, 22, 15, 90, 35);
    public static final Layout FURNACE_LAYOUT = new Layout(WORKSTATION_TEXTURE, 22, 15, 80, 35);

    public record Layout(ResourceLocation texture, int width, int height, int offsetX, int offsetY) {
    }

    private final Layout coraline$layout;
    private final IntSupplier coraline$leftPos;
    private final IntSupplier coraline$topPos;

    public RecipeArrowButton(ImageButton vanillaButton, Layout layout, IntSupplier leftPos, IntSupplier topPos) {
        super(leftPos.getAsInt() + layout.offsetX(), topPos.getAsInt() + layout.offsetY(), layout.width(), layout.height(), CommonComponents.EMPTY, button -> vanillaButton.onPress(), DEFAULT_NARRATION);
        this.coraline$layout = layout;
        this.coraline$leftPos = leftPos;
        this.coraline$topPos = topPos;
    }

    @Nullable
    public static ImageButton coraline$findVanillaButton(Screen screen) {
        for (GuiEventListener child : screen.children()) {
            if (child instanceof ImageButton button && button.getWidth() == VANILLA_BUTTON_WIDTH && button.getHeight() == VANILLA_BUTTON_HEIGHT) {
                return button;
            }
        }

        return null;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.setPosition(this.coraline$leftPos.getAsInt() + this.coraline$layout.offsetX(), this.coraline$topPos.getAsInt() + this.coraline$layout.offsetY());
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.isHovered()) {
            RenderSystem.enableBlend();
            guiGraphics.blit(this.coraline$layout.texture(), this.getX(), this.getY(), 0.0F, 0.0F, this.width, this.height, this.width, this.height);
        }
    }
}