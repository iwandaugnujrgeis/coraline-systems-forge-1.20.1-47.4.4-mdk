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

    private static final double FARLANDS_EDGE = 12_550_821.0;
    private static final int SEARCH_RADIUS = 16;

    /**
     * Custom cooldown map keyed by entity UUID, measured in game ticks.
     *
     * IMPORTANT — STATIC MAP CROSS-WORLD SAFETY:
     * Static fields survive world loads within the same JVM session. If a player
     * teleports at game-tick T in World A, then loads World B (which starts at
     * a lower tick, e.g. 0), the expression (currentTick - lastTeleport) becomes
     * negative. Negative is still less than COOLDOWN_TICKS, so the old guard
     * "diff < COOLDOWN_TICKS" fires and permanently blocks the portal for that
     * session. The fix: only enforce the cooldown when diff is non-negative.
     * A negative diff means we crossed into a different world — treat it as expired.
     */
    private static final Map<UUID, Long> teleportCooldowns = new HashMap<>();
    private static final long COOLDOWN_TICKS = 100L;

    // -------------------------------------------------------------------------
    // Entry point
    // -------------------------------------------------------------------------

    public static void teleportToFarlands(Entity entity, ServerLevel level, BlockPos portalPos) {
        UUID id = entity.getUUID();
        long currentTick = level.getGameTime();

        Long lastTeleport = teleportCooldowns.get(id);
        if (lastTeleport != null) {
            long diff = currentTick - lastTeleport;
            // diff < 0 means we are in a different world session — treat as no cooldown.
            // diff >= 0 && diff < COOLDOWN_TICKS means we genuinely just teleported.
            if (diff >= 0 && diff < COOLDOWN_TICKS) return;
        }

        teleportCooldowns.put(id, currentTick);
        entity.setPortalCooldown();

        // ---------------------------------------------------------------
        // Step 1 — Identify the full group of blocks this portal is made of
        // ---------------------------------------------------------------
        Set<BlockPos> entryGroup = findPortalGroup(level, portalPos);

        // ---------------------------------------------------------------
        // Step 2 — Check the link registry
        // ---------------------------------------------------------------
        StaticPortalLinkData linkData = StaticPortalLinkData.get(level);
        if (linkData == null) return; // safety guard (mirrors AMWorldData pattern)

        BlockPos linkedDest = linkData.getLinkedDestination(portalPos);

        if (linkedDest != null) {
            doTeleport(entity, level, linkedDest);
            return;
        }

        // ---------------------------------------------------------------
        // Step 3 — Unlinked portal: calculate Farlands destination
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
            if (facing.getAxis() == Direction.Axis.X) {
                destX = currentX > 0 ? currentX - FARLANDS_EDGE : currentX + FARLANDS_EDGE;
            } else {
                destZ = currentZ > 0 ? currentZ - FARLANDS_EDGE : currentZ + FARLANDS_EDGE;
            }
        } else {
            switch (facing) {
                case EAST  -> destX =  FARLANDS_EDGE;
                case WEST  -> destX = -FARLANDS_EDGE;
                case SOUTH -> destZ =  FARLANDS_EDGE;
                case NORTH -> destZ = -FARLANDS_EDGE;
                default    -> destX =  FARLANDS_EDGE;
            }
        }

        BlockPos roughDest = new BlockPos((int) destX, (int) destY, (int) destZ);

        // ---------------------------------------------------------------
        // Step 4 — Find or build the exit portal
        // ---------------------------------------------------------------
        Direction.Axis axis = getPortalAxis(level, portalPos);
        Optional<BlockPos> existingExit = findExistingUnlinkedPortal(level, roughDest, linkData);

        BlockPos exitSpawnPos;
        Set<BlockPos> exitGroup;

        if (existingExit.isPresent()) {
            exitSpawnPos = existingExit.get();
            exitGroup    = findPortalGroup(level, exitSpawnPos);
        } else {
            exitSpawnPos = roughDest;
            generateExitPortal(level, exitSpawnPos, axis);
            exitGroup = getGeneratedPortalInterior(exitSpawnPos, axis);
        }

        // ---------------------------------------------------------------
        // Step 5 — Register the bidirectional link
        // ---------------------------------------------------------------
        BlockPos entrySpawnPos = getCanonicalSpawn(entryGroup);

        linkData.linkPortals(
                entryGroup, entrySpawnPos,
                exitGroup,  exitSpawnPos
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
        double spawnY = dest.getY();
        double spawnZ = dest.getZ() + 0.5;

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

    private static BlockPos getCanonicalSpawn(Set<BlockPos> group) {
        return group.stream()
                .min(Comparator.comparingInt((BlockPos p) -> p.getY())
                        .thenComparingInt(p -> p.getX())
                        .thenComparingInt(p -> p.getZ()))
                .orElseThrow();
    }

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

    private static Optional<BlockPos> findExistingUnlinkedPortal(ServerLevel level, BlockPos center, StaticPortalLinkData linkData) {
        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
            for (int y = -SEARCH_RADIUS; y <= SEARCH_RADIUS; y++) {
                for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (!level.getBlockState(pos).is(CoralineBlocks.STATIC_PORTAL_BLOCK.get())) continue;
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