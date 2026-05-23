package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.NonNullList;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.quark.addons.oddities.item.BackpackItem;

@Mixin(Inventory.class)
public abstract class InventoryMixin {

    @Shadow @Final public Player player;

    // Correct pattern: @Final annotation (not Java's 'final'), no initializer.
    // Using Java's 'final' + '= null' caused the compiler to emit a constructor
    // assignment that corrupted the field before Inventory.<init> could build
    // its compartments ImmutableList, resulting in a NullPointerException.
    @Shadow @Final public NonNullList<ItemStack> items;

    // -------------------------------------------------------------------------
    // Existing injection: include Quark backpack contents in the recipe book's
    // StackedContents so recipes requiring items stored in the backpack show up.
    // -------------------------------------------------------------------------
    @Inject(method = "fillStackedContents", at = @At("TAIL"))
    private void addBackpackContentsToRecipeBook(StackedContents stackedContent, CallbackInfo ci) {
        ItemStack backpack = this.player.getItemBySlot(EquipmentSlot.CHEST);

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

    // -------------------------------------------------------------------------
    // New injection: fix vanilla recipe book autofill for NBT-identity items
    // (water bottles, splash potions, tipped arrows, etc.).
    //
    // ROOT CAUSE:
    //   StackedContents tracks items by registry ID only — NBT is stripped.
    //   When the recipe book later calls findSlotMatchingUnusedItem to locate
    //   the actual slot, it passes an NBT-less ItemStack (e.g. plain
    //   minecraft:potion). That fails isSameItemSameTags against a water bottle
    //   which carries {Potion:"minecraft:water"} NBT, so the method returns -1
    //   and nothing gets placed into the crafting grid.
    //
    // FIX:
    //   When the normal search returns -1 AND the search stack has no NBT
    //   (the StackedContents-stripping signature), do a second pass that matches
    //   by item type only, recovering the correct slot.
    //
    // SAFETY GUARD:
    //   If the search stack already carries NBT and still didn't match, there is
    //   genuinely no right item in the inventory. We never widen the search in
    //   that case, so enchanted tools, named items, etc. are unaffected.
    //
    // EDGE CASE — multiple potions of different types:
    //   If the player has both a water bottle and a healing potion, the fallback
    //   picks whichever comes first. Autofill may place the wrong variant and
    //   crafting won't complete. This is an inherent limitation of the vanilla
    //   recipe book — it has no way to know which specific NBT variant the
    //   ingredient requires once StackedContents has discarded that information.
    //   Using an item tag (e.g. kubejs:water_bottles) remains the cleanest
    //   solution for recipes where this ambiguity matters.
    // -------------------------------------------------------------------------
    @Inject(method = "findSlotMatchingUnusedItem", at = @At("RETURN"), cancellable = true)
    private void coralinesystems$fallbackNbtItemMatch(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        // Already found a valid slot — nothing to do.
        if (cir.getReturnValue() != -1) return;

        // The search stack has its own NBT and still didn't match → genuine miss,
        // do NOT widen the search.
        if (stack.hasTag()) return;

        // Second pass: item-type-only match. Same undamaged/unenchanted/unnamed
        // guards as the vanilla method to avoid touching special items.
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