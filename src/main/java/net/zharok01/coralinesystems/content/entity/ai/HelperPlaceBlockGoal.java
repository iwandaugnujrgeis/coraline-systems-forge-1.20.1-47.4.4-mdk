package net.zharok01.coralinesystems.content.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

    public HelperPlaceBlockGoal(HelperEntity helper) {
        this.helper = helper;
        this.level = helper.level();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // Only place if carrying a block and standing on/near solid ground
        return !helper.getCarriedBlock().isAir() && helper.getRandom().nextInt(100) < 5;
    }

    @Override
    public void start() {
        BlockPos headPos = helper.blockPosition().relative(helper.getDirection());
        BlockPos placePos = null;

        // Try to find a spot: directly in front, or in front and down one (the floor)
        if (level.getBlockState(headPos).isAir() && level.getBlockState(headPos.below()).isSolid()) {
            placePos = headPos;
        }

        if (placePos != null) {
            BlockState toPlace = helper.getCarriedBlock();

            // 1. Play the correct sound for that specific block
            SoundType soundType = toPlace.getSoundType(level, placePos, helper);
            helper.playSound(soundType.getPlaceSound(), soundType.getVolume(), soundType.getPitch());

            // 2. Swing the arm (Player-like movement)
            helper.swing(InteractionHand.MAIN_HAND);

            // 3. Place the block in the world
            level.setBlock(placePos, toPlace, 3);

            // 4. Clear the Helper's hand
            helper.setCarriedBlock(null);
        }
    }
}