package net.zharok01.coralinesystems.mixin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.zharok01.coralinesystems.registry.CoralineItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin {

    @Redirect(
            method = "serverTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
    )
    private static boolean coralineSystems$replaceFuelInServerTick(ItemStack stack, Item item) {
        if (item == Items.BLAZE_POWDER) {
            return stack.is(CoralineItems.TIMEDUST.get());
        }
        return stack.is(item);
    }

    @Redirect(
            method = "canPlaceItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
    )
    private boolean coralineSystems$replaceFuelInCanPlaceItem(ItemStack stack, Item item) {
        if (item == Items.BLAZE_POWDER) {
            return stack.is(CoralineItems.TIMEDUST.get());
        }
        return stack.is(item);
    }
}