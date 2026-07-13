package net.zharok01.coralinesystems.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityGrizzlyBear;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;

public class GrizzlyBearProtectCubGoal extends NearestAttackableTargetGoal<Player> {
    private final EntityGrizzlyBear bear;

    public GrizzlyBearProtectCubGoal(EntityGrizzlyBear bear) {
        // Range 20 blocks, must see target, don't check for anger
        super(bear, Player.class, 20, true, true, null);
        this.bear = bear;
    }

    @Override
    public boolean canUse() {
        // If the checking bear is a baby, it doesn't protect others
        if (this.bear.isBaby()) return false;

        if (super.canUse()) {
            // Scan nearby area (8 block radius) for cubs
            for (EntityGrizzlyBear nearbyBear : this.bear.level().getEntitiesOfClass(EntityGrizzlyBear.class, this.bear.getBoundingBox().inflate(8.0, 4.0, 8.0))) {
                if (nearbyBear.isBaby()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected double getFollowDistance() {
        // Halve the tracking distance so they don't chase you to the ends of the earth
        return super.getFollowDistance() * 0.5;
    }
}