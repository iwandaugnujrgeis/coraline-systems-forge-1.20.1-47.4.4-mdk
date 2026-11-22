package net.zharok01.coralinesystems.event.advancement;

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
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        BlockState state = event.getLevel().getBlockState(event.getPos());

        if (!(state.getBlock() instanceof RepeaterBlock)) {
            return;
        }

        int currentDelay = state.getValue(BlockStateProperties.DELAY);
        if (currentDelay == 3) {
            CoralineTriggers.REPEATER_MAX_DELAY.trigger(serverPlayer);
        }
    }
}