package net.zharok01.coralinesystems.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.network.ServerboundNameTagPacket;
import net.zharok01.coralinesystems.registry.CoralinePacketHandler;

@OnlyIn(Dist.CLIENT)
public class NameTagScreen extends Screen {

    private static final ResourceLocation NAMETAG_SCREEN_TEXTURE =
            new ResourceLocation(CoralineSystems.MOD_ID, "textures/gui/container/nametag_screen.png");

    private static final int MAX_NAME_LENGTH = 18;
    private static final int PANEL_WIDTH = 195;
    private static final int PANEL_HEIGHT = 100;
    private static final int PANEL_TOP_Y = 62;
    private static final int EDITBOX_WIDTH = 180;
    private static final int EDITBOX_HEIGHT = 12;
    private static final int EDITBOX_PANEL_OFFSET_Y = 38;

    private final ItemStack stack;
    private final InteractionHand hand;

    private EditBox nameField;
    private int panelLeft;
    private boolean coraline$committed;

    public NameTagScreen(ItemStack stack, InteractionHand hand) {
        super(Component.translatable("screen.coraline_systems.nametag"));
        this.stack = stack;
        this.hand = hand;
    }

    @Override
    protected void init() {
        panelLeft = (width - PANEL_WIDTH) / 2;

        nameField = new EditBox(
                font,
                width / 2 - EDITBOX_WIDTH / 2,
                PANEL_TOP_Y + EDITBOX_PANEL_OFFSET_Y,
                EDITBOX_WIDTH,
                EDITBOX_HEIGHT,
                Component.empty()
        );
        nameField.setMaxLength(MAX_NAME_LENGTH);
        nameField.setBordered(false);
        nameField.setTextColor(0xFFFFFF);
        nameField.setCanLoseFocus(false);
        nameField.setResponder(text -> coraline$updateNameFieldPosition());
        if (stack.hasCustomHoverName()) {
            nameField.setValue(stack.getHoverName().getString());
        }
        coraline$updateNameFieldPosition();
        addWidget(nameField);
        setInitialFocus(nameField);

        addRenderableWidget(Button.builder(
                CommonComponents.GUI_DONE,
                btn -> onClose()
        ).bounds(width / 2 - 100, height / 4 + 144, 200, 20).build());
    }

    @Override
    public void resize(net.minecraft.client.Minecraft minecraft, int newWidth, int newHeight) {
        String preservedValue = nameField != null ? nameField.getValue() : "";
        super.resize(minecraft, newWidth, newHeight);
        nameField.setValue(preservedValue);
        coraline$updateNameFieldPosition();
    }

    @Override
    public void tick() {
        nameField.tick();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 || keyCode == 257 || keyCode == 335) {
            onClose();
            return true;
        }
        if (nameField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return nameField.charTyped(codePoint, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        coraline$renderBg(graphics);
        graphics.drawCenteredString(font, title, width / 2, 40, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
        nameField.render(graphics, mouseX, mouseY, partialTick);
    }

    private void coraline$renderBg(GuiGraphics graphics) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        graphics.blit(NAMETAG_SCREEN_TEXTURE, panelLeft, PANEL_TOP_Y, 0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        graphics.blit(NAMETAG_SCREEN_TEXTURE,
                panelLeft + 10, PANEL_TOP_Y + (PANEL_HEIGHT / 2) - 24,
                0, 100, 48, 48);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        if (!coraline$committed) {
            coraline$committed = true;
            CoralinePacketHandler.sendToServer(
                    new ServerboundNameTagPacket(hand, nameField.getValue().trim())
            );
        }
        super.onClose();
    }

    private void coraline$updateNameFieldPosition() {
        if (nameField != null) {
            int textWidth = font.width(nameField.getValue());
            nameField.setX((width - textWidth) / 2);
        }
    }
}
