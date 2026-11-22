package net.zharok01.coralinesystems.entity.ai;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.EntityRaccoon;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.ArrayList;
import java.util.List;

public class RaccoonScatterChestGoal extends MoveToBlockGoal {

    private static final int SEARCH_RANGE = 16;
    private static final double REACH_DISTANCE = 2.0D;
    private static final int MIN_THROWS = 3;
    private static final int EXTRA_THROWS = 3;
    private static final int THROW_INTERVAL_TICKS = 8;
    private static final int POST_SCATTER_COOLDOWN_TICKS = 600;
    private static final int STAND_TICKS = 15;
    private static final double MIN_THROW_SPEED = 0.12D;
    private static final double EXTRA_THROW_SPEED = 0.18D;

    private final EntityRaccoon raccoon;
    private boolean chestOpened;
    private boolean scatterFinished;
    private int throwsLeft;
    private int throwDelay;

    public RaccoonScatterChestGoal(EntityRaccoon raccoon) {
        super(raccoon, 1.0D, SEARCH_RANGE);
        this.raccoon = raccoon;
    }

    @Override
    public boolean canUse() {
        if (this.raccoon.isTame() || !AMConfig.raccoonsStealFromChests) return false;
        if (this.nextStartTick <= 0 && !ForgeEventFactory.getMobGriefingEvent(this.raccoon.level(), this.raccoon)) return false;
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !this.scatterFinished && super.canContinueToUse();
    }

    @Override
    public double acceptedDistance() {
        return REACH_DISTANCE;
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof Container container
                && !container.isEmpty()
                && (this.chestOpened || this.raccoon.isLootable(container));
    }

    @Override
    public void start() {
        super.start();
        this.chestOpened = false;
        this.scatterFinished = false;
        this.throwDelay = 0;
    }

    @Override
    public void stop() {
        super.stop();
        if (this.chestOpened) {
            this.coraline$toggleChest(false);
            this.nextStartTick = reducedTickDelay(POST_SCATTER_COOLDOWN_TICKS);
        }
        this.chestOpened = false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isReachedTarget()) return;

        this.raccoon.getLookControl().setLookAt(Vec3.atCenterOf(this.blockPos));

        if (!this.chestOpened) {
            this.chestOpened = true;
            this.throwsLeft = MIN_THROWS + this.raccoon.getRandom().nextInt(EXTRA_THROWS + 1);
            this.coraline$toggleChest(true);
        }

        if (--this.throwDelay > 0) return;
        this.throwDelay = THROW_INTERVAL_TICKS;

        this.coraline$throwRandomStack();
        if (--this.throwsLeft <= 0) {
            this.scatterFinished = true;
        }
    }

    private void coraline$throwRandomStack() {
        if (!(this.raccoon.level().getBlockEntity(this.blockPos) instanceof Container container)) return;

        List<Integer> filledSlots = new ArrayList<>();
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            if (!container.getItem(slot).isEmpty()) {
                filledSlots.add(slot);
            }
        }

        RandomSource random = this.raccoon.getRandom();
        int slot = filledSlots.get(random.nextInt(filledSlots.size()));
        ItemStack thrown = container.removeItem(slot, container.getItem(slot).getCount());

        ItemEntity drop = new ItemEntity(
                this.raccoon.level(),
                this.blockPos.getX() + 0.5D,
                this.blockPos.getY() + 1.0D,
                this.blockPos.getZ() + 0.5D,
                thrown
        );
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double speed = MIN_THROW_SPEED + random.nextDouble() * EXTRA_THROW_SPEED;
        drop.setDeltaMovement(Math.cos(angle) * speed, 0.3D + random.nextDouble() * 0.1D, Math.sin(angle) * speed);
        this.raccoon.level().addFreshEntity(drop);

        this.raccoon.setStanding(true);
        this.raccoon.maxStandTime = STAND_TICKS;
        this.raccoon.playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, 1.0F, 0.8F + random.nextFloat() * 0.4F);
    }

    private void coraline$toggleChest(boolean open) {
        if (this.raccoon.level().getBlockEntity(this.blockPos) instanceof ChestBlockEntity chest) {
            Block block = chest.getBlockState().getBlock();
            this.raccoon.level().blockEvent(this.blockPos, block, 1, open ? 1 : 0);
            this.raccoon.level().blockUpdated(this.blockPos, block);
            this.raccoon.level().blockUpdated(this.blockPos.below(), block);
        }
    }
}