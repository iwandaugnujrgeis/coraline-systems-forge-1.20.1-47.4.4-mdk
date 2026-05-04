package net.zharok01.coralinesystems.content.zipline;

import net.mehvahdjukaar.supplementaries.common.block.blocks.RopeBlock;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "coraline_systems")
public class ZiplineEvents {

    @SubscribeEvent
    public static void onRightClickRope(PlayerInteractEvent.RightClickBlock event) {
        // FIX: PlayerInteractEvent fires on both sides — only start ziplining on the server.
        // The client-side fire has no ZIPLINING_PLAYERS map entry and would desync.
        if (event.getLevel().isClientSide()) return;

        Player player = event.getEntity();
        BlockState state = event.getLevel().getBlockState(event.getPos());

        if (!(state.getBlock() instanceof RopeBlock)) return;
        if (!player.getMainHandItem().is(ItemTags.PICKAXES)) return;

        // Only grab the rope if the player jumped into it with horizontal momentum —
        // standing still and right-clicking should not trigger the zipline.
        if (player.onGround()) return;
        if (player.getDeltaMovement().horizontalDistance() < 0.1) return;

        ZiplineHandler.startZiplining(player, player.getDeltaMovement());
        event.setCanceled(true); // prevent the pickaxe from interacting with the rope block
    }
}