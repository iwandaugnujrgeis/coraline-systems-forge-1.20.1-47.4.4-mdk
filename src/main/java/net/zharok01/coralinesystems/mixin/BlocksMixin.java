package net.zharok01.coralinesystems.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TorchflowerCropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Blocks.class)
public class BlocksMixin {

    /**
     * Injects into the static initializer of Blocks to give the Torchflower block a constant light level of 10.
     * Uses a slice to safely isolate the constructor invocation between the "torchflower" and "poppy" registrations.
     */
    @ModifyArg(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/FlowerBlock;<init>(Lnet/minecraft/world/effect/MobEffect;ILnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)V"
            ),
            slice = @Slice(
                    from = @At(value = "CONSTANT", args = "stringValue=torchflower"),
                    to = @At(value = "CONSTANT", args = "stringValue=poppy")
            )
    )
    private static BlockBehaviour.Properties coralineSystems$setTorchflowerLightLevel(BlockBehaviour.Properties properties) {
        return properties.lightLevel(blockState -> 10);
    }

    /**
     * Injects into the static initializer of Blocks to make the Torchflower Crop emit dynamic light
     * scaling up with its current growth age (Age * 3).
     */
    @ModifyArg(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/TorchflowerCropBlock;<init>(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)V"
            )
    )
    private static BlockBehaviour.Properties coralineSystems$setTorchflowerCropLightLevel(BlockBehaviour.Properties properties) {
        return properties.lightLevel(blockState -> blockState.getValue(TorchflowerCropBlock.AGE) * 3);
    }

    /**
     * Injects into the flowerPot registration helper method to make a potted torchflower emit a light level of 10.
     */
    @ModifyArg(
            method = "flowerPot(Lnet/minecraft/world/level/block/Block;[Lnet/minecraft/world/flag/FeatureFlag;)Lnet/minecraft/world/level/block/FlowerPotBlock;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/FlowerPotBlock;<init>(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)V"
            )
    )
    private static BlockBehaviour.Properties coralineSystems$setPottedTorchflowerLightLevel(Block block, BlockBehaviour.Properties properties) {
        return block.equals(Blocks.TORCHFLOWER) ? properties.lightLevel(blockState -> 10) : properties;
    }
}