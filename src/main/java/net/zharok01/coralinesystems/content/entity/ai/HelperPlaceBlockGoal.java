package net.zharok01.coralinesystems.content.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.zharok01.coralinesystems.content.entity.custom.HelperEntity;

import java.util.EnumSet;

public class HelperPlaceBlockGoal extends Goal {
    private final HelperEntity helper;
    private final Level level;
    private int cooldownTicks = 0;

    public HelperPlaceBlockGoal(HelperEntity helper) {
        this.helper = helper;
        this.level = helper.level();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // 1. MUST BE JAMMING (Music is playing)
        if (!helper.isJamming()) {
            return false;
        }

        // 2. INTERNAL COOLDOWN
        // This prevents them from placing the block immediately after picking it up
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return false;
        }

        // 3. MUST BE CARRYING SOMETHING
        if (helper.getCarriedBlock().isAir()) {
            return false;
        }

        // 4. RANDOM CHANCE
        // A small 5% chance per check to place it, so they wander a bit first
        return helper.getRandom().nextInt(100) < 5;
    }

    @Override
    public void start() {
        // Find a spot directly in front of them
        BlockPos headPos = helper.blockPosition().relative(helper.getDirection());
        BlockPos placePos = null;

        // Check if the spot is air and there is solid ground beneath it
        if (level.getBlockState(headPos).isAir() && level.getBlockState(headPos.below()).isSolid()) {
            placePos = headPos;
        }

        if (placePos != null) {
            BlockState toPlace = helper.getCarriedBlock();

            // 1. Play the correct sound for the specific block being placed
            SoundType soundType = toPlace.getSoundType(level, placePos, helper);
            helper.playSound(soundType.getPlaceSound(), soundType.getVolume(), soundType.getPitch());

            // 2. Swing the main hand (Standard placement animation)
            helper.swing(InteractionHand.MAIN_HAND);

            // 3. Place the block in the world
            level.setBlock(placePos, toPlace, 3);

            // 4. Clear the Helper's hand
            helper.setCarriedBlock(null);

            // 5. Reset the cooldown for the next time they pick a block up
            this.cooldownTicks = 400; // 5 seconds of "walking time" next time
        }
    }

    @Override
    public boolean canContinueToUse() {
        // This is an instant action, so we don't need to continue ticking
        return false;
    }
}