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
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // FIX: Safer check for the carried block
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
        this.helper.setJamming(true);
    }

    @Override
    public void tick() {
        if (targetPos == null) return;

        this.helper.getLookControl().setLookAt(targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D);

        if (breakingTime % 10 == 0) {
            BlockState state = helper.level().getBlockState(targetPos);
            SoundType soundType = state.getSoundType(helper.level(), targetPos, helper);
            helper.playSound(soundType.getHitSound(), soundType.getVolume(), soundType.getPitch());
            this.helper.swing(this.helper.getUsedItemHand());
        }

        breakingTime++;

        if (breakingTime >= MAX_BREAKING_TIME) {
            Level level = helper.level();
            BlockState state = level.getBlockState(targetPos);

            if (!level.isClientSide) {
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
        this.helper.setJamming(false);
        this.targetPos = null;
    }
}