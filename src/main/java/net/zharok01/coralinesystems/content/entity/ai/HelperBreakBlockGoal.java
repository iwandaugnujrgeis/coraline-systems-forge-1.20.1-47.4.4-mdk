package net.zharok01.coralinesystems.content.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.zharok01.coralinesystems.content.entity.custom.HelperEntity;
import net.zharok01.coralinesystems.registry.CoralineTags;

import java.util.EnumSet;

public class HelperBreakBlockGoal extends Goal {
    private final HelperEntity helper;
    private BlockPos targetPos;
    private int breakingTime;
    private int cooldownTicks = 0;

    private static final int MAX_BREAKING_TIME = 40;
    private static final int COOLDOWN_AFTER_MINE = 400;

    public HelperBreakBlockGoal(HelperEntity helper) {
        this.helper = helper;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // 1. MUST BE JAMMING (Music is playing)
        if (!helper.isJamming()) {
            return false;
        }

        // 2. Cooldown check
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return false;
        }

        // 3. Carrying check
        BlockState carried = helper.getCarriedBlock();
        if (carried != null && !carried.isAir()) {
            return false;
        }

        this.targetPos = findTargetBlock();
        return this.targetPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        // If the music stops (isJamming becomes false), stop mining immediately
        return helper.isJamming() && targetPos != null &&
                !helper.level().getBlockState(targetPos).isAir() &&
                breakingTime < MAX_BREAKING_TIME;
    }

    private BlockPos findTargetBlock() {
        BlockPos pos = helper.blockPosition().relative(helper.getDirection());
        BlockState state = helper.level().getBlockState(pos);

        // NEW: Check if the block is in our custom tag
        if (state.is(CoralineTags.HELPER_MINEABLE)) {
            return pos;
        }

        BlockPos eyePos = pos.above();
        BlockState eyeState = helper.level().getBlockState(eyePos);
        if (eyeState.is(CoralineTags.HELPER_MINEABLE)) {
            return eyePos;
        }

        return null;
    }

    @Override
    public void start() {
        this.breakingTime = 0;
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

        int progress = (int) ((float) breakingTime / (float) MAX_BREAKING_TIME * 10.0F);
        if (progress < 10) {
            this.helper.level().destroyBlockProgress(this.helper.getId(), targetPos, progress);
        }

        breakingTime++;

        if (breakingTime >= MAX_BREAKING_TIME) {
            Level level = helper.level();
            BlockState state = level.getBlockState(targetPos);

            if (!level.isClientSide) {
                level.destroyBlockProgress(this.helper.getId(), targetPos, -1);
                this.helper.setCarriedBlock(state);
                level.destroyBlock(targetPos, false);
            }
            this.cooldownTicks = COOLDOWN_AFTER_MINE;
            this.stop();
        }
    }

    @Override
    public void stop() {
        if (targetPos != null) {
            this.helper.level().destroyBlockProgress(this.helper.getId(), targetPos, -1);
        }
        this.targetPos = null;
    }
}