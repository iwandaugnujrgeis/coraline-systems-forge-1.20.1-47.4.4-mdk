package net.zharok01.coralinesystems.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntitySugarGlider;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class GliderSleepGoal extends Goal {

    private static final int SEARCH_RADIUS = 12;
    private static final int SEARCH_HEIGHT_UP = 14;
    private static final int SEARCH_HEIGHT_DOWN = 3;
    private static final int SEARCH_ATTEMPTS = 8;
    private static final int START_CHANCE = 100;

    private static final double coraline$PATH_SPEED = 1.0D;
    private static final double SETTLE_DISTANCE_SQ = 1.5D;
    private static final int APPROACH_TIMEOUT_TICKS = 200;

    private static final double SPIDER_ALARM_RANGE = 8.0D;
    private static final int ALARM_CHECK_INTERVAL_TICKS = 10;

    private final EntitySugarGlider glider;
    private BlockPos perchSpot;
    private Path coraline$perchPath;
    private int approachTicks;

    public GliderSleepGoal(EntitySugarGlider glider) {
        this.glider = glider;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private boolean isSpiderNearby() {
        return GliderKiteSpiderGoal.findNearestSpider(this.glider, SPIDER_ALARM_RANGE) != null;
    }

    private boolean isDisturbed() {
        return this.glider.hurtTime > 0 || this.glider.getLastHurtByMob() != null;
    }

    private boolean isPerchSpot(BlockPos pos) {
        return this.glider.level().getBlockState(pos.below()).is(BlockTags.LEAVES)
                && this.glider.level().isEmptyBlock(pos)
                && this.glider.level().isEmptyBlock(pos.above())
                && this.glider.level().canSeeSky(pos);
    }

    private BlockPos findPerchSpot() {
        this.coraline$perchPath = null;
        BlockPos origin = this.glider.blockPosition();
        if (this.isPerchSpot(origin)) return origin;

        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();
        for (int attempt = 0; attempt < SEARCH_ATTEMPTS; attempt++) {
            int x = origin.getX() + this.glider.getRandom().nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int z = origin.getZ() + this.glider.getRandom().nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            for (int y = origin.getY() + SEARCH_HEIGHT_UP; y >= origin.getY() - SEARCH_HEIGHT_DOWN; y--) {
                probe.set(x, y, z);
                if (this.isPerchSpot(probe)) {
                    BlockPos perch = probe.immutable();
                    Path path = this.glider.getNavigation().createPath(perch, 0);
                    if (path != null && path.canReach()) {
                        this.coraline$perchPath = path;
                        return perch;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean canUse() {
        if (!this.glider.level().isDay()) {
            if (SugarGliderData.isSleeping(this.glider)) {
                SugarGliderData.setSleeping(this.glider, false);
            }
            return false;
        }

        if (this.isDisturbed() || this.isSpiderNearby()) return false;
        if (SugarGliderData.isSleeping(this.glider)) return true;

        if (!this.glider.onGround() || this.glider.getRandom().nextInt(reducedTickDelay(START_CHANCE)) != 0) return false;

        this.perchSpot = this.findPerchSpot();
        return this.perchSpot != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.glider.level().isDay() || this.isDisturbed()) return false;

        if (SugarGliderData.isSleeping(this.glider)) {
            return this.glider.tickCount % ALARM_CHECK_INTERVAL_TICKS != 0 || !this.isSpiderNearby();
        }
        return this.approachTicks < APPROACH_TIMEOUT_TICKS;
    }

    @Override
    public void start() {
        this.approachTicks = 0;
        if (SugarGliderData.isSleeping(this.glider)) {
            this.holdStill();
        } else if (this.coraline$perchPath != null) {
            this.glider.getNavigation().moveTo(this.coraline$perchPath, coraline$PATH_SPEED);
        }
    }

    @Override
    public void stop() {
        SugarGliderData.setSleeping(this.glider, false);
        this.glider.setGliding(false);
        this.glider.getNavigation().stop();
        this.perchSpot = null;
        this.coraline$perchPath = null;
    }

    @Override
    public void tick() {
        if (SugarGliderData.isSleeping(this.glider)) return;

        this.approachTicks++;
        Vec3 perchFloor = Vec3.atBottomCenterOf(this.perchSpot);
        double distanceSq = this.glider.position().distanceToSqr(perchFloor);

        if (this.glider.onGround() && distanceSq < SETTLE_DISTANCE_SQ) {
            SugarGliderData.setSleeping(this.glider, true);
            this.holdStill();
        }
    }

    private void holdStill() {
        this.glider.getNavigation().stop();
        this.glider.setGliding(false);
        this.glider.getMoveControl().setWantedPosition(this.glider.getX(), this.glider.getY(), this.glider.getZ(), 0.0D);
    }
}