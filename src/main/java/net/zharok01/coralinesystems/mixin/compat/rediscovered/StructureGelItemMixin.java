package net.zharok01.coralinesystems.mixin.compat.rediscovered;

import com.legacy.structure_gel.api.item.StructureGelItem;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = StructureGelItem.class, remap = false)
public abstract class StructureGelItemMixin {
    @Inject(method = "m_7373_", at = @At("HEAD"), cancellable = true, remap = false)
    private void coraline$removeShiftTooltip(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag, CallbackInfo ci) {
        ci.cancel();
    }
}