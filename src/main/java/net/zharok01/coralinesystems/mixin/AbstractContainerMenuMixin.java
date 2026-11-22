package net.zharok01.coralinesystems.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ClickType;
import net.zharok01.coralinesystems.registry.CoralineItems;
import net.zharok01.coralinesystems.registry.CoralineTriggers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {

    @Unique private static final int BREWING_FUEL_SLOT = 4;

    @Inject(method = "clicked", at = @At("TAIL"))
    private void coraline$triggerTimedustFuel(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        AbstractContainerMenu self = (AbstractContainerMenu) (Object) this;
        if (!(self instanceof BrewingStandMenu menu) || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (menu.getSlot(BREWING_FUEL_SLOT).getItem().is(CoralineItems.TIMEDUST.get())) {
            CoralineTriggers.TIMEDUST_FUEL.trigger(serverPlayer);
        }
    }
}