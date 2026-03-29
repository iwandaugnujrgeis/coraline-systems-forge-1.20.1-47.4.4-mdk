package net.zharok01.coralinesystems.registry;

import com.github.alexthe666.alexsmobs.client.particle.AMParticleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

public class CoralineUtils {

    //Teleportation with Alex's Mobs' static particles!
    public static boolean randomTeleportStatic(LivingEntity entity, double x, double y, double z, boolean showParticles) {
        double oldX = entity.getX();
        double oldY = entity.getY();
        double oldZ = entity.getZ();
        Level level = entity.level();

        // Use the vanilla logic for finding a safe floor position
        BlockPos targetPos = BlockPos.containing(x, y, z);
        if (level.hasChunkAt(targetPos)) {
            boolean foundFloor = false;
            while (!foundFloor && targetPos.getY() > level.getMinBuildHeight()) {
                BlockPos below = targetPos.below();
                if (level.getBlockState(below).blocksMotion()) {
                    foundFloor = true;
                } else {
                    y--;
                    targetPos = below;
                }
            }

            if (foundFloor) {
                // Perform the actual move
                entity.teleportTo(x, y, z);

                // Check for collisions at destination
                if (level.noCollision(entity) && !level.containsAnyLiquid(entity.getBoundingBox())) {
                    if (entity instanceof PathfinderMob mob) {
                        mob.getNavigation().stop();
                    }

                    if (showParticles && level instanceof ServerLevel serverLevel) {
                        // Spawn STATIC_SPARK particles at the NEW position
                        serverLevel.sendParticles(AMParticleRegistry.STATIC_SPARK.get(),
                                x, entity.getY() + 1.0D, z,
                                10, 0.2, 0.5, 0.2, 0.02);

                        // Optional: Spawn them at the OLD position too for a "fade" effect
                        serverLevel.sendParticles(AMParticleRegistry.STATIC_SPARK.get(),
                                oldX, oldY + 1.0D, oldZ,
                                10, 0.2, 0.5, 0.2, 0.02);
                    }
                    return true;
                }
            }
        }

        // If it fails, revert to old position
        entity.teleportTo(oldX, oldY, oldZ);
        return false;
    }
}


