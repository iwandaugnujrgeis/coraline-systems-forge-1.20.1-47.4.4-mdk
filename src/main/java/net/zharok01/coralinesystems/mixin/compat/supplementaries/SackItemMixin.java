package net.zharok01.coralinesystems.mixin.compat.supplementaries;

import java.util.List;
import java.util.Optional;
import net.mehvahdjukaar.supplementaries.common.items.SackItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SackItem.class)
public abstract class SackItemMixin {

    @Inject(method = "m_142422_", at = @At("HEAD"), cancellable = true, remap = false)
    private void coraline$removeTooltipImage(ItemStack stack, CallbackInfoReturnable<Optional<TooltipComponent>> cir) {
        cir.setReturnValue(Optional.empty());
    }

    @Inject(method = "m_7373_", at = @At("HEAD"), cancellable = true, remap = false)
    private void coraline$removeHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag, CallbackInfo ci) {
        ci.cancel();
    }
}