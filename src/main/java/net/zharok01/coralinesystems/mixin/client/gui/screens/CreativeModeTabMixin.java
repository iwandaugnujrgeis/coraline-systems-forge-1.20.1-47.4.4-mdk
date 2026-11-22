package net.zharok01.coralinesystems.mixin.client.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.zharok01.coralinesystems.client.gui.creative_tabs.CoralineCreativeTabLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Set;

@Mixin(CreativeModeTab.class)
public abstract class CreativeModeTabMixin {

    @Shadow private Collection<ItemStack> displayItems;
    @Shadow private Set<ItemStack> displayItemsSearchTab;
    @Shadow public abstract void rebuildSearchTree();

    @Unique private static final ResourceLocation OPERATOR_TAB_ID = new ResourceLocation("minecraft", "op_blocks");

    @Unique private ItemStack coraline$cachedIcon = null;
    @Unique private boolean coraline$iconCached = false;

    @Inject(method = "buildContents", at = @At("HEAD"), cancellable = true)
    private void coraline$onBuildContents(CreativeModeTab.ItemDisplayParameters parameters, CallbackInfo ci) {
        CreativeModeTab self = (CreativeModeTab) (Object) this;
        if (!CoralineCreativeTabLoader.INSTANCE.hasReplacement(self)) return;

        Collection<ItemStack> finalItems = CoralineCreativeTabLoader.INSTANCE.buildFinalItemList(self, parameters);

        displayItems = finalItems;
        displayItemsSearchTab = ItemStackLinkedSet.createTypeAndTagSet();
        displayItemsSearchTab.addAll(finalItems);

        rebuildSearchTree();

        coraline$iconCached = false;
        coraline$cachedIcon = null;

        ci.cancel();
    }

    @Inject(method = "hasAnyItems", at = @At("RETURN"), cancellable = true)
    private void coraline$onHasAnyItems(CallbackInfoReturnable<Boolean> cir) {
        CreativeModeTab self = (CreativeModeTab) (Object) this;
        if (CoralineCreativeTabLoader.INSTANCE.hasReplacement(self)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "shouldDisplay", at = @At("RETURN"), cancellable = true)
    private void coraline$onShouldDisplay(CallbackInfoReturnable<Boolean> cir) {
        CreativeModeTab self = (CreativeModeTab) (Object) this;
        if (CoralineCreativeTabLoader.INSTANCE.isDisabled(self)) {
            cir.setReturnValue(false);
        } else if (OPERATOR_TAB_ID.equals(BuiltInRegistries.CREATIVE_MODE_TAB.getKey(self))
                && !Minecraft.getInstance().options.operatorItemsTab().get()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getIconItem", at = @At("RETURN"), cancellable = true)
    private void coraline$onGetIconItem(CallbackInfoReturnable<ItemStack> cir) {
        CreativeModeTab self = (CreativeModeTab) (Object) this;
        if (!CoralineCreativeTabLoader.INSTANCE.hasReplacement(self)) return;
        if (!coraline$iconCached) {
            coraline$cachedIcon = CoralineCreativeTabLoader.INSTANCE.getReplacementIcon(self);
            coraline$iconCached = true;
        }
        if (coraline$cachedIcon != null && !coraline$cachedIcon.isEmpty()) {
            cir.setReturnValue(coraline$cachedIcon);
        }
    }

    @Inject(method = "contains", at = @At("RETURN"), cancellable = true)
    private void coraline$onContains(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        CreativeModeTab self = (CreativeModeTab) (Object) this;
        if (CoralineCreativeTabLoader.INSTANCE.hasReplacement(self)) {
            cir.setReturnValue(displayItemsSearchTab.contains(stack));
        }
    }
}