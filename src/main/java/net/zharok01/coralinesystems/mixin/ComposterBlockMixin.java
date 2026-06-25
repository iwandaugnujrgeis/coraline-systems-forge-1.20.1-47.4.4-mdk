package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.zharok01.coralinesystems.registry.CoralineBlocks;
import net.zharok01.coralinesystems.registry.CoralineTriggers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ComposterBlock.class)
public class ComposterBlockMixin {

    /**
     * Intercepts the tick scheduling when a player fills the Composter to the brim.
     */
    @Redirect(
            method = "addItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/LevelAccessor;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;I)V"
            )
    )
    private static void redirectAddItemTick(LevelAccessor level, BlockPos pos, Block block, int originalDelay) {
        theCoralineSystems$scheduleOrganicTick(level, pos, block);
    }

    /**
     * Intercepts the tick scheduling if a level 7 Composter is placed directly into the world.
     */
    @Redirect(
            method = "onPlace",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;I)V"
            )
    )
    private void redirectOnPlaceTick(Level level, BlockPos pos, Block block, int originalDelay) {
        theCoralineSystems$scheduleOrganicTick(level, pos, block);
    }

    /**
     * Helper method to calculate the new 5 to 7 minute delay (6000 - 8400 ticks)
     * and apply it to the queue.
     */
    @Unique private static void theCoralineSystems$scheduleOrganicTick(LevelAccessor level, BlockPos pos, Block block) {
        int minTicks = 6000; // 5 minutes
        int maxTicks = 8400; // 7 minutes

        // Generate a random delay between minTicks and maxTicks
        int newDelay = minTicks + level.getRandom().nextInt(maxTicks - minTicks + 1);

        level.scheduleTick(pos, block, newDelay);
    }

    // Shadow the vanilla shapes array so we can access the pre-calculated collision heights
    @Shadow @Final private static VoxelShape[] SHAPES;

    @Unique @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    private void coralinesystems$solidCompostLayers(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        // Read the current compost level and return the corresponding shape,
        // rather than defaulting to SHAPES[0] like Vanilla does.
        int currentLevel = state.getValue(ComposterBlock.LEVEL);
        cir.setReturnValue(SHAPES[currentLevel]);
    }

    /**
     * Intercepts `new ItemEntity(level, x, y, z, boneMealStack)` inside
     * `extractProduce` and returns a new `ItemEntity` carrying one
     * Organic Compost item instead.
     */
    @Redirect(
            method = "extractProduce",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/item/ItemEntity;"
            )
    )
    private static ItemEntity redirectBoneMealDropToOrganicCompost(
            Level level,
            double posX, double posY, double posZ,
            ItemStack ignoredBoneMeal
    ) {
        ItemStack compostStack = new ItemStack(CoralineBlocks.ORGANIC_COMPOST.get().asItem());
        return new ItemEntity(level, posX, posY, posZ, compostStack);
    }

    /**
     * Injects at the very end of `extractProduce` to fire our custom advancement trigger.
     * We check if the entity that caused the extraction is a ServerPlayer (to avoid crashes
     * if a Hopper or other automated system extracts the item).
     */
    @Inject(
            method = "extractProduce",
            at = @At("TAIL")
    )
    private static void triggerCompostHarvestedAdvancement(
            Entity entity,
            BlockState state,
            Level level,
            BlockPos pos,
            CallbackInfoReturnable<BlockState> cir
    ) {
        // Ensure we are on the server side and the entity is an actual player
        if (!level.isClientSide() && entity instanceof ServerPlayer serverPlayer) {
            CoralineTriggers.HARVEST_COMPOST.trigger(serverPlayer);
        }
    }
}