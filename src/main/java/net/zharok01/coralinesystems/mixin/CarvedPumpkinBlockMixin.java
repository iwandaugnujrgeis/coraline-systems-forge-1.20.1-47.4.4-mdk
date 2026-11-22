package net.zharok01.coralinesystems.mixin;

import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CarvedPumpkinBlock.class)
public class CarvedPumpkinBlockMixin {
    @Inject(method = "getOrCreateIronGolemBase", at = @At("HEAD"), cancellable = true)
    private void coraline$disableIronGolemBase(CallbackInfoReturnable<BlockPattern> cir) {
        cir.setReturnValue(BlockPatternBuilder.start().aisle("~").where('~', arg -> false).build());
    }

    @Inject(method = "getOrCreateIronGolemFull", at = @At("HEAD"), cancellable = true)
    private void coraline$disableIronGolemFull(CallbackInfoReturnable<BlockPattern> cir) {
        cir.setReturnValue(BlockPatternBuilder.start().aisle("~").where('~', arg -> false).build());
    }
}