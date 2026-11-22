package net.zharok01.coralinesystems.quiver.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.mixin.accessor.PlayerAccessor;
import net.zharok01.coralinesystems.network.QuiverInventoryPacket;
import net.zharok01.coralinesystems.quiver.QuiverContents;
import net.zharok01.coralinesystems.quiver.QuiverInventoryMenu;
import net.zharok01.coralinesystems.registry.CoralinePacketHandler;

public class QuiverInventoryScreen extends InventoryScreen {
    private static final ResourceLocation QUIVER_GUI_TEXTURE = CoralineSystems.of("textures/gui/quiver_gui.png");
    private static final int PLAYER_PREVIEW_X = 51;
    private static final int PLAYER_PREVIEW_Y = 75;
    private static final int PLAYER_PREVIEW_SCALE = 30;
    private static final int PLAYER_PREVIEW_LOOK_HEIGHT = 50;

    private static InventoryMenu coraline$vanillaInventoryMenu;

    public QuiverInventoryScreen(InventoryMenu menu, Inventory inventory, Component title) {
        super(coraline$lendInventoryMenu(inventory.player, menu));
        ((PlayerAccessor) inventory.player).coraline$setInventoryMenu(coraline$vanillaInventoryMenu);
    }

    private static Player coraline$lendInventoryMenu(Player player, InventoryMenu menu) {
        coraline$vanillaInventoryMenu = player.inventoryMenu;
        ((PlayerAccessor) player).coraline$setInventoryMenu(menu);
        return player;
    }

    @Override
    protected void init() {
        this.imageHeight = QuiverInventoryMenu.IMAGE_HEIGHT;
        super.init();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (this.minecraft.screen == this && !QuiverContents.isWornBy(this.minecraft.player)) {
            this.coraline$returnToVanillaInventory();
        }
    }

    private void coraline$returnToVanillaInventory() {
        LocalPlayer player = this.minecraft.player;
        ItemStack carried = this.menu.getCarried();
        CoralinePacketHandler.sendToServer(new QuiverInventoryPacket(false));
        player.containerMenu = player.inventoryMenu;
        player.inventoryMenu.setCarried(carried);
        this.minecraft.setScreen(new InventoryScreen(player));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(QUIVER_GUI_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        renderEntityInInventoryFollowsMouse(guiGraphics, this.leftPos + PLAYER_PREVIEW_X, this.topPos + PLAYER_PREVIEW_Y, PLAYER_PREVIEW_SCALE, (float) (this.leftPos + PLAYER_PREVIEW_X - mouseX), (float) (this.topPos + PLAYER_PREVIEW_Y - PLAYER_PREVIEW_LOOK_HEIGHT - mouseY), this.minecraft.player);
    }
}