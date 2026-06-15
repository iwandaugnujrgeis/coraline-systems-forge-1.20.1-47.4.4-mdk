package net.zharok01.coralinesystems.event.advancements;

import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.registry.CoralineTriggers;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RepeaterMaxDelayEvent {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        // Only fire server-side and only for ServerPlayers
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        BlockState state = event.getLevel().getBlockState(event.getPos());

        // Check the block is a Repeater
        if (!(state.getBlock() instanceof RepeaterBlock)) {
            return;
        }

        // RepeaterBlock.use() calls state.cycle(DELAY).
        // DELAY cycles 1→2→3→4→1, so right-clicking when delay=3
        // produces delay=4 (maximum). That is the moment we want to catch.
        int currentDelay = state.getValue(BlockStateProperties.DELAY);
        if (currentDelay == 3) {
            CoralineTriggers.REPEATER_MAX_DELAY.trigger(serverPlayer);
        }
    }
}