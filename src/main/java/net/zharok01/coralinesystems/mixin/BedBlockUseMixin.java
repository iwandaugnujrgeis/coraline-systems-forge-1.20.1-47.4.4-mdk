package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BedBlock.class)
public abstract class BedBlockUseMixin {

    @SuppressWarnings("removal")
    @Inject(
            method = "use",
            at = @At("HEAD"),
            cancellable = true
    )
    private void coraline$poofOnUse(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (hand != InteractionHand.MAIN_HAND) return;
        if (level.isClientSide()) return;

        ServerLevel serverLevel = (ServerLevel) level;

        BedPart part = state.getValue(BedBlock.PART);
        Direction facing = state.getValue(BedBlock.FACING);

        BlockPos headPos = (part == BedPart.HEAD) ? pos : pos.relative(facing);
        BlockPos footPos = (part == BedPart.HEAD) ? pos.relative(facing.getOpposite()) : pos;

        BlockState headState = serverLevel.getBlockState(headPos);
        BlockState footState = serverLevel.getBlockState(footPos);

        if (headState.getBlock() instanceof BedBlock) {
            serverLevel.removeBlock(headPos, false);
            coraline$spawnPoofEffect(serverLevel, headPos);
        }
        if (footState.getBlock() instanceof BedBlock) {
            serverLevel.removeBlock(footPos, false);
            coraline$spawnPoofEffect(serverLevel, footPos);
        }


        Item silverNugget = ForgeRegistries.ITEMS.getValue(new ResourceLocation("kubejs", "silver_nugget"));
        if (silverNugget != null && silverNugget != Items.AIR) {
            Block.popResource(serverLevel, footPos, new ItemStack(silverNugget));
        }

        cir.setReturnValue(InteractionResult.SUCCESS);
    }

    @Unique private static void coraline$spawnPoofEffect(ServerLevel level, BlockPos pos) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.3;
        double z = pos.getZ() + 0.5;
        level.playSound(null, pos, SoundEvents.PUFFER_FISH_BLOW_UP, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.CLOUD, x, y, z, 12, 0.25, 0.15, 0.25, 0.02);
    }
}
