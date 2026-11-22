package net.zharok01.coralinesystems.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.zharok01.coralinesystems.entity.CryptidEntity;
import net.zharok01.coralinesystems.registry.CoralineSounds;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class CryptidKnockOnWoodGoal extends Goal {

    private static final int SCAN_RADIUS = 6;
    private static final int LINGER_TICKS = 40;
    private static final int MIN_COOLDOWN = 160;
    private static final int MAX_COOLDOWN = 400;
    private static final int KNOCK_COUNT = 3;
    private static final int KNOCK_INTERVAL = 10;

    private final CryptidEntity cryptid;
    private final Level level;

    @Nullable
    private BlockPos targetLog;
    private int cooldown;
    private int lingerTimer;
    private int knocksRemaining;
    private int knockTimer;

    public CryptidKnockOnWoodGoal(CryptidEntity cryptid) {
        this.cryptid = cryptid;
        this.level = cryptid.level();
        this.cooldown = MIN_COOLDOWN;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        if (this.cryptid.isFleeing()) return false;

        this.targetLog = findNearbyLog();
        return this.targetLog != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.cryptid.isFleeing()) return false;
        if (this.targetLog == null) return false;

        if (!this.cryptid.getNavigation().isDone()) return true;

        return this.lingerTimer > 0 || this.knocksRemaining > 0;
    }

    @Override
    public void start() {
        if (this.targetLog == null) return;

        Path path = this.cryptid.getNavigation().createPath(this.targetLog, 1);
        if (path != null) {
            this.cryptid.getNavigation().moveTo(path, 1.0D);
        }

        this.lingerTimer = LINGER_TICKS;
        this.knocksRemaining = KNOCK_COUNT;
        this.knockTimer = 0;
    }

    @Override
    public void tick() {
        if (!this.cryptid.getNavigation().isDone() || this.targetLog == null) return;

        this.cryptid.getLookControl().setLookAt(
                this.targetLog.getX() + 0.5,
                this.targetLog.getY() + 0.5,
                this.targetLog.getZ() + 0.5
        );

        if (this.knocksRemaining > 0) {
            if (this.knockTimer <= 0) {
                this.level.playSound(
                        null,
                        this.targetLog.getX() + 0.5,
                        this.targetLog.getY() + 0.5,
                        this.targetLog.getZ() + 0.5,
                        CoralineSounds.CRYPTID_KNOCK.get(),
                        SoundSource.NEUTRAL,
                        1.0F,
                        0.9F + this.cryptid.getRandom().nextFloat() * 0.2F
                );
                this.knocksRemaining--;
                this.knockTimer = KNOCK_INTERVAL;
            } else {
                this.knockTimer--;
            }
        } else {
            this.lingerTimer--;
        }
    }

    @Override
    public void stop() {
        this.targetLog = null;
        this.lingerTimer = 0;
        this.knocksRemaining = 0;
        this.knockTimer = 0;
        this.cooldown = MIN_COOLDOWN + this.cryptid.getRandom().nextInt(MAX_COOLDOWN - MIN_COOLDOWN);
    }

    @Nullable
    private BlockPos findNearbyLog() {
        BlockPos origin = this.cryptid.blockPosition();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;

        for (BlockPos candidate : BlockPos.betweenClosed(
                origin.offset(-SCAN_RADIUS, -2, -SCAN_RADIUS),
                origin.offset(SCAN_RADIUS, 4, SCAN_RADIUS))) {

            if (!this.level.getBlockState(candidate).is(BlockTags.LOGS)) continue;

            double dist = candidate.distSqr(origin);
            if (dist < bestDist) {
                bestDist = dist;
                best = candidate.immutable();
            }
        }

        return best;
    }
}
