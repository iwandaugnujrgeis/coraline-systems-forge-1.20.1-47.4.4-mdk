package net.zharok01.coralinesystems.event;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID)
public class OffhandTorchPriorityHandler {

    @SubscribeEvent
    @SuppressWarnings("null")
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        ItemStack offhandStack = event.getEntity().getOffhandItem();
        if (!(offhandStack.getItem() instanceof BlockItem blockItem)
                || !(blockItem.getBlock() instanceof TorchBlock)) {
            return;
        }
        UseOnContext context = new UseOnContext(event.getEntity(), event.getHand(), event.getHitVec());
        BlockState blockState = event.getLevel().getBlockState(event.getPos());
        ItemStack mainHandStack = event.getItemStack();
        boolean wouldModifyBlock = false;
        if (mainHandStack.canPerformAction(ToolActions.AXE_STRIP)) {
            if (blockState.getToolModifiedState(context, ToolActions.AXE_STRIP, false) != null) {
                wouldModifyBlock = true;
            } else if (blockState.getToolModifiedState(context, ToolActions.AXE_SCRAPE, false) != null) {
                wouldModifyBlock = true;
            } else {
                wouldModifyBlock = blockState.getToolModifiedState(context, ToolActions.AXE_WAX_OFF, false) != null;
            }
        } else if (mainHandStack.canPerformAction(ToolActions.HOE_TILL)
                && HoeItem.onlyIfAirAbove(context)) {
            wouldModifyBlock = blockState.getToolModifiedState(context, ToolActions.HOE_TILL, false) != null;
        }
        if (wouldModifyBlock) {
            event.setUseItem(Event.Result.DENY);
        }
    }
}
