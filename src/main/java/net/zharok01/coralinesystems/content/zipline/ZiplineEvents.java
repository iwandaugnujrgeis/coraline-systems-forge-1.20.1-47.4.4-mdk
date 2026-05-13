package net.zharok01.coralinesystems.content.zipline;

import net.mehvahdjukaar.supplementaries.common.block.blocks.RopeBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.registry.CoralineTags;

@Mod.EventBusSubscriber(modid = "coraline_systems")
public class ZiplineEvents {

    @SubscribeEvent
    public static void onRightClickRope(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        BlockState state = event.getLevel().getBlockState(event.getPos());

        if (!(state.getBlock() instanceof RopeBlock)) return;
        if (!player.getMainHandItem().is(CoralineTags.ZIPLINEABLE)) return;

        // Prevent attaching while standing still on the ground
        if (player.onGround()) return;

        // Prevent attaching to a rope that is at or below the player's feet —
        // covers the case of standing on top of the rope itself, where onGround()
        // alone is not reliable because the rope's own hitbox counts as ground.
        if (event.getPos().getY() <= player.blockPosition().getY()) return;

        // If they are already ziplining, ignore further clicks
        if (ZiplineHandler.ZIPLINING_PLAYERS.containsKey(player.getUUID())) return;

        // Trigger on BOTH client and server for instant feedback!
        ZiplineHandler.startZiplining(player, player.getDeltaMovement(), event.getPos());

        if (event.getLevel().isClientSide()) {
            ZiplineClientHelper.playZiplineSound(player);
        }

        event.setCanceled(true);
    }
}