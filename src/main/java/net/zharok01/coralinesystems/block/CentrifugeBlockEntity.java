package net.zharok01.coralinesystems.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.zharok01.coralinesystems.event.ServerEventListener;
import net.zharok01.coralinesystems.item.OrbItem;
import net.zharok01.coralinesystems.registry.CoralineBlockEntities;

public class CentrifugeBlockEntity extends BlockEntity {

    public CentrifugeBlockEntity(BlockPos pos, BlockState state) {
        super(CoralineBlockEntities.CENTRIFUGE.get(), pos, state);
    }

    public boolean tryActivate(Player player, InteractionHand hand) {
        if (level == null || level.isClientSide()) {
            return false;
        }

        var manager = ServerEventListener.getManager();
        if (manager == null) {
            return false;
        }

        if (manager.isActive()) {
            return false;
        }

        ItemStack heldStack = player.getItemInHand(hand);
        if (!(heldStack.getItem() instanceof OrbItem)) {
            return false;
        }

        heldStack.shrink(1);

        // Pass our own pos so the manager can flip blockstate and
        // spawn stop effects at the right location.
        boolean started = manager.startSession((ServerLevel) level, worldPosition);
        if (!started) {
            return false;
        }

        // Activation sound — one-shot, plays immediately on insertion.
        // Swap SoundEvents.BEACON_ACTIVATE for your custom SoundEvent later.
        level.playSound(
                null,
                worldPosition,
                SoundEvents.BEACON_ACTIVATE,
                SoundSource.BLOCKS,
                1.0f,
                1.0f
        );

        // Activation particle burst — FLASH gives a sharp "ignition" pop.
        ((ServerLevel) level).sendParticles(
                net.minecraft.core.particles.ParticleTypes.FLASH,
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 1.0,
                worldPosition.getZ() + 0.5,
                1,       // FLASH looks best as a single instance
                0.0, 0.0, 0.0,
                0.0
        );

        return true;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
    }
}