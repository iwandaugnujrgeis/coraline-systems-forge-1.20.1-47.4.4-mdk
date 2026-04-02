package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.zharok01.coralinesystems.registry.CoralineBlocks;
import net.zharok01.coralinesystems.registry.CoralineItems;
import net.zharok01.coralinesystems.registry.CoralineStats;
import net.zharok01.coralinesystems.registry.CoralineTriggers;
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
        if (!level.isClientSide() && entity instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();

            if (stack.is(CoralineItems.DIMENSIONAL_SHARD.get())) {

                Entity owner = itemEntity.getOwner();

                if (owner instanceof ServerPlayer player) {
                    CoralineTriggers.CORRUPT_PORTAL.trigger(player);
                    player.awardStat(CoralineStats.PORTALS_CORRUPTED.get());
                }

                level.playSound(null, pos, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.BLOCKS, 2.0F, 0.2F);
                itemEntity.discard();
                corruptPortal(level, pos, state.getValue(NetherPortalBlock.AXIS));
                info.cancel();
            }
        }
    }

    /**
     * Called when a Static portal block is broken (e.g. a player mines it or an explosion destroys it).
     *
     * We inject into NetherPortalBlock.onRemove — but StaticPortalBlock extends Block, not NetherPortalBlock.
     * The cleaner approach is to override onRemove() directly in StaticPortalBlock.
     * This Mixin handles the Nether portal side only (in case a conversion is reversed).
     *
     * For Static portal destruction cleanup, see StaticPortalBlock.onRemove().
     */

    @Unique
    private void corruptPortal(Level level, BlockPos startPos, Direction.Axis axis) {
        Set<BlockPos> portalBlocks = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(startPos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (visited.contains(current)) continue;
            visited.add(current);

            BlockState currentState = level.getBlockState(current);
            if (currentState.getBlock() instanceof NetherPortalBlock) {
                portalBlocks.add(current);
                addNeighbors(queue, current);
            }
        }

        BlockState staticPortal = CoralineBlocks.STATIC_PORTAL_BLOCK.get()
                .defaultBlockState()
                .setValue(NetherPortalBlock.AXIS, axis);
        for (BlockPos p : portalBlocks) {
            level.setBlock(p, staticPortal, 2);
        }
    }

    @Unique
    private void addNeighbors(Queue<BlockPos> queue, BlockPos pos) {
        queue.add(pos.above());
        queue.add(pos.below());
        queue.add(pos.north());
        queue.add(pos.south());
        queue.add(pos.east());
        queue.add(pos.west());
    }
}
