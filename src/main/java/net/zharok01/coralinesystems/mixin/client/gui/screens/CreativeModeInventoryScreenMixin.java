package net.zharok01.coralinesystems.mixin.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.CreativeTabsScreenPage;
import net.zharok01.coralinesystems.client.gui.creative_tabs.CreativeTabIconCycler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends EffectRenderingInventoryScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
    @Shadow
    private CreativeTabsScreenPage currentPage;

    @Shadow
    protected abstract boolean checkTabClicked(CreativeModeTab creativeModeTab, double relativeMouseX, double relativeMouseY);

    private CreativeModeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Redirect(method = "renderTabButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab;getIconItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack coraline$cycleTabIcon(CreativeModeTab tab) {
        return CreativeTabIconCycler.getIcon(tab);
    }

    @Inject(method = "checkTabHovering", at = @At("HEAD"), cancellable = true)
    private void coraline$removeTabTooltip(GuiGraphics guiGraphics, CreativeModeTab creativeModeTab, int mouseX, int mouseY, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void coraline$playTabClickSound(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button != 0) {
            return;
        }

        double relativeMouseX = mouseX - this.leftPos;
        double relativeMouseY = mouseY - this.topPos;

        for (CreativeModeTab tab : this.currentPage.getVisibleTabs()) {
            if (this.checkTabClicked(tab, relativeMouseX, relativeMouseY)) {
                assert this.minecraft != null;
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return;
            }
        }
    }

}