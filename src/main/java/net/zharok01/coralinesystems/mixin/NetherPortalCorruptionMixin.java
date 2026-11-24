package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.zharok01.coralinesystems.entity.projectile.ThrownDimensionalShard;
import net.zharok01.coralinesystems.registry.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

@Mixin(NetherPortalBlock.class)
public class NetherPortalCorruptionMixin {

    @Inject(at = @At("HEAD"), method = "entityInside", cancellable = true)
    private void onEntityInside(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo info) {
        if (!level.isClientSide()) {
            boolean isTriggeringShard = false;
            Entity owner = null;

            if (entity instanceof ItemEntity itemEntity) {
                ItemStack stack = itemEntity.getItem();
                if (stack.is(CoralineItems.DIMENSIONAL_SHARD.get())) {
                    isTriggeringShard = true;
                    owner = itemEntity.getOwner();
                }
            }
            else if (entity instanceof ThrownDimensionalShard thrownShard) {
                isTriggeringShard = true;
                owner = thrownShard.getOwner();
            }

            if (isTriggeringShard) {
                if (owner instanceof ServerPlayer player) {
                    CoralineTriggers.CORRUPT_PORTAL.trigger(player);
                    player.awardStat(CoralineStats.PORTALS_CORRUPTED.get());
                }

                level.playSound(null, pos, CoralineSounds.STATIC_PORTAL_OPEN.get(), SoundSource.BLOCKS, 2.0F, 0.2F);

                entity.discard();

                coraline$corruptPortal(level, pos);
                info.cancel();
            }
        }
    }

    @Unique
    private void coraline$corruptPortal(Level level, BlockPos startPos) {
        Set<BlockPos> portalBlocks = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();

        if (!level.getBlockState(startPos).is(Blocks.NETHER_PORTAL)) return;
        queue.add(startPos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (visited.contains(current)) continue;
            visited.add(current);

            BlockState currentState = level.getBlockState(current);
            if (currentState.is(Blocks.NETHER_PORTAL)) {
                portalBlocks.add(current);
                coraline$addNeighbors(queue, current);
            }
        }

        for (BlockPos p : portalBlocks) {
            Direction.Axis axis = level.getBlockState(p).getValue(NetherPortalBlock.AXIS);
            BlockState staticPortal = CoralineBlocks.STATIC_PORTAL_BLOCK.get()
                    .defaultBlockState()
                    .setValue(NetherPortalBlock.AXIS, axis);
            level.setBlock(p, staticPortal, 2);
        }
    }

    @Unique
    private void coraline$addNeighbors(Queue<BlockPos> queue, BlockPos pos) {
        queue.add(pos.above());
        queue.add(pos.below());
        queue.add(pos.north());
        queue.add(pos.south());
        queue.add(pos.east());
        queue.add(pos.west());
    }
}