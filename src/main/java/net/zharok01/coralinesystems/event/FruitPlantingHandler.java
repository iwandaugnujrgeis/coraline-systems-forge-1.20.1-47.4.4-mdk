package net.zharok01.coralinesystems.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.block.HangingFruitBlock;
import net.zharok01.coralinesystems.registry.CoralineBlocks;
import net.zharok01.coralinesystems.registry.CoralineItems;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FruitPlantingHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        if (event.getFace() != Direction.DOWN) {
            return;
        }

        Player player = event.getEntity();
        ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (held.isEmpty()) {
            return;
        }

        Item heldItem = held.getItem();
        HangingFruitBlock fruitBlock = coraline$fruitBlockFor(heldItem);
        if (fruitBlock == null) {
            return;
        }

        Level level = event.getLevel();
        BlockPos clickedPos = event.getPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        if (!clickedState.is(fruitBlock.getValidLeafTag())) {
            return;
        }

        BlockPos belowPos = clickedPos.below();
        if (!level.isEmptyBlock(belowPos)) {
            return;
        }

        BlockState fruitState = fruitBlock.defaultBlockState();
        if (!fruitState.canSurvive(level, belowPos)) {
            return;
        }

        if (level.isClientSide) {
            player.swing(InteractionHand.MAIN_HAND, false);
            event.setCanceled(true);
            return;
        }

        level.setBlock(belowPos, fruitState, Block.UPDATE_ALL);
        level.playSound(null, belowPos, SoundEvents.CROP_PLANTED,
                SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);

        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }

        event.setCanceled(true);
    }

    private static HangingFruitBlock coraline$fruitBlockFor(Item item) {
        if (item == CoralineItems.APPLE.get())  return (HangingFruitBlock) CoralineBlocks.APPLE_FRUIT.get();
        if (item == CoralineItems.BANANA.get()) return (HangingFruitBlock) CoralineBlocks.BANANA_FRUIT.get();
        if (item == CoralineItems.MANGO.get())  return (HangingFruitBlock) CoralineBlocks.MANGO_FRUIT.get();
        if (item == CoralineItems.WALNUT.get()) return (HangingFruitBlock) CoralineBlocks.WALNUT_FRUIT.get();
        return null;
    }
}
