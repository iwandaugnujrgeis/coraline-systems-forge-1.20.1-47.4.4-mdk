package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.quark.addons.oddities.item.BackpackItem;

@Mixin(Inventory.class)
public abstract class InventoryMixin {

    @Shadow @Final public Player player;
    @Shadow @Final public NonNullList<ItemStack> items;

    @Inject(method = "fillStackedContents", at = @At("TAIL"))
    private void addBackpackContentsToRecipeBook(StackedContents stackedContent, CallbackInfo ci) {
        ItemStack backpack = this.player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(backpack.getItem() instanceof BackpackItem)) return;

        backpack.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(handler -> {
            for (int i = 0; i < handler.getSlots(); ++i) {
                ItemStack stack = handler.getStackInSlot(i);
                if (!stack.isEmpty()) stackedContent.accountStack(stack);
            }
        });
    }

    @Inject(method = "findSlotMatchingUnusedItem", at = @At("RETURN"), cancellable = true)
    private void coraline$fallbackNbtItemMatch(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValue() != -1) return;
        if (stack.hasTag()) return;

        for (int i = 0; i < this.items.size(); i++) {
            ItemStack inv = this.items.get(i);
            if (!inv.isEmpty()
                    && inv.getItem() == stack.getItem()
                    && !inv.isDamaged()
                    && !inv.isEnchanted()
                    && !inv.hasCustomHoverName()) {
                cir.setReturnValue(i);
                return;
            }
        }
    }
}