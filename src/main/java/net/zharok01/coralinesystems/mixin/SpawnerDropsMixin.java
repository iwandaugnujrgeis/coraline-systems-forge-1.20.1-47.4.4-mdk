package net.zharok01.coralinesystems.mixin;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.zharok01.coralinesystems.mixin.accessor.BaseSpawnerAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class SpawnerDropsMixin {
    @Inject(
            method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;)Ljava/util/List;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void coraline$dropEmptySpawner(BlockState state, ServerLevel serverLevel, BlockPos blockPos, @Nullable BlockEntity blockEntity, CallbackInfoReturnable<List<ItemStack>> cir) {
        coraline$replaceDropsIfSpawnerIsEmpty(state, blockEntity, cir);
    }

    @Inject(
            method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void coraline$dropEmptySpawnerWithTool(BlockState state, ServerLevel serverLevel, BlockPos blockPos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack tool, CallbackInfoReturnable<List<ItemStack>> cir) {
        coraline$replaceDropsIfSpawnerIsEmpty(state, blockEntity, cir);
    }

    @Unique
    private static void coraline$replaceDropsIfSpawnerIsEmpty(BlockState state, @Nullable BlockEntity blockEntity, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (!state.is(Blocks.SPAWNER) || !(blockEntity instanceof SpawnerBlockEntity spawnerBlockEntity)) {
            return;
        }

        BaseSpawnerAccessor spawner = (BaseSpawnerAccessor) (Object) spawnerBlockEntity.getSpawner();
        SpawnData nextSpawnData = spawner.coraline$getNextSpawnData();
        boolean hasAssignedEntity = nextSpawnData != null && nextSpawnData.getEntityToSpawn().contains("id", Tag.TAG_STRING);

        if (!hasAssignedEntity) {
            hasAssignedEntity = spawner.coraline$getSpawnPotentials().unwrap().stream()
                    .map(WeightedEntry.Wrapper::getData)
                    .anyMatch(spawnData -> spawnData.getEntityToSpawn().contains("id", Tag.TAG_STRING));
        }

        if (!hasAssignedEntity) {
            cir.setReturnValue(List.of(new ItemStack(Blocks.SPAWNER)));
        }
    }
}