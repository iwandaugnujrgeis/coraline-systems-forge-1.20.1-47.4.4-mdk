package net.zharok01.coralinesystems.registry;

import com.github.alexthe666.alexsmobs.client.particle.AMParticleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

public class CoralineUtils {

    /**
     * Teleports an entity to (x, y, z), finding the nearest safe floor.
     *
     * The floor search walks DOWNWARD from the requested Y. It is capped at
     * MAX_FLOOR_DROP blocks below the requested Y — without this cap, if the
     * entity tries to land on an elevated surface (player on a cliff/building)
     * the search would silently fall all the way to the bedrock-level ground,
     * making the entity appear to ignore the target's height entirely.
     *
     * If no floor is found within MAX_FLOOR_DROP blocks, the teleport fails
     * and the entity stays at its old position — the caller may retry with a
     * different Y, which is what teleportTowards() does over 10 attempts.
     */
    private static final int MAX_FLOOR_DROP = 8;

    public static boolean randomTeleportStatic(LivingEntity entity, double x, double y, double z, boolean showParticles) {
        double oldX = entity.getX();
        double oldY = entity.getY();
        double oldZ = entity.getZ();
        Level level = entity.level();

        BlockPos targetPos = BlockPos.containing(x, y, z);
        if (!level.hasChunkAt(targetPos)) return false;

        // Walk downward from the requested Y looking for a solid floor,
        // but give up after MAX_FLOOR_DROP blocks so we don't land on
        // ground far below the intended destination (e.g. below a cliff
        // or elevated platform the target is standing on).
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
            // No safe floor within range — fail cleanly so the caller can retry
            return false;
        }

        // Teleport to the found position and verify the entity fits there
        entity.teleportTo(x, y, z);

        if (level.noCollision(entity) && !level.containsAnyLiquid(entity.getBoundingBox())) {
            if (entity instanceof PathfinderMob mob) {
                mob.getNavigation().stop();
            }

            if (showParticles && level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(AMParticleRegistry.STATIC_SPARK.get(),
                        x, entity.getY() + 1.0D, z,
                        10, 0.2, 0.5, 0.2, 0.02);
                serverLevel.sendParticles(AMParticleRegistry.STATIC_SPARK.get(),
                        oldX, oldY + 1.0D, oldZ,
                        10, 0.2, 0.5, 0.2, 0.02);
            }
            return true;
        }

        // Destination is blocked — revert
        entity.teleportTo(oldX, oldY, oldZ);
        return false;
    }
}