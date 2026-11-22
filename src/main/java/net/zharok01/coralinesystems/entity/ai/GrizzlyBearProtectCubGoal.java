package net.zharok01.coralinesystems.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityGrizzlyBear;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;

public class GrizzlyBearProtectCubGoal extends NearestAttackableTargetGoal<Player> {
    private final EntityGrizzlyBear bear;

    public GrizzlyBearProtectCubGoal(EntityGrizzlyBear bear) {
        super(bear, Player.class, 20, true, true, null);
        this.bear = bear;
    }

    @Override
    public boolean canUse() {
        if (this.bear.isBaby()) return false;

        if (super.canUse()) {
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
        return super.getFollowDistance() * 0.5;
    }
}
