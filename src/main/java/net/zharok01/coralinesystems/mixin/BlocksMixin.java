package net.zharok01.coralinesystems.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TorchflowerCropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.zharok01.coralinesystems.mixin.accessor.BlockBehaviourAccessor;
import net.zharok01.coralinesystems.registry.CoralineSoundTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Blocks.class)
public class BlocksMixin {


    @Inject(
            method = "register(Ljava/lang/String;Lnet/minecraft/world/level/block/Block;)Lnet/minecraft/world/level/block/Block;",
            at = @At("HEAD")
    )
    private static void coraline$overrideMushroomSoundTypes(String id, Block block, CallbackInfoReturnable<Block> cir) {
        if ("brown_mushroom".equals(id) || "red_mushroom".equals(id)) {
            ((BlockBehaviourAccessor) block).setSoundType(CoralineSoundTypes.MUSHROOM);
        }

        if ("oak_leaves".equals(id) || "spruce_leaves".equals(id) || "birch_leaves".equals(id) || "jungle_leaves".equals(id) || "acacia_leaves".equals(id) || "dark_oak_leaves".equals(id)) {
            ((BlockBehaviourAccessor) block).setSoundType(CoralineSoundTypes.LEAVES);
        }
    }

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
    private static BlockBehaviour.Properties coraline$setTorchflowerLightLevel(BlockBehaviour.Properties properties) {
        return properties.lightLevel(blockState -> 10);
    }

    @ModifyArg(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/TorchflowerCropBlock;<init>(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)V"
            )
    )
    private static BlockBehaviour.Properties coraline$setTorchflowerCropLightLevel(BlockBehaviour.Properties properties) {
        return properties.lightLevel(blockState -> blockState.getValue(TorchflowerCropBlock.AGE) * 3);
    }

    @ModifyArg(
            method = "flowerPot(Lnet/minecraft/world/level/block/Block;[Lnet/minecraft/world/flag/FeatureFlag;)Lnet/minecraft/world/level/block/FlowerPotBlock;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/FlowerPotBlock;<init>(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)V"
            )
    )
    private static BlockBehaviour.Properties coraline$setPottedTorchflowerLightLevel(Block block, BlockBehaviour.Properties properties) {
        return block.equals(Blocks.TORCHFLOWER) ? properties.lightLevel(blockState -> 10) : properties;
    }
}