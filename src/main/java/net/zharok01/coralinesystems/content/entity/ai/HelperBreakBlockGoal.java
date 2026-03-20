package net.zharok01.coralinesystems.content.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.zharok01.coralinesystems.content.entity.custom.HelperEntity;

import java.util.EnumSet;

public class HelperBreakBlockGoal extends Goal {
    private final HelperEntity helper;
    private BlockPos targetPos;
    private int breakingTime;
    private static final int MAX_BREAKING_TIME = 40; // 2 seconds (20 ticks = 1s)

    public HelperBreakBlockGoal(HelperEntity helper) {
        this.helper = helper;
        // Flag.MOVE prevents them from wandering off mid-mine
        // Flag.LOOK allows the AI to control the head orientation
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // Safer check for the carried block to prevent NullPointerExceptions
        BlockState carried = helper.getCarriedBlock();
        if (carried != null && !carried.isAir()) {
            return false;
        }

        this.targetPos = findTargetBlock();
        return this.targetPos != null;
    }

    private BlockPos findTargetBlock() {
        // Look for a block at feet level in front of them
        BlockPos pos = helper.blockPosition().relative(helper.getDirection());
        if (!helper.level().getBlockState(pos).isAir()) return pos;

        // Also check eye level
        BlockPos eyePos = pos.above();
        if (!helper.level().getBlockState(eyePos).isAir()) return eyePos;

        return null;
    }

    @Override
    public void start() {
        this.breakingTime = 0;
        // Trigger the "Crazy Jamming" arms in your Mixin
        this.helper.setJamming(true);
    }

    @Override
    public void tick() {
        if (targetPos == null) return;

        // 1. Face the block
        this.helper.getLookControl().setLookAt(targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D);

        // 2. Play sound effects and swing arm every 10 ticks
        if (breakingTime % 10 == 0) {
            BlockState state = helper.level().getBlockState(targetPos);
            SoundType soundType = state.getSoundType(helper.level(), targetPos, helper);
            helper.playSound(soundType.getHitSound(), soundType.getVolume(), soundType.getPitch());
            this.helper.swing(this.helper.getUsedItemHand());
        }

        // 3. Update block cracking overlay (0-10 progress stages)
        int progress = (int) ((float) breakingTime / (float) MAX_BREAKING_TIME * 10.0F);
        if (progress < 10) {
            this.helper.level().destroyBlockProgress(this.helper.getId(), targetPos, progress);
        }

        breakingTime++;

        // 4. Finalize breaking
        if (breakingTime >= MAX_BREAKING_TIME) {
            Level level = helper.level();
            BlockState state = level.getBlockState(targetPos);

            if (!level.isClientSide) {
                // Clear the cracks immediately before the block is removed
                level.destroyBlockProgress(this.helper.getId(), targetPos, -1);

                this.helper.setCarriedBlock(state);
                level.destroyBlock(targetPos, false);
            }
            this.stop();
        }
    }

    @Override
    public boolean canContinueToUse() {
        return targetPos != null && !helper.level().getBlockState(targetPos).isAir() && breakingTime < MAX_BREAKING_TIME;
    }

    @Override
    public void stop() {
        // Clear the cracking overlay if the goal stops for any reason
        if (targetPos != null) {
            this.helper.level().destroyBlockProgress(this.helper.getId(), targetPos, -1);
        }

        this.helper.setJamming(false);
        this.targetPos = null;
    }
}