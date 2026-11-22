package net.zharok01.coralinesystems.event.zipline;

import net.mehvahdjukaar.supplementaries.common.block.blocks.RopeBlock;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.registry.CoralineSounds;
import net.zharok01.coralinesystems.registry.CoralineTags;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID)
public class ZiplineEvents {

    private static final int ROPE_CRUMBLE_PARTICLES = 8;

    @SubscribeEvent
    public static void onRightClickRope(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockState ropeState = level.getBlockState(event.getPos());

        if (!(ropeState.getBlock() instanceof RopeBlock)) return;
        if (!event.getItemStack().is(CoralineTags.ZIPLINEABLE)) return;
        if (player.getCooldowns().isOnCooldown(event.getItemStack().getItem())) return;
        if (ZiplineStateCapability.coraline$needsRelease(player)) return;
        if (player.onGround()) return;
        if (event.getPos().getY() <= player.blockPosition().getY()) return;
        if (ZiplineHandler.coraline$ziplinersFor(player).containsKey(player.getUUID())) return;
        if (ZiplineHandler.coraline$findRopeAbove(player) == null) return;

        ZiplineHandler.startZiplining(player, player.getDeltaMovement(), event.getPos());

        level.playSound(player, player.getX(), player.getY(), player.getZ(), CoralineSounds.ZIPLINE_ATTACH.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        if (level instanceof ServerLevel serverLevel) {
            ZiplineStateCapability.setPressed(player, true);

            Vec3 hitLocation = event.getHitVec().getLocation();
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ropeState),
                    hitLocation.x, hitLocation.y, hitLocation.z, ROPE_CRUMBLE_PARTICLES, 0.1, 0.1, 0.1, 0.05);
        } else {
            ZiplineClientHelper.playZiplineSound(player);
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}