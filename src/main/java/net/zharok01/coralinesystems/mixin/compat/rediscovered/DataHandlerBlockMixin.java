package net.zharok01.coralinesystems.mixin.compat.rediscovered;

import com.legacy.structure_gel.core.block.DataHandlerBlock;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DataHandlerBlock.class, remap = false)
public abstract class DataHandlerBlockMixin {
    @Inject(method = "m_5871_", at = @At("HEAD"), cancellable = true, remap = false)
    private void coraline$removeShiftTooltip(ItemStack stack, BlockGetter level, List<Component> lore, TooltipFlag flag, CallbackInfo ci) {
        ci.cancel();
    }
}