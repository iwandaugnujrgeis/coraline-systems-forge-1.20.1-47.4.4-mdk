package net.zharok01.coralinesystems.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

public class CoralineUtils {

    private static final int MAX_FLOOR_DROP = 8;

    public static boolean randomTeleportStatic(LivingEntity entity, double x, double y, double z, boolean showParticles) {
        double oldX = entity.getX();
        double oldY = entity.getY();
        double oldZ = entity.getZ();
        Level level = entity.level();

        BlockPos targetPos = BlockPos.containing(x, y, z);
        if (!level.hasChunkAt(targetPos)) return false;

        int dropped = 0;
        boolean foundFloor = false;
        while (!foundFloor && dropped < MAX_FLOOR_DROP && targetPos.getY() > level.getMinBuildHeight()) {
            BlockPos below = targetPos.below();
            if (level.getBlockState(below).blocksMotion()) {
                foundFloor = true;
            } else {
                y--;
                targetPos = below;
                dropped++;
            }
        }

        if (!foundFloor) {
            return false;
        }

        entity.teleportTo(x, y, z);

        if (level.noCollision(entity) && !level.containsAnyLiquid(entity.getBoundingBox())) {
            if (entity instanceof PathfinderMob mob) {
                mob.getNavigation().stop();
            }

            if (showParticles && level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(CoralineParticles.DARK_SMOKE.get(),
                        x, entity.getY() + 1.0D, z,
                        10, 0.2, 0.5, 0.2, 0.02);
                serverLevel.sendParticles(CoralineParticles.DARK_SMOKE.get(),
                        oldX, oldY + 1.0D, oldZ,
                        10, 0.2, 0.5, 0.2, 0.02);
            }
            return true;
        }

        entity.teleportTo(oldX, oldY, oldZ);
        return false;
    }
}