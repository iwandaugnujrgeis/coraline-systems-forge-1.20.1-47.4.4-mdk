package net.zharok01.coralinesystems.mixin;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.common.util.LazyOptional;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.quark.addons.oddities.item.BackpackItem;

@Mixin(Inventory.class)
public abstract class InventoryMixin {

    @Shadow @Final public Player player;

    @Inject(method = "fillStackedContents", at = @At("TAIL"))
    private void addBackpackContentsToRecipeBook(StackedContents stackedContent, CallbackInfo ci) {
        ItemStack backpack = this.player.getItemBySlot(EquipmentSlot.CHEST);

        // Ensure they are actually wearing the Quark backpack
        if (!(backpack.getItem() instanceof BackpackItem)) return;

        LazyOptional<IItemHandler> optional = backpack.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
        if (optional.isPresent()) {
            IItemHandler handler = optional.orElse(null);
            if (handler != null) {
                for (int i = 0; i < handler.getSlots(); ++i) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        stackedContent.accountStack(stack);
                    }
                }
            }
        }
    }
}