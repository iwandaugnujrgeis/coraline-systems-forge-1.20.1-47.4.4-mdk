package net.zharok01.coralinesystems.mixin.compat.rediscovered;

import com.legacy.rediscovered.item.QuiverItem;
import java.util.Optional;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.zharok01.coralinesystems.quiver.QuiverContents;
import net.zharok01.coralinesystems.quiver.QuiverContentsTooltip;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = QuiverItem.class, remap = false)
public abstract class QuiverItemMixin {
    @Redirect(
            method = "m_7203_(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;m_36341_()Z", remap = false)
    )
    private boolean coraline$neverOpenMenu(Player player) {
        return false;
    }

    @Inject(method = "m_142207_", at = @At("HEAD"), cancellable = true)
    private void coraline$disableStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player, CallbackInfoReturnable<Boolean> callback) {
        callback.setReturnValue(false);
    }

    @Inject(method = "m_142305_", at = @At("HEAD"), cancellable = true)
    private void coraline$disableOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> callback) {
        callback.setReturnValue(false);
    }

    @Inject(method = "m_142422_", at = @At("HEAD"), cancellable = true)
    private void coraline$hideContentsTooltip(ItemStack stack, CallbackInfoReturnable<Optional<TooltipComponent>> callback) {
        callback.setReturnValue(Optional.empty());
    }

    @Inject(method = "m_142023_(Lnet/minecraft/world/entity/item/ItemEntity;)V", at = @At("HEAD"), cancellable = true)
    private void coraline$spillAllArrows(ItemEntity itemEntity, CallbackInfo callback) {
        ItemUtils.onContainerDestroyed(itemEntity, QuiverContents.read(itemEntity.getItem()).stream());
        callback.cancel();
    }
}