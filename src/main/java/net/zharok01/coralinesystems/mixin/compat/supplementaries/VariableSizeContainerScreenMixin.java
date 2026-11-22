package net.zharok01.coralinesystems.mixin.compat.supplementaries;

import net.mehvahdjukaar.supplementaries.client.screens.VariableSizeContainerScreen;
import net.mehvahdjukaar.supplementaries.common.inventories.VariableSizeContainerMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VariableSizeContainerScreen.class)
public abstract class VariableSizeContainerScreenMixin extends AbstractContainerScreen<VariableSizeContainerMenu> {

    // Disabled this for now since I'm back to 9 slots
    protected VariableSizeContainerScreenMixin(VariableSizeContainerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "m_7856_", at = @At("TAIL"), remap = false)
    private void coraline$alignSackTitleToLeft(CallbackInfo ci) {
        this.titleLabelX = 8;
    }
}