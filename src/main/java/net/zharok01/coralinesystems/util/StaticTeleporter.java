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

import java.util.*;

public class StaticTeleporter {

    // The coordinate where the Farlands begin
    private static final double FARLANDS_EDGE = 12_550_821.0;

    // How far to scan when looking for an existing linked or unlinked exit portal
    private static final int SEARCH_RADIUS = 16;

    // Custom cooldown: 100 ticks (5 s). Prevents re-entry jitter right after landing.
    private static final Map<UUID, Long> teleportCooldowns = new HashMap<>();
    private static final long COOLDOWN_TICKS = 100L;

    // -------------------------------------------------------------------------
    // Entry point — called by StaticPortalBlock.entityInside after the countdown
    // -------------------------------------------------------------------------

    public static void teleportToFarlands(Entity entity, ServerLevel level, BlockPos portalPos) {
        UUID id = entity.getUUID();
        long currentTick = level.getGameTime();

        // Cooldown guard — prevents instant re-fire right after landing
        Long lastTeleport = teleportCooldowns.get(id);
        if (lastTeleport != null && currentTick - lastTeleport < COOLDOWN_TICKS) return;
        teleportCooldowns.put(id, currentTick);
        entity.setPortalCooldown(); // belt-and-suspenders vanilla guard

        // ---------------------------------------------------------------
        // Step 1 — Identify the full group of blocks this portal is made of
        // ---------------------------------------------------------------
        Set<BlockPos> entryGroup = findPortalGroup(level, portalPos);

        // ---------------------------------------------------------------
        // Step 2 — Check the link registry
        //
        // If this portal already has a registered sister, send the entity
        // straight there.  The portal cannot create NEW links anymore —
        // it can only go back to its one paired destination.
        // ---------------------------------------------------------------
        StaticPortalLinkData linkData = StaticPortalLinkData.get(level);

        // Retrieve the stored destination for this specific entry block.
        // (All blocks in a linked group point to the same destination, so
        // checking portalPos is sufficient once the group is linked.)
        BlockPos linkedDest = linkData.getLinkedDestination(portalPos);

        if (linkedDest != null) {
            // Portal is already linked — teleport to the known sister portal.
            doTeleport(entity, level, linkedDest);
            return;
        }

        // ---------------------------------------------------------------
        // Step 3 — Unlinked portal: calculate where to send the player
        // ---------------------------------------------------------------
        double currentX = entity.getX();
        double currentZ = entity.getZ();
        double destY    = entity.getY();
        double destX    = currentX;
        double destZ    = currentZ;

        Direction facing = entity.getDirection();

        boolean inFarlandsX = Math.abs(currentX) >= FARLANDS_EDGE;
        boolean inFarlandsZ = Math.abs(currentZ) >= FARLANDS_EDGE;

        if (inFarlandsX || inFarlandsZ) {
            // Already in the Farlands — return trip toward 0,0 along the same axis
            if (facing.getAxis() == Direction.Axis.X) {
                destX = currentX > 0 ? currentX - FARLANDS_EDGE : currentX + FARLANDS_EDGE;
            } else {
                destZ = currentZ > 0 ? currentZ - FARLANDS_EDGE : currentZ + FARLANDS_EDGE;
            }
        } else {
            // Normal world — send them to the Farlands edge in the direction they face
            switch (facing) {
                case EAST  -> destX =  FARLANDS_EDGE;
                case WEST  -> destX = -FARLANDS_EDGE;
                case SOUTH -> destZ =  FARLANDS_EDGE;
                case NORTH -> destZ = -FARLANDS_EDGE;
                default    -> destX =  FARLANDS_EDGE;  // fallback (shouldn't normally happen)
            }
        }

        BlockPos roughDest = new BlockPos((int) destX, (int) destY, (int) destZ);

        // ---------------------------------------------------------------
        // Step 4 — Find or build the exit portal
        // ---------------------------------------------------------------
        Direction.Axis axis = getPortalAxis(level, portalPos);
        Optional<BlockPos> existingExit = findExistingUnlinkedPortal(level, roughDest, linkData);

        BlockPos exitSpawnPos; // canonical spawn position inside the exit portal
        Set<BlockPos> exitGroup;

        if (existingExit.isPresent()) {
            // Re-use an existing unlinked Static portal nearby
            exitSpawnPos = existingExit.get();
            exitGroup    = findPortalGroup(level, exitSpawnPos);
        } else {
            // No portal there yet — build one
            exitSpawnPos = roughDest;
            generateExitPortal(level, exitSpawnPos, axis);
            exitGroup = getGeneratedPortalInterior(exitSpawnPos, axis);
        }

        // ---------------------------------------------------------------
        // Step 5 — Register the bidirectional link
        //
        // canonical spawn for entry side = centre-bottom of entry portal
        // canonical spawn for exit  side = centre-bottom of exit  portal
        // ---------------------------------------------------------------
        BlockPos entrySpawnPos = getCanonicalSpawn(entryGroup);

        linkData.linkPortals(
                entryGroup, entrySpawnPos,   // blocks in A  →  spawn inside A  (for return trips)
                exitGroup,  exitSpawnPos     // blocks in B  →  spawn inside B  (for first trip)
        );

        // ---------------------------------------------------------------
        // Step 6 — Teleport
        // ---------------------------------------------------------------
        doTeleport(entity, level, exitSpawnPos);
    }

    // -------------------------------------------------------------------------
    // Teleport helper
    // -------------------------------------------------------------------------

    private static void doTeleport(Entity entity, ServerLevel level, BlockPos dest) {
        double spawnX = dest.getX() + 0.5;
        double spawnY = dest.getY();        // player stands on the obsidian floor below
        double spawnZ = dest.getZ() + 0.5;

        // Invert yaw so the player faces away from the portal on arrival
        float invertedYaw = entity.getYRot() + 180.0F;

        entity.teleportTo(spawnX, spawnY, spawnZ);
        entity.setYRot(invertedYaw);
        entity.setYHeadRot(invertedYaw);

        level.playSound(null, spawnX, spawnY, spawnZ,
                SoundEvents.LIGHTNING_BOLT_IMPACT,
                SoundSource.MASTER,
                1.5F, 0.3F);
    }

    // -------------------------------------------------------------------------
    // Portal group helpers
    // -------------------------------------------------------------------------

    /**
     * BFS flood-fill to collect all connected Static portal blocks from a seed position.
     * This is how we treat a multi-block portal as one logical unit.
     */
    public static Set<BlockPos> findPortalGroup(ServerLevel level, BlockPos seed) {
        Set<BlockPos> group   = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(seed);

        while (!queue.isEmpty()) {
            BlockPos cur = queue.poll();
            if (group.contains(cur)) continue;
            if (!level.getBlockState(cur).is(CoralineBlocks.STATIC_PORTAL_BLOCK.get())) continue;
            group.add(cur.immutable());
            queue.add(cur.above()); queue.add(cur.below());
            queue.add(cur.north()); queue.add(cur.south());
            queue.add(cur.east());  queue.add(cur.west());
        }
        return group;
    }

    /**
     * From a set of portal block positions, find the one with the lowest Y
     * (then lowest X, then lowest Z) to use as a stable canonical spawn point.
     */
    private static BlockPos getCanonicalSpawn(Set<BlockPos> group) {
        return group.stream()
                .min(Comparator.comparingInt((BlockPos p) -> p.getY())
                        .thenComparingInt(p -> p.getX())
                        .thenComparingInt(p -> p.getZ()))
                .orElseThrow();
    }

    /**
     * Returns the exact interior block positions that generateExitPortal() places.
     * Used to register the exit portal group immediately after building it,
     * without needing a BFS (since no tick has passed yet for level updates to settle).
     */
    private static Set<BlockPos> getGeneratedPortalInterior(BlockPos base, Direction.Axis axis) {
        Set<BlockPos> blocks = new HashSet<>();
        for (int lateral = 0; lateral <= 1; lateral++) {
            for (int vertical = 0; vertical <= 2; vertical++) {
                blocks.add(base.offset(
                        axis == Direction.Axis.X ? lateral : 0,
                        vertical,
                        axis == Direction.Axis.Z ? lateral : 0
                ).immutable());
            }
        }
        return blocks;
    }

    // -------------------------------------------------------------------------
    // Portal search and generation
    // -------------------------------------------------------------------------

    /**
     * Scans near roughDest for an existing Static portal that is NOT yet linked.
     * We skip linked portals so players can't accidentally "hijack" an existing wormhole.
     */
    private static Optional<BlockPos> findExistingUnlinkedPortal(ServerLevel level, BlockPos center, StaticPortalLinkData linkData) {
        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
            for (int y = -SEARCH_RADIUS; y <= SEARCH_RADIUS; y++) {
                for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (!level.getBlockState(pos).is(CoralineBlocks.STATIC_PORTAL_BLOCK.get())) continue;
                    // Only re-use portals that haven't been claimed by a pair yet
                    if (linkData.getLinkedDestination(pos) == null) {
                        int drop = getDistanceToBottom(level, pos);
                        return Optional.of(pos.below(drop));
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

    private static Direction.Axis getPortalAxis(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.hasProperty(NetherPortalBlock.AXIS)) return state.getValue(NetherPortalBlock.AXIS);
        return Direction.Axis.X;
    }

    /**
     * Carves a standard 2-wide × 3-tall Static portal with an obsidian frame.
     * Clears one block on each face of the interior to prevent suffocation.
     */
    private static void generateExitPortal(ServerLevel level, BlockPos pos, Direction.Axis axis) {
        BlockState portalState = CoralineBlocks.STATIC_PORTAL_BLOCK.get()
                .defaultBlockState()
                .setValue(NetherPortalBlock.AXIS, axis);
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        BlockState air      = Blocks.AIR.defaultBlockState();

        for (int lateral = -1; lateral <= 2; lateral++) {
            for (int vertical = -1; vertical <= 3; vertical++) {
                BlockPos target = pos.offset(
                        axis == Direction.Axis.X ? lateral : 0,
                        vertical,
                        axis == Direction.Axis.Z ? lateral : 0
                );
                boolean interior = (lateral == 0 || lateral == 1) && (vertical >= 0 && vertical <= 2);

                if (interior) {
                    level.setBlockAndUpdate(target, portalState);
                    // Carve the two faces so the player can walk through
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
