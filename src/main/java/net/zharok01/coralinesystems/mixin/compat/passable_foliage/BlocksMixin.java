package net.zharok01.coralinesystems.mixin.compat.passable_foliage;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.zharok01.coralinesystems.block.PassableFoliageBlock;
import net.zharok01.coralinesystems.registry.CoralineTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Blocks.class, priority = 500)
public class BlocksMixin {

    @Unique
    private static boolean coraline$tagErrorLogged;

    @SuppressWarnings("deprecation")
    @Inject(method = "rebuildCache", at = @At("HEAD"))
    private static void coraline$initPassableFlags(CallbackInfo ci) {
        for (Block block : BuiltInRegistries.BLOCK) {
            try {
                boolean passable = block.builtInRegistryHolder().is(CoralineTags.PASSABLE_FOLIAGE);
                ((PassableFoliageBlock) block).coraline$setPassable(passable);
            } catch (Throwable t) {
                if (!coraline$tagErrorLogged) {
                    System.err.println("[CoralineSystems/PassableFoliage] Failed to set passable flag: " + t);
                    coraline$tagErrorLogged = true;
                }
                ((PassableFoliageBlock) block).coraline$setPassable(false);
            }
        }
    }
}
