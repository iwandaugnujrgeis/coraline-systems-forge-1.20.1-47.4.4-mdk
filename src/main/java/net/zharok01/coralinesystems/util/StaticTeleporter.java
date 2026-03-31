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

    private static final int FARLANDS_SHIFT = 1_000_000;
    private static final int SEARCH_RADIUS = 16;

    /**
     * Custom cooldown map keyed by entity UUID, measured in game ticks.
     *
     * WHY we don't use entity.setPortalCooldown() / entity.isOnPortalCooldown():
     * Vanilla's portal cooldown system is built for CROSS-DIMENSION teleports. For same-dimension
     * teleports like ours, calling entity.teleportTo() can clear or bypass that cooldown before
     * the destination portal has a chance to check it — causing the instant round-trip bug where
     * the player flickers back and forth between the two portals forever.
     *
     * Using our own map that is checked BEFORE teleportTo() is called sidesteps this entirely.
     */
    private static final Map<UUID, Long> teleportCooldowns = new HashMap<>();

    /** 300 ticks = 15 seconds. Long enough that the player clears the destination portal area. */
    private static final long COOLDOWN_TICKS = 300L;

    public static void teleportToFarlands(Entity entity, ServerLevel level, BlockPos portalPos) {
        UUID id = entity.getUUID();
        long currentTick = level.getGameTime();

        // Primary guard: our own cooldown map
        Long lastTeleportTick = teleportCooldowns.get(id);
        if (lastTeleportTick != null && currentTick - lastTeleportTick < COOLDOWN_TICKS) return;

        // Log this teleport before doing anything else
        teleportCooldowns.put(id, currentTick);

        // Secondary guard: vanilla cooldown as a belt-and-suspenders fallback
        entity.setPortalCooldown();

        // --- The Threshold Shift ---
        // If the entity is on the "normal" side (X < 12_550_821), send them to the Farlands.
        // If they're already in the Farlands (X >= 12,550,821), bring them back.
        double destX = entity.getX() < 12_550_821 ? entity.getX() + FARLANDS_SHIFT : entity.getX() - FARLANDS_SHIFT;
        double destZ = entity.getZ() < 12_550_821 ? entity.getZ() + FARLANDS_SHIFT : entity.getZ() - FARLANDS_SHIFT;
        double destY = entity.getY();

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
        double spawnY = finalDestPos.getY();         // bottom of the portal — player stands on the obsidian floor
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
                        // Walk down to the bottom-most portal block in this column
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

    /**
     * Generates a standard 2-wide × 3-tall portal with an obsidian frame.
     * Also carves air on both faces of the portal interior so the player can't suffocate.
     */
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

                    // Clear one block on each side of the portal so players don't get stuck
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
