package net.zharok01.coralinesystems.event.static_portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.zharok01.coralinesystems.registry.CoralineBlocks;
import net.zharok01.coralinesystems.registry.CoralineSounds;

import java.util.*;

public class StaticPortalTeleporter {

    public static final double FARLANDS_EDGE = 6_960_353.0;
    private static final int SEARCH_RADIUS = 16;
    private static final int PORTAL_UPDATE_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    private static final Map<UUID, Long> teleportCooldowns = new HashMap<>();
    private static final long COOLDOWN_TICKS = 160L;

    private record ExitSite(BlockPos base, boolean carve) {}

    public static void coraline$forgetCooldown(UUID id) {
        teleportCooldowns.remove(id);
    }

    public static void teleportToFarlands(Entity entity, ServerLevel level, BlockPos contactPos) {
        Set<BlockPos> entryGroup = findPortalGroup(level, contactPos);
        if (entryGroup.isEmpty()) return;

        UUID id = entity.getUUID();
        long currentTick = level.getGameTime();

        Long lastTeleport = teleportCooldowns.get(id);
        if (lastTeleport != null) {
            long diff = currentTick - lastTeleport;
            if (diff >= 0 && diff < COOLDOWN_TICKS) return;
        }

        teleportCooldowns.put(id, currentTick);
        entity.setPortalCooldown();

        StaticPortalLinkData linkData = StaticPortalLinkData.get(level);

        BlockPos linkedDest = linkData.getLinkedDestination(level, contactPos);

        if (linkedDest != null) {
            if (level.getBlockState(linkedDest).is(CoralineBlocks.STATIC_PORTAL_BLOCK.get())) {
                doTeleport(entity, level, linkedDest);
                return;
            }
            linkData.unlinkPair(level.dimension(), contactPos);
        }

        double currentX = entity.getX();
        double currentZ = entity.getZ();
        double destY = entity.getY();
        double destX = currentX;
        double destZ = currentZ;

        Direction facing = entity.getDirection();

        boolean inFarlandsX = Math.abs(currentX) >= FARLANDS_EDGE;
        boolean inFarlandsZ = Math.abs(currentZ) >= FARLANDS_EDGE;

        /**
         * #+  .*#-   =*.    +=      ####*=  ::=*%+:   :%-   %#:   *+   +####*.   :*#**#=. :*+ .*#= :#= .*#*##-
         * %+  .*#%=  =*.   :#.      %*  :#=:-:+###.  ::%-   %##-  **   *+   #=  :#=   .#+  +%.:%#+.+%. =#:  :.
         * %+  .*+:%+.=*.   #+       %#++*#--:-#+.**   :%-   %+-#= **   *#**#%: .=#-.   =*: -#=+*=*=*+  .+##*+:
         * %+   *+  *#**.  -#        %*-*#=-:.=%*=*%:  :%-  .%+ :#+#*   *+..:*%+=*%*==-:**...*##+:*%%- . ...:*%
         * %+  .*+   +%*.  #=       .%*:-*#. .#+:::*%  :%-   %+  :*%*   +#**#%#+++#%#**##+==:=%#: =%*. .=#*=+#+
         * .          .   +#       .:=-:..:   .     .   :    ..    ..   :++**+++++++***+++++===-...::    .:-:.
         *                =:     .:::-:.   :          .    .           .=++++++++++++++++++++++=-:.  .
         *                .    .::::..     :          .               :=+++++++++++++++++++++++++=:..   ..
         * -.   :-.   .:      :------      --     :-   .-.    -.   .:-=+======+++++=====+**+=+++==:..-. .:----.
         * =:   -==.  :-.   .::--.  :-    --=-   .--   .==:   =.    --++==+++======+++*+==**==++==-.:=  :-.  .
         * =:   -:.=: :-   ::::--:..--   .-..-.   :-   .-.--. =:  .-=:++========+==+****+=+*+=+===-:--...-=--:
         * =:   -:  =-:-  .:=:.:-:.=-   .-- .--   --   .-..--.-:  .--+++==++===++===+***+=+++======--:  . ...--
         * =:   -:   -=- .:::: :-  -=:  :-:..:=*#--=   .-.  -==:  :-:+++==+====+++====+===+++===++==-:  --:..--
         * .    .     ...:-:.   .   ..  .    +#***+:   ...   -:=::===++++=====++========+++++++===::.     :::.
         *              .::.               . *@%##%#: : .:.  .++ :--+****+++============++++*+++-.
         *             .=+:      .           =@%%##**=-::..:: :...:=****++====-=======+++++++++-..
         * :.   :: .  -=::.:   .---:     . --:#*#%*+++.:---:..---==+++++=-===========-==++++++-:-.   :.  .-==:
         * =-   -==   -==.     :=.  == . .-==-.=+*+=++*+==-=.:===--=============--====-==++==..==.  .=. :=.  -.
         * =-   -:-=..==:.    .:=. :-=   .=:.=:-+-==++**++=-=-====+============-======-====-=:.=-:. --  .==:.
         * =-   -- :=-=-.    =+===-==    -=:.=+=..-=+*+++=-=+==+++*+===========---=======+=:-=-=.  :=..    :-==
         * =-   -:  .==-.   *++==: :=.  .======--===++++=+++===++=++===========--====-=-==-.:==-::.--   :=:  -=
         * -:   :.  :-=:    ===--.  -- .::. :--=-=+=====++*##+++*+++===++==========--=--==:..--...:-.  . :----
         *          ::.                 ....-=::-=++*#***##*******++===+++======--==----=-::.  .::--.. .  .
         *          ::.     .     .  :   . . :=++*****+++**********++=++====--=---==--=--......::-:...    ...
         * .    .. ::::.     . ...:..  ...:::*++++++++==+**+*++***+*+===----===========-::::..:-:::::-:.. .. ..
         * =-   -=-.::--    . .-=::-=:  ..=====+++=**+===++**=+*+++++++==--====--=======--==:.==::==::--.    =.
         * =-   --==::--       --   ==..:-===++++++**+====+**=+***++*++========---===+======--=-.:--:.      :-
         * =-  .-==+=---       :====-.:++==+==++**+**+==++++*=++*++++++++===========++=========.   .::==.   -.
         * =-  .===**==-       :-. ==.:.-=====++**+***===+*+==+++++++++============++=====--==-   ..   =:  :-
         * =:  .===*+-=:     ..::.  ==.:=:::++=++*+***==+++*+=+++++======+=======++++====-. -=    .-===:   -.
         *        ..-.    .      .   ::..:-=**#*+**+***++****++++==+++++++++==++++++=--:.                 ::
         *          ::         -:.      :: -==**+******+******+++==+++++++=+++++++==-:..             ..
         *          -=-.       :++=.. .  .:-:-**************+++++++++++++++++++++=-:---.             .    .::
         * *=  .+*- .:+=   .   =**=+*:  .:***-=+***++*******+*+++++++++++********-=*=:.=*:  ++  +*- .*: .+-:-*-
         * *=  .+=*+.:+=      .+=...*= .:+*=*++*+**+++******+*+++++++++++******#+-*-    :+. -*..*+= -*  -*:
         * *=  .+:.*+-+=       =****+. .-*-:**++**************++++++++++*******=.:*:    .*:  +==+:+=+=   .=***-
         * *=  .+:  =**=       =- :*= .:=**+********+****+****++++++++++***+=-*= .+-    =+.  =**- -**.   .   =*
         * *=  .+:   -*=      .+-  -*- -*-:=+******++****+++***++++++++*******+   .+*++*+  ...**  :*=   .+*++*.
         *     ::    . :.            ...::.=+****+++++++++++++++*******====-..
         * :::=---.     .                .-=+****++++++++++++++++++++==----:
         * **++=-=-      .        .  .   . -++***++++++++++++++===-------:..
         * :------.   :-...   -   :----:..   :=+==++++------+=---::::--::::----:     --.                -----.
         * ::   :    .:::...      :.   ::.:.....:-+++-=++-::--::-::::--:::::        ::::                :.   ::
         * ::::::    :. .: ..    .:   ..:.   ::.:::..-.:::.:-:::------::. :--:::   .:  :.               :    .-
         * :: .:    .::.::  :     :==- .:.  ..-.::..:-:....:-...-:...::.. .:       ::..::               :    ::
         * ::  .:   :.   .:  .    ::::::  .  ...::.  .:-::::...:-....::.. .::::.  .:    :.              -:::::
         *                   ..           .  .....:.: .:..:....  .....:.
         *                   ..           ..-   .....:::.. .-.-      ::                        ::::::.
         *                    .                  .....  ...:. .       .:.
         *        *#***-     +#=     ##***-     .:.**- .:+#**#=.  .   :#-   =#   -##***.    *#:        :##***:
         *        *+  .*-   .#**:    #=  .**     . *#. :*=.. .**     ::#=.  +#   -*.       =**#        .#:  .*+
         *        **::=*:   *+.++    #+.  :#.      *#..-#- . --*-     :##***##   -#*+++   .*- #=       .#:   =#
         *        **:=*:   -#=:+#:   #=   =#     ..*#..-#- ..+##*-    .#=   +#   -*:      =*-:+#.      .#:   +#
         *        *+  =*.  #+::-+*   #+--+*:       *#...=*=:+###*+.   .#= ..+#   -#=:::. :*=:::*+      .#=--+*.
         *        :.   :. .:    .-. .:--:.         -- .. .-==+**+-     -:.  :-   .:::::. .:     :       ::::.
         */

        if (inFarlandsX || inFarlandsZ) {
            if (facing.getAxis() == Direction.Axis.X) {
                destX = currentX > 0 ? currentX - FARLANDS_EDGE : currentX + FARLANDS_EDGE;
            } else {
                destZ = currentZ > 0 ? currentZ - FARLANDS_EDGE : currentZ + FARLANDS_EDGE;
            }
        } else {
            switch (facing) {
                case WEST -> destX = -FARLANDS_EDGE;
                case SOUTH -> destZ = FARLANDS_EDGE;
                case NORTH -> destZ = -FARLANDS_EDGE;
                default -> destX = FARLANDS_EDGE;
            }
        }

        BlockPos roughDest = new BlockPos((int) destX, (int) destY, (int) destZ);

        Direction.Axis axis = getPortalAxis(level, contactPos);
        Optional<BlockPos> existingExit = findExistingUnlinkedPortal(level, roughDest, linkData, entryGroup);

        BlockPos exitSpawnPos;
        Set<BlockPos> exitGroup;

        if (existingExit.isPresent()) {
            exitSpawnPos = existingExit.get();
            exitGroup = findPortalGroup(level, exitSpawnPos);
        } else {
            Optional<ExitSite> site = coraline$findExitSite(level, roughDest, axis);
            if (site.isEmpty()) return;
            exitSpawnPos = site.get().base();
            generateExitPortal(level, site.get(), axis);
            exitGroup = getGeneratedPortalInterior(exitSpawnPos, axis);
        }

        BlockPos entrySpawnPos = getCanonicalSpawn(entryGroup);

        linkData.linkPortals(
                level.dimension(),
                entryGroup, entrySpawnPos,
                exitGroup,  exitSpawnPos
        );

        doTeleport(entity, level, exitSpawnPos);
    }

    private static void doTeleport(Entity entity, ServerLevel level, BlockPos dest) {
        double spawnX = dest.getX() + 0.5;
        double spawnY = dest.getY();
        double spawnZ = dest.getZ() + 0.5;

        float invertedYaw = entity.getYRot() + 180.0F;

        entity.teleportToWithTicket(spawnX, spawnY, spawnZ);
        entity.setYRot(invertedYaw);
        entity.setYHeadRot(invertedYaw);

        level.playSound(null, spawnX, spawnY, spawnZ,
                CoralineSounds.TELEPORT_SUCCESS.get(),
                SoundSource.MASTER,
                1.5F, 0.3F);
    }

    public static Set<BlockPos> findPortalGroup(ServerLevel level, BlockPos seed) {
        Set<BlockPos> group = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(seed);

        while (!queue.isEmpty()) {
            BlockPos cur = queue.poll();
            if (group.contains(cur)) continue;
            if (!level.getBlockState(cur).is(CoralineBlocks.STATIC_PORTAL_BLOCK.get())) continue;
            group.add(cur.immutable());
            queue.add(cur.above()); queue.add(cur.below());
            queue.add(cur.north()); queue.add(cur.south());
            queue.add(cur.east()); queue.add(cur.west());
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

    private static Optional<BlockPos> findExistingUnlinkedPortal(ServerLevel level, BlockPos center, StaticPortalLinkData linkData, Set<BlockPos> entryGroup) {
        BlockPos nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
            for (int y = -SEARCH_RADIUS; y <= SEARCH_RADIUS; y++) {
                for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (!level.isLoaded(pos)) continue;
                    if (!level.getBlockState(pos).is(CoralineBlocks.STATIC_PORTAL_BLOCK.get())) continue;
                    if (entryGroup.contains(pos)) continue;
                    if (linkData.getLinkedDestination(level, pos) != null) continue;
                    double distance = x * x + y * y + z * z;
                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearest = pos;
                    }
                }
            }
        }
        return Optional.ofNullable(nearest).map(pos -> pos.below(getDistanceToBottom(level, pos)));
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

    private static Optional<ExitSite> coraline$findExitSite(ServerLevel level, BlockPos center, Direction.Axis axis) {
        Direction direction = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        WorldBorder border = level.getWorldBorder();
        int minY = level.getMinBuildHeight();
        int topY = Math.min(level.getMaxBuildHeight(), minY + level.getLogicalHeight()) - 1;
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();
        BlockPos bestSite = null;
        double bestDistance = -1.0D;
        BlockPos partialSite = null;
        double partialDistance = -1.0D;

        for (BlockPos.MutableBlockPos column : BlockPos.spiralAround(center, SEARCH_RADIUS, Direction.EAST, Direction.SOUTH)) {
            if (!border.isWithinBounds(column) || !border.isWithinBounds(column.relative(direction))) continue;

            int columnTop = Math.min(topY, level.getHeight(Heightmap.Types.MOTION_BLOCKING, column.getX(), column.getZ()));
            for (int y = columnTop; y >= minY; y--) {
                column.setY(y);
                if (!coraline$canReplace(level, column)) continue;

                int airTop = y;
                while (y > minY && coraline$canReplace(level, column.move(Direction.DOWN))) {
                    y--;
                }
                if (y + 4 > topY) continue;
                int airDepth = airTop - y;
                if (airDepth > 0 && airDepth < 3) continue;

                column.setY(y);
                if (!coraline$canHostFrame(level, column, probe, direction, 0)) continue;

                double distance = center.distSqr(column);
                if (coraline$canHostFrame(level, column, probe, direction, -1)
                        && coraline$canHostFrame(level, column, probe, direction, 1)
                        && (bestDistance < 0 || bestDistance > distance)) {
                    bestDistance = distance;
                    bestSite = column.immutable();
                }
                if (bestDistance < 0 && (partialDistance < 0 || partialDistance > distance)) {
                    partialDistance = distance;
                    partialSite = column.immutable();
                }
            }
        }

        if (bestSite != null) return Optional.of(new ExitSite(bestSite, false));
        if (partialSite != null) return Optional.of(new ExitSite(partialSite, false));

        BlockPos fallback = new BlockPos(center.getX(), Mth.clamp(center.getY(), minY + 1, topY - 3), center.getZ());
        if (!border.isWithinBounds(fallback) || !border.isWithinBounds(fallback.relative(direction, 2))) return Optional.empty();
        if (coraline$touchesProtectedBlock(level, fallback, direction)) return Optional.empty();
        return Optional.of(new ExitSite(fallback, true));
    }

    private static boolean coraline$canReplace(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.canBeReplaced() && state.getFluidState().isEmpty();
    }

    private static boolean coraline$canHostFrame(ServerLevel level, BlockPos origin, BlockPos.MutableBlockPos probe, Direction direction, int depth) {
        for (int lateral = -1; lateral < 3; lateral++) {
            for (int vertical = -1; vertical < 4; vertical++) {
                probe.set(coraline$portalOffset(origin, direction, lateral, vertical, depth));
                if (vertical < 0 && !level.getBlockState(probe).isSolid()) return false;
                if (vertical >= 0 && !coraline$canReplace(level, probe)) return false;
            }
        }
        return true;
    }

    private static boolean coraline$touchesProtectedBlock(ServerLevel level, BlockPos base, Direction direction) {
        for (int lateral = -1; lateral <= 2; lateral++) {
            for (int vertical = -1; vertical <= 3; vertical++) {
                if (coraline$isProtected(level, coraline$portalOffset(base, direction, lateral, vertical, 0))) return true;
            }
        }
        for (int depth = -1; depth <= 1; depth++) {
            for (int lateral = 0; lateral <= 1; lateral++) {
                for (int vertical = -1; vertical <= 2; vertical++) {
                    if (coraline$isProtected(level, coraline$portalOffset(base, direction, lateral, vertical, depth))) return true;
                }
            }
        }
        return false;
    }

    private static boolean coraline$isProtected(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.hasBlockEntity() || state.getDestroySpeed(level, pos) < 0;
    }

    private static BlockPos coraline$portalOffset(BlockPos base, Direction direction, int lateral, int vertical, int depth) {
        Direction depthDirection = direction.getClockWise();
        return base.offset(
                lateral * direction.getStepX() + depth * depthDirection.getStepX(),
                vertical,
                lateral * direction.getStepZ() + depth * depthDirection.getStepZ()
        );
    }

    private static void generateExitPortal(ServerLevel level, ExitSite site, Direction.Axis axis) {
        Direction direction = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        BlockPos base = site.base();
        BlockState portalState = CoralineBlocks.STATIC_PORTAL_BLOCK.get()
                .defaultBlockState()
                .setValue(NetherPortalBlock.AXIS, axis);
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();

        if (site.carve()) {
            BlockState air = Blocks.AIR.defaultBlockState();
            for (int depth = -1; depth <= 1; depth++) {
                for (int lateral = 0; lateral <= 1; lateral++) {
                    for (int vertical = -1; vertical <= 2; vertical++) {
                        level.setBlockAndUpdate(coraline$portalOffset(base, direction, lateral, vertical, depth), vertical < 0 ? obsidian : air);
                    }
                }
            }
        }

        for (int lateral = -1; lateral <= 2; lateral++) {
            for (int vertical = -1; vertical <= 3; vertical++) {
                if (lateral == -1 || lateral == 2 || vertical == -1 || vertical == 3) {
                    level.setBlockAndUpdate(coraline$portalOffset(base, direction, lateral, vertical, 0), obsidian);
                }
            }
        }

        for (int lateral = 0; lateral <= 1; lateral++) {
            for (int vertical = 0; vertical <= 2; vertical++) {
                level.setBlock(coraline$portalOffset(base, direction, lateral, vertical, 0), portalState, PORTAL_UPDATE_FLAGS);
            }
        }
    }
}
