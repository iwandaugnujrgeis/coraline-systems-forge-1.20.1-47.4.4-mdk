package net.zharok01.coralinesystems.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.zharok01.coralinesystems.registry.CoralineBlocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class StaticTeleporter {

    private static final double FARLANDS_EDGE = 12_550_821.0;
    private static final int SEARCH_RADIUS = 16;

    /**
     * Custom cooldown map keyed by entity UUID, measured in game ticks.
     */
    private static final Map<UUID, Long> teleportCooldowns = new HashMap<>();

    /** 300 ticks = 15 seconds. Long enough that the player clears the destination portal area. */
    private static final long COOLDOWN_TICKS = 100L;

    public static void teleportToFarlands(Entity entity, ServerLevel level, BlockPos portalPos) {
        UUID id = entity.getUUID();
        long currentTick = level.getGameTime();

        // Primary guard: our own cooldown map
        Long lastTeleportTick = teleportCooldowns.get(id);
        if (lastTeleportTick != null && currentTick - lastTeleportTick < COOLDOWN_TICKS) return;

        // Log this teleport before doing anything else
        teleportCooldowns.put(id, currentTick);

        // Secondary guard: vanilla cooldown
        entity.setPortalCooldown();

        // --- The Threshold Shift (Directional) ---
        double currentX = entity.getX();
        double currentZ = entity.getZ();
        double destY = entity.getY();

        double destX = currentX;
        double destZ = currentZ;

        // Get the direction the entity is currently looking
        Direction facing = entity.getDirection();

        // Check if they are already in the Farlands (on either axis)
        boolean inFarlandsX = Math.abs(currentX) >= FARLANDS_EDGE;
        boolean inFarlandsZ = Math.abs(currentZ) >= FARLANDS_EDGE;

        if (inFarlandsX || inFarlandsZ) {
            // If they are IN the Farlands, bring them back towards 0,0
            // We subtract the distance they traveled along their current axis
            if (facing.getAxis() == Direction.Axis.X) {
                destX = currentX > 0 ? currentX - FARLANDS_EDGE : currentX + FARLANDS_EDGE;
            } else if (facing.getAxis() == Direction.Axis.Z) {
                destZ = currentZ > 0 ? currentZ - FARLANDS_EDGE : currentZ + FARLANDS_EDGE;
            }
        } else {
            // If they are in the NORMAL world, send them TO the Farlands
            // The distance added/subtracted depends on the direction they are facing
            switch (facing) {
                case EAST:  // Positive X
                    destX = FARLANDS_EDGE;
                    break;
                case WEST:  // Negative X
                    destX = -FARLANDS_EDGE;
                    break;
                case SOUTH: // Positive Z
                    destZ = FARLANDS_EDGE;
                    break;
                case NORTH: // Negative Z
                    destZ = -FARLANDS_EDGE;
                    break;
                default:
                    // Fallback just in case (e.g., facing straight up/down)
                    destX = FARLANDS_EDGE;
                    break;
            }
        }

        BlockPos roughDest = new BlockPos((int) destX, (int) destY, (int) destZ);

        // --- Find or Build Exit Portal ---
        Optional<BlockPos> existingPortal = findExistingPortal(level, roughDest);

        BlockPos finalDestPos;
        Direction.Axis axis;

        if (existingPortal.isPresent()) {
            finalDestPos = existingPortal.get();
            BlockState existingState = level.getBlockState(finalDestPos);
            axis = existingState.hasProperty(NetherPortalBlock.AXIS)
                    ? existingState.getValue(NetherPortalBlock.AXIS)
                    : Direction.Axis.X;
        } else {
            finalDestPos = roughDest;
            axis = level.getBlockState(portalPos).hasProperty(NetherPortalBlock.AXIS)
                    ? level.getBlockState(portalPos).getValue(NetherPortalBlock.AXIS)
                    : Direction.Axis.X;
            generateExitPortal(level, finalDestPos, axis);
        }

        // --- Teleport ---
        double spawnX = finalDestPos.getX() + 0.5;
        double spawnY = finalDestPos.getY();
        double spawnZ = finalDestPos.getZ() + 0.5;

        // Face away from the portal so you "walk out" of it naturally
        float invertedYaw = entity.getYRot() + 180.0F;

        entity.teleportTo(spawnX, spawnY, spawnZ);
        entity.setYRot(invertedYaw);
        entity.setYHeadRot(invertedYaw);

        level.playSound(null, spawnX, spawnY, spawnZ,
                SoundEvents.LIGHTNING_BOLT_IMPACT,
                SoundSource.MASTER,
                1.5F, 0.3F);
    }

    private static Optional<BlockPos> findExistingPortal(ServerLevel level, BlockPos center) {
        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
            for (int y = -SEARCH_RADIUS; y <= SEARCH_RADIUS; y++) {
                for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (level.getBlockState(pos).is(CoralineBlocks.STATIC_PORTAL_BLOCK.get())) {
                        int dropToBottom = getDistanceToBottom(level, pos);
                        return Optional.of(pos.below(dropToBottom));
                    }
                }
            }
        }
        return Optional.empty();
    }

    private static int getDistanceToBottom(ServerLevel level, BlockPos pos) {
        int drop = 0;
        while (drop < 5 && level.getBlockState(pos.below(drop + 1)).is(CoralineBlocks.STATIC_PORTAL_BLOCK.get())) {
            drop++;
        }
        return drop;
    }

    private static void generateExitPortal(ServerLevel level, BlockPos pos, Direction.Axis axis) {
        BlockState portalState = CoralineBlocks.STATIC_PORTAL_BLOCK.get()
                .defaultBlockState()
                .setValue(NetherPortalBlock.AXIS, axis);
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        BlockState air     = Blocks.AIR.defaultBlockState();

        for (int lateral = -1; lateral <= 2; lateral++) {
            for (int vertical = -1; vertical <= 3; vertical++) {
                BlockPos target = pos.offset(
                        axis == Direction.Axis.X ? lateral : 0,
                        vertical,
                        axis == Direction.Axis.Z ? lateral : 0
                );

                boolean isInterior = (lateral == 0 || lateral == 1) && (vertical >= 0 && vertical <= 2);

                if (isInterior) {
                    level.setBlockAndUpdate(target, portalState);

                    BlockPos front = target.relative(axis == Direction.Axis.X ? Direction.SOUTH : Direction.EAST);
                    BlockPos back  = target.relative(axis == Direction.Axis.X ? Direction.NORTH : Direction.WEST);
                    if (!level.getBlockState(front).is(Blocks.OBSIDIAN)) level.setBlockAndUpdate(front, air);
                    if (!level.getBlockState(back).is(Blocks.OBSIDIAN))  level.setBlockAndUpdate(back,  air);
                } else {
                    level.setBlockAndUpdate(target, obsidian);
                }
            }
        }
    }
}