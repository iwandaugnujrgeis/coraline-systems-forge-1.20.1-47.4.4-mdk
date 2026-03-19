package net.zharok01.coralinesystems.mixin;

import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RespawnAnchorBlock.class)
public class RespawnAnchorFuelMixin {

    @WrapMethod(method = "isRespawnFuel")
    private static boolean isRespawnFuel(ItemStack stack, Operation<Boolean> original) {
        if (stack.is(AMItemRegistry.SOUL_HEART.get())) return true;
        return original.call(stack);
    }
}