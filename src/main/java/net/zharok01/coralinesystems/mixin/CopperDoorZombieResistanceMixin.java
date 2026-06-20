package net.zharok01.coralinesystems.mixin;

import com.teamabnormals.caverns_and_chasms.common.block.weathering.CopperDoorBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DoorBlock.class)
public class CopperDoorZombieResistanceMixin {

    @Inject(
            method = "isWoodenDoor(Lnet/minecraft/world/level/block/state/BlockState;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void preventCopperDoorZombieTargeting(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof CopperDoorBlock) {
            cir.setReturnValue(false);
        }
    }
}