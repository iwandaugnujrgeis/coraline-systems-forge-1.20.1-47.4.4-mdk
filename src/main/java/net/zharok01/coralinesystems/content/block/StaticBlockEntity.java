package net.zharok01.coralinesystems.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.zharok01.coralinesystems.registry.CoralineBlockEntities;

public class StaticBlockEntity extends BlockEntity {

    private BlockState copiedState = Blocks.AIR.defaultBlockState();

    public StaticBlockEntity(BlockPos pos, BlockState state) {
        super(CoralineBlockEntities.STATIC_BLOCK_ENTITY.get(), pos, state);
    }

    public void setCopiedState(BlockState state) {
        // Prevent copying another static block to avoid infinite loops
        if (state.getBlock() instanceof StaticBlock) {
            this.copiedState = Blocks.STONE.defaultBlockState();
        } else {
            this.copiedState = state;
        }
        this.setChanged();

        // Tells the server to send the update to the client, exactly as shown in the tutorial
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public BlockState getCopiedState() {
        return this.copiedState;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("CopiedState", NbtUtils.writeBlockState(this.copiedState));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("CopiedState")) {
            this.copiedState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("CopiedState"));
        }
    }

    // Required for the client to receive the NBT data for rendering
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }
}