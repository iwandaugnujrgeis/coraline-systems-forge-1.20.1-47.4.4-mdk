package net.zharok01.coralinesystems.quiver;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public record QuiverContentsTooltip(NonNullList<ItemStack> contents) implements TooltipComponent {
}
