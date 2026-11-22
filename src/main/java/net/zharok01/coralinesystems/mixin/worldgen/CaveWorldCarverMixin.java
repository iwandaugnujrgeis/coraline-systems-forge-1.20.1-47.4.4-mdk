package net.zharok01.coralinesystems.mixin.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.zharok01.coralinesystems.mixin.accessor.CaveCarverConfigurationAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.function.Function;

@Mixin(CaveWorldCarver.class)
public abstract class CaveWorldCarverMixin {
    @Shadow protected abstract int getCaveBound();
    @Shadow protected abstract float getThickness(RandomSource random);
    @Shadow protected abstract double getYScale();
    @Shadow protected abstract void createRoom(
            CarvingContext context, CaveCarverConfiguration config,
            ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeAccessor,
            Aquifer aquifer,
            double x, double y, double z,
            float radius, double horizontalVerticalRatio,
            CarvingMask carvingMask, WorldCarver.CarveSkipChecker skipChecker
    );
    @Shadow protected abstract void createTunnel(
            CarvingContext context, CaveCarverConfiguration config,
            ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeAccessor,
            long seed, Aquifer aquifer,
            double x, double y, double z,
            double horizontalRadiusMultiplier, double verticalRadiusMultiplier,
            float thickness, float yaw, float pitch,
            int branchIndex, int branchCount, double horizontalVerticalRatio,
            CarvingMask carvingMask, WorldCarver.CarveSkipChecker skipChecker
    );

    @Unique private static final double CREATE_ROOM_BASE_RADIUS = 1.5;
    @Unique private static final double ENTRANCE_STEP_LENGTH = 1.5;
    @Unique private static final double SINKHOLE_COLLAR_RADIUS = 4.5;
    @Unique private static final double SINKHOLE_WOBBLE = 1.2;
    @Unique private static final double SURFACE_DENSITY_THRESHOLD = 0.390625;
    @Unique private static final double TERRACE_CEILING_HEIGHT = 2.6;
    @Unique private static final double TERRACE_FLOOR_CUT = 0.4;
    @Unique private static final double TERRACE_STEP_LENGTH = 1.0;
    @Unique private static final float DEEP_THICKNESS_BOOST = 1.4f;
    @Unique private static final int BIOME_SAMPLE_Y = 64;
    @Unique private static final int CAVE_MOUTH_CHANCE = 32;
    @Unique private static final int CAVE_RANGE = 4;
    @Unique private static final int DEEP_CAVE_Y_THRESHOLD = 0;
    @Unique private static final int ENTRANCE_HANDOFF_MARGIN = 6;
    @Unique private static final int ENTRANCE_HANDOFF_MIN_Y = 24;
    @Unique private static final int ENTRANCE_HANDOFF_Y_SPREAD = 16;
    @Unique private static final int ENTRANCE_MAX_STEPS = 60;
    @Unique private static final int ENTRANCE_START_Y_SPREAD = 28;
    @Unique private static final int EXTRA_BRANCH_CHANCE = 8;
    @Unique private static final int RIFT_CHAMBER_CHANCE = 16;
    @Unique private static final int SINKHOLE_BOTTOM_MIN_Y = 28;
    @Unique private static final int SINKHOLE_BOTTOM_Y_SPREAD = 18;
    @Unique private static final int SINKHOLE_CHANCE = 20;
    @Unique private static final int SINKHOLE_COLLAR_BOTTOM_Y = 60;
    @Unique private static final int SINKHOLE_COLLAR_TOP_Y = 85;
    @Unique private static final int SINKHOLE_TOP_Y = 100;
    @Unique private static final int SQUEEZE_MAX_Y = 0;
    @Unique private static final int SURFACE_ENTRANCE_CHANCE = 2;
    @Unique private static final int SURFACE_SCAN_COARSE_STEP = 4;
    @Unique private static final int SURFACE_SCAN_TOP_Y = 140;
    @Unique private static final int SURFACE_Y_THRESHOLD = 52;
    @Unique private static final int TERRACE_CHANCE = 4;
    @Unique private static final int TERRACE_LANDING_MIN_STEPS = 2;
    @Unique private static final int TERRACE_LANDING_STEPS_SPREAD = 2;
    @Unique private static final double TRENCH_MIN_HALF_WIDTH = 1.9;
    @Unique private static final double TRENCH_HALF_WIDTH_SPREAD = 0.7;
    @Unique private static final int TRENCH_OPEN_CLEARANCE = 3;
    @Unique private static final int TRENCH_COVER_DEPTH = 5;
    @Unique private static final int TRENCH_COVERED_STEPS = 10;
    @Unique private static final int TRENCH_MAX_STEPS = 40;
    @Unique private static final int SURFACE_RESCAN_MARGIN = 12;
    @Unique private static final int TERRACE_MIN_SURFACE_Y = 64;
    @Unique private static final int TERRACE_MOUTH_DEPTH = 1;
    @Unique private static final int TERRACE_SPAGHETTI_MIN = 2;
    @Unique private static final int TERRACE_SPAGHETTI_SPREAD = 3;
    @Inject(method = "getCaveBound", at = @At("HEAD"), cancellable = true)
    private void coraline$widenCaveBound(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(16);
    }
    @Inject(
            method = "carve(Lnet/minecraft/world/level/levelgen/carver/CarvingContext;" +
                    "Lnet/minecraft/world/level/levelgen/carver/CaveCarverConfiguration;" +
                    "Lnet/minecraft/world/level/chunk/ChunkAccess;" +
                    "Ljava/util/function/Function;" +
                    "Lnet/minecraft/util/RandomSource;" +
                    "Lnet/minecraft/world/level/levelgen/Aquifer;" +
                    "Lnet/minecraft/world/level/ChunkPos;" +
                    "Lnet/minecraft/world/level/chunk/CarvingMask;)Z",
            at = @At("HEAD"),
            cancellable = true

    )

    private void coraline$carveWithSurfaceRules(
            CarvingContext context,
            CaveCarverConfiguration config,
            ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biomeAccessor,
            RandomSource random,
            Aquifer aquifer,
            ChunkPos chunkPos,
            CarvingMask carvingMask,
            CallbackInfoReturnable<Boolean> cir
    ) {
        int rangeCoord = coraline$rangeCoord();
        int clusterCount = random.nextInt(random.nextInt(random.nextInt(this.getCaveBound()) + 1) + 1);
        for (int k = 0; k < clusterCount; k++) {
            double x = chunkPos.getBlockX(random.nextInt(16));
            double y = config.y.sample(random, context);
            double z = chunkPos.getBlockZ(random.nextInt(16));
            double hRadiusMultiplier = config.horizontalRadiusMultiplier.sample(random);
            double vRadiusMultiplier = config.verticalRadiusMultiplier.sample(random);
            double floorLevel = ((CaveCarverConfigurationAccessor) config).coraline$getFloorLevel().sample(random);
            if (y > SURFACE_Y_THRESHOLD) {
                if (random.nextInt(SURFACE_ENTRANCE_CHANCE) == 0) {
                    coraline$carveSpaghettiEntrance(context, config, chunk, biomeAccessor, aquifer, carvingMask, random, x, z);
                }
                continue;
            }
            WorldCarver.CarveSkipChecker skipChecker = (ctx, ex, fy, gz, ix) -> coraline$shouldSkip(ex, fy, gz, floorLevel);
            int tunnelCount = 1;
            if (random.nextInt(4) == 0) {
                double yScaleForRoom = config.yScale.sample(random);
                float roomRadius = 1.0f + random.nextFloat() * 6.0f;
                this.createRoom(context, config, chunk, biomeAccessor, aquifer, x, y, z, roomRadius, yScaleForRoom, carvingMask, skipChecker);
                tunnelCount += random.nextInt(4);
            }
            for (int p = 0; p < tunnelCount; p++) {
                float yaw = random.nextFloat() * ((float) Math.PI * 2f);
                float pitch = (random.nextFloat() - 0.5f) / 4.0f;
                float thickness = this.getThickness(random);
                int branches = rangeCoord - random.nextInt(rangeCoord / 4);
                long seed = random.nextLong();
                this.createTunnel(context, config, chunk, biomeAccessor, seed, aquifer, x, y, z,
                        hRadiusMultiplier, vRadiusMultiplier, thickness, yaw, pitch, 0, branches,
                        this.getYScale(), carvingMask, skipChecker);
            }
        }
        RandomSource rng = RandomSource.create(random.nextLong());
        coraline$addExtraBranches(context, config, chunk, biomeAccessor, aquifer, chunkPos, carvingMask, rng);
        coraline$spawnDiagonalRiftChamber(context, config, chunk, biomeAccessor, aquifer, chunkPos, carvingMask, rng);
        coraline$spawnSqueezePassages(context, config, chunk, biomeAccessor, aquifer, chunkPos, carvingMask, rng);
        coraline$spawnSurfaceSinkhole(context, config, chunk, biomeAccessor, aquifer, chunkPos, carvingMask, rng);
        coraline$spawnTerraceCave(context, config, chunk, biomeAccessor, aquifer, chunkPos, carvingMask, rng);
        coraline$spawnCaveMouth(context, config, chunk, biomeAccessor, aquifer, chunkPos, carvingMask, rng);
        cir.setReturnValue(true);
    }

    @Unique
    private static boolean coraline$shouldSkip(double relX, double relY, double relZ, double floorLevel) {
        if (relY <= floorLevel) return true;
        return relX * relX + relY * relY + relZ * relZ >= 1.0;
    }

    @Unique
    private WorldCarver.CarveSkipChecker coraline$makeSkipChecker(CaveCarverConfiguration config, RandomSource rng) {
        float floorLevel = ((CaveCarverConfigurationAccessor) config).coraline$getFloorLevel().sample(rng);
        return (ctx, ex, fx, gx, ix) -> fx <= floorLevel || (ex * ex + fx * fx + gx * gx >= 1.0);
    }

    @Unique
    private static WorldCarver.CarveSkipChecker coraline$roundSkipChecker() {
        return (ctx, ex, fy, gz, ix) -> ex * ex + fy * fy + gz * gz >= 1.0;
    }

    @Unique
    private static WorldCarver.CarveSkipChecker coraline$flatFloorSkipChecker() {
        return (ctx, ex, fy, gz, ix) -> fy <= -TERRACE_FLOOR_CUT || ex * ex + fy * fy + gz * gz >= 1.0;
    }

    @Unique
    private static WorldCarver.CarveSkipChecker coraline$trenchSkipChecker() {
        return (ctx, ex, fy, gz, ix) -> fy <= -TERRACE_FLOOR_CUT || ex * ex + gz * gz >= 1.0;
    }

    @Unique
    private static int coraline$rangeCoord() {
        return SectionPos.sectionToBlockCoord(CAVE_RANGE * 2 - 1);
    }

    @Unique
    private static boolean coraline$isOpenLandBiome(Function<BlockPos, Holder<Biome>> biomeAccessor, double x, double z) {
        Holder<Biome> biome = biomeAccessor.apply(new BlockPos(Mth.floor(x), BIOME_SAMPLE_Y, Mth.floor(z)));
        return !(biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_RIVER)
                || biome.is(BiomeTags.IS_BEACH) || biome.is(BiomeTags.IS_MOUNTAIN));
    }

    @Unique
    private void coraline$carveBlob(
            CarvingContext context, CaveCarverConfiguration config,
            ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeAccessor,
            Aquifer aquifer, CarvingMask carvingMask,
            double x, double y, double z, double horizontalRadius, double verticalRadius
    ) {
        this.createRoom(context, config, chunk, biomeAccessor, aquifer, x, y, z,
                (float) (horizontalRadius - CREATE_ROOM_BASE_RADIUS), verticalRadius / horizontalRadius,
                carvingMask, coraline$roundSkipChecker());
    }

    @Unique
    private void coraline$carveFlatFloorBlob(
            CarvingContext context, CaveCarverConfiguration config,
            ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeAccessor,
            Aquifer aquifer, CarvingMask carvingMask,
            double x, double floorY, double z, double horizontalRadius, double verticalRadius
    ) {
        double centerY = floorY + verticalRadius * TERRACE_FLOOR_CUT;
        this.createRoom(context, config, chunk, biomeAccessor, aquifer, x, centerY, z,
                (float) (horizontalRadius - CREATE_ROOM_BASE_RADIUS), verticalRadius / horizontalRadius,
                carvingMask, coraline$flatFloorSkipChecker());
    }

    @Unique
    private void coraline$carveSprawlingTunnel(
            CarvingContext context, CaveCarverConfiguration config,
            ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeAccessor,
            Aquifer aquifer, CarvingMask carvingMask, RandomSource rng,
            double x, double y, double z
    ) {
        int rangeCoord = coraline$rangeCoord();
        float yaw = rng.nextFloat() * ((float) Math.PI * 2f);
        float pitch = (rng.nextFloat() - 0.5f) / 4.0f;
        float thickness = this.getThickness(rng);
        int branchCount = rangeCoord - rng.nextInt(rangeCoord / 4);
        this.createTunnel(context, config, chunk, biomeAccessor,
                rng.nextLong(), aquifer, x, y, z,
                config.horizontalRadiusMultiplier.sample(rng), config.verticalRadiusMultiplier.sample(rng),
                thickness, yaw, pitch, 0, branchCount,
                this.getYScale(), carvingMask, coraline$makeSkipChecker(config, rng));
    }

    @Unique
    private void coraline$carveSpaghettiTunnel(
            CarvingContext context, CaveCarverConfiguration config,
            ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeAccessor,
            Aquifer aquifer, CarvingMask carvingMask, RandomSource rng,
            double x, double y, double z
    ) {
        int rangeCoord = coraline$rangeCoord();
        float yaw = rng.nextFloat() * ((float) Math.PI * 2f);
        float pitch = (rng.nextFloat() - 0.5f) / 4.0f;
        float thickness = 0.5f + rng.nextFloat() * 0.5f;
        double hRadius = 0.6 + rng.nextDouble() * 0.3;
        double vRadius = 0.6 + rng.nextDouble() * 0.3;
        int branchCount = rangeCoord / 2 + rng.nextInt(rangeCoord / 4);
        this.createTunnel(context, config, chunk, biomeAccessor,
                rng.nextLong(), aquifer, x, y, z,
                hRadius, vRadius, thickness, yaw, pitch, 0, branchCount,
                this.getYScale(), carvingMask, coraline$makeSkipChecker(config, rng));
    }

    @Unique
    private void coraline$carveSpaghettiEntrance(
            CarvingContext context, CaveCarverConfiguration config,
            ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeAccessor,
            Aquifer aquifer, CarvingMask carvingMask, RandomSource rng,
            double x, double z
    ) {
        double y = SURFACE_Y_THRESHOLD + rng.nextInt(ENTRANCE_START_Y_SPREAD);
        float yaw = rng.nextFloat() * ((float) Math.PI * 2f);
        float pitch = -(0.4f + rng.nextFloat() * 0.3f);
        double radius = 1.1 + rng.nextDouble() * 0.5;
        int handoffY = ENTRANCE_HANDOFF_MIN_Y + rng.nextInt(ENTRANCE_HANDOFF_Y_SPREAD);
        for (int step = 0; step < ENTRANCE_MAX_STEPS && y > handoffY; step++) {
            coraline$carveBlob(context, config, chunk, biomeAccessor, aquifer, carvingMask, x, y, z, radius, radius * 0.9);
            yaw += (rng.nextFloat() - 0.5f) * 0.5f;
            pitch += (rng.nextFloat() - 0.5f) * 0.08f;
            double horizontalStep = Math.cos(pitch) * ENTRANCE_STEP_LENGTH;
            x += Math.cos(yaw) * horizontalStep;
            z += Math.sin(yaw) * horizontalStep;
            y += Math.sin(pitch) * ENTRANCE_STEP_LENGTH;
        }
        if (y < SURFACE_Y_THRESHOLD - ENTRANCE_HANDOFF_MARGIN) {
            coraline$carveBlob(context, config, chunk, biomeAccessor, aquifer, carvingMask,
                    x, y, z, 2.5 + rng.nextDouble(), 2.0 + rng.nextDouble() * 0.5);
            coraline$carveSprawlingTunnel(context, config, chunk, biomeAccessor, aquifer, carvingMask, rng, x, y, z);
        }
    }

    @Unique
    private void coraline$spawnTerraceCave(
            CarvingContext context, CaveCarverConfiguration config,
            ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeAccessor,
            Aquifer aquifer, ChunkPos chunkPos, CarvingMask carvingMask,
            RandomSource rng
    ) {
        if (rng.nextInt(TERRACE_CHANCE) != 0) return;
        double x = chunkPos.getBlockX(rng.nextInt(16));
        double z = chunkPos.getBlockZ(rng.nextInt(16));
        if (!coraline$isOpenLandBiome(biomeAccessor, x, z)) return;
        int surfaceY = coraline$estimateSurfaceY(context, x, z, SURFACE_SCAN_TOP_Y);
        if (surfaceY < TERRACE_MIN_SURFACE_Y) return;
        double startFloorY = surfaceY - TERRACE_MOUTH_DEPTH;
        double halfWidth = TRENCH_MIN_HALF_WIDTH + rng.nextDouble() * TRENCH_HALF_WIDTH_SPREAD;
        float yaw = rng.nextFloat() * ((float) Math.PI * 2f);
        int landingLength = TERRACE_LANDING_MIN_STEPS + rng.nextInt(TERRACE_LANDING_STEPS_SPREAD);
        double floorY = startFloorY;
        boolean covered = false;
        int coveredSteps = 0;
        for (int step = 0; step < TRENCH_MAX_STEPS && coveredSteps < TRENCH_COVERED_STEPS; step++) {
            floorY = startFloorY - (step / landingLength);
            if (!covered && step > 0) {
                surfaceY = coraline$estimateSurfaceY(context, x, z, surfaceY + SURFACE_RESCAN_MARGIN);
            }
            covered = covered || floorY <= surfaceY - TRENCH_COVER_DEPTH;
            if (covered) {
                coraline$carveFlatFloorBlob(context, config, chunk, biomeAccessor, aquifer, carvingMask,
                        x, floorY, z, halfWidth, TERRACE_CEILING_HEIGHT);
                coveredSteps++;
            } else {
                double verticalRadius = (surfaceY + TRENCH_OPEN_CLEARANCE - floorY) / (1.0 + TERRACE_FLOOR_CUT);
                double centerY = floorY + verticalRadius * TERRACE_FLOOR_CUT;
                this.createRoom(context, config, chunk, biomeAccessor, aquifer, x, centerY, z,
                        (float) (halfWidth - CREATE_ROOM_BASE_RADIUS), verticalRadius / halfWidth,
                        carvingMask, coraline$trenchSkipChecker());
            }
            yaw += (rng.nextFloat() - 0.5f) * 0.15f;
            x += Math.cos(yaw) * TERRACE_STEP_LENGTH;
            z += Math.sin(yaw) * TERRACE_STEP_LENGTH;
        }
        int spaghettiCount = TERRACE_SPAGHETTI_MIN + rng.nextInt(TERRACE_SPAGHETTI_SPREAD);
        for (int i = 0; i < spaghettiCount; i++) {
            coraline$carveSpaghettiTunnel(context, config, chunk, biomeAccessor, aquifer, carvingMask, rng,
                    x, floorY + 1.0, z);
        }
    }

    @Unique
    private void coraline$spawnSurfaceSinkhole(
            CarvingContext context, CaveCarverConfiguration config,
            ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeAccessor,
            Aquifer aquifer, ChunkPos chunkPos, CarvingMask carvingMask,
            RandomSource rng
    ) {
        if (rng.nextInt(SINKHOLE_CHANCE) != 0) return;
        double x = chunkPos.getBlockX(rng.nextInt(16));
        double z = chunkPos.getBlockZ(rng.nextInt(16));
        if (!coraline$isOpenLandBiome(biomeAccessor, x, z)) return;
        double shaftRadius = 2.0 + rng.nextDouble() * 0.8;
        double bottomY = SINKHOLE_BOTTOM_MIN_Y + rng.nextInt(SINKHOLE_BOTTOM_Y_SPREAD);
        double y = SINKHOLE_TOP_Y;
        while (y > bottomY) {
            double collar = Mth.clamp((y - SINKHOLE_COLLAR_BOTTOM_Y) / (SINKHOLE_COLLAR_TOP_Y - SINKHOLE_COLLAR_BOTTOM_Y), 0.0, 1.0);
            double radius = Mth.lerp(collar, shaftRadius, SINKHOLE_COLLAR_RADIUS) * (0.85 + rng.nextDouble() * 0.3);
            coraline$carveBlob(context, config, chunk, biomeAccessor, aquifer, carvingMask, x, y, z, radius, radius);
            x += (rng.nextDouble() - 0.5) * SINKHOLE_WOBBLE;
            z += (rng.nextDouble() - 0.5) * SINKHOLE_WOBBLE;
            y -= radius * 0.7;
        }
        coraline$carveBlob(context, config, chunk, biomeAccessor, aquifer, carvingMask,
                x, y, z, 5.0 + rng.nextDouble() * 3.0, 3.0 + rng.nextDouble() * 1.5);
        int sprawlCount = 2 + rng.nextInt(2);
        for (int i = 0; i < sprawlCount; i++) {
            coraline$carveSprawlingTunnel(context, config, chunk, biomeAccessor, aquifer, carvingMask, rng, x, y, z);
        }
    }

    @Unique
    private void coraline$addExtraBranches(
            CarvingContext context, CaveCarverConfiguration config,
            ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeAccessor,
            Aquifer aquifer, ChunkPos chunkPos, CarvingMask carvingMask,
            RandomSource rng
    ) {
        int bound = this.getCaveBound();
        int iterations = rng.nextInt(rng.nextInt(rng.nextInt(bound) + 1) + 1);
        int rangeCoord = coraline$rangeCoord();
        for (int k = 0; k < iterations; k++) {
            if (rng.nextInt(EXTRA_BRANCH_CHANCE) != 0) {
                rng.nextInt(16);
                rng.nextInt(16);
                continue;
            }
            double x = chunkPos.getBlockX(rng.nextInt(16));
            double y = config.y.sample(rng, context);
            double z = chunkPos.getBlockZ(rng.nextInt(16));
            if (y > SURFACE_Y_THRESHOLD) {
                y = SURFACE_Y_THRESHOLD - rng.nextInt(20);
            }
            WorldCarver.CarveSkipChecker skipChecker = coraline$makeSkipChecker(config, rng);
            double hRadius, vRadius;
            float pitch, thickness;
            if (rng.nextInt(2) == 0) {
                if (y > 45.0) y = 45.0 - rng.nextInt(20);
                hRadius = config.horizontalRadiusMultiplier.sample(rng);
                vRadius = config.verticalRadiusMultiplier.sample(rng);
                float sign = rng.nextBoolean() ? 1f : -1f;
                pitch = sign * ((float) Math.PI / 4f + rng.nextFloat() * (float) Math.PI / 12f);
                thickness = this.getThickness(rng);
            } else {
                hRadius = config.horizontalRadiusMultiplier.sample(rng);
                vRadius = config.verticalRadiusMultiplier.sample(rng);
                pitch = (rng.nextFloat() - 0.5f) / 4.0f;
                thickness = this.getThickness(rng);
                if (y < DEEP_CAVE_Y_THRESHOLD) thickness *= DEEP_THICKNESS_BOOST;
            }
            float yaw = rng.nextFloat() * ((float) Math.PI * 2f);
            int branchCount = rangeCoord - rng.nextInt(rangeCoord / 4);
            this.createTunnel(context, config, chunk, biomeAccessor,
                    rng.nextLong(), aquifer,
                    x, y, z, hRadius, vRadius,
                    thickness, yaw, pitch, 0, branchCount,
                    this.getYScale(), carvingMask, skipChecker);
        }
    }

    @Unique
    private void coraline$spawnDiagonalRiftChamber(
            CarvingContext context, CaveCarverConfiguration config,
            ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeAccessor,
            Aquifer aquifer, ChunkPos chunkPos, CarvingMask carvingMask,
            RandomSource rng
    ) {
        if (rng.nextInt(RIFT_CHAMBER_CHANCE) != 0) return;
        double x = chunkPos.getBlockX(rng.nextInt(16));
        double z = chunkPos.getBlockZ(rng.nextInt(16));
        double y = config.y.sample(rng, context);
        if (y > SURFACE_Y_THRESHOLD) y = SURFACE_Y_THRESHOLD - rng.nextInt(15);
        float yaw = rng.nextFloat() * ((float) Math.PI * 2f);
        float pitch = -(0.15f + rng.nextFloat() * 0.15f);
        double dirX = Math.sin(yaw);
        double dirZ = -Math.cos(yaw);
        double spacing = 5.0 + rng.nextDouble() * 4.0;
        double yDrop = 3.0 + rng.nextDouble() * 4.0;
        double hRadius = 0.5 + rng.nextDouble() * 0.3;
        double vRadius = 1.4 + rng.nextDouble() * 0.5;
        float thickness = 3.0f + rng.nextFloat() * 2.0f;
        int stubBranch = coraline$rangeCoord() / 3 + rng.nextInt(coraline$rangeCoord() / 4);
        WorldCarver.CarveSkipChecker sc0 = coraline$makeSkipChecker(config, rng);
        WorldCarver.CarveSkipChecker sc1 = coraline$makeSkipChecker(config, rng);
        WorldCarver.CarveSkipChecker sc2 = coraline$makeSkipChecker(config, rng);
        this.createTunnel(context, config, chunk, biomeAccessor,
                rng.nextLong(), aquifer,
                x, y, z,
                hRadius, vRadius, thickness, yaw, pitch, 0, stubBranch,
                this.getYScale(), carvingMask, sc0);
        this.createTunnel(context, config, chunk, biomeAccessor,
                rng.nextLong(), aquifer,
                x + dirX * spacing, y - yDrop, z + dirZ * spacing,
                hRadius, vRadius, thickness, yaw, pitch, 0, stubBranch,
                this.getYScale(), carvingMask, sc1);
        this.createTunnel(context, config, chunk, biomeAccessor,
                rng.nextLong(), aquifer,
                x + dirX * spacing * 2, y - yDrop * 2, z + dirZ * spacing * 2,
                hRadius, vRadius, thickness, yaw, pitch, 0, stubBranch,
                this.getYScale(), carvingMask, sc2);
    }

    @Unique
    private void coraline$spawnSqueezePassages(
            CarvingContext context, CaveCarverConfiguration config,
            ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeAccessor,
            Aquifer aquifer, ChunkPos chunkPos, CarvingMask carvingMask,
            RandomSource rng
    ) {
        int count = rng.nextInt(3);
        if (count == 0) return;
        int rangeCoord = coraline$rangeCoord();
        for (int i = 0; i < count; i++) {
            double x = chunkPos.getBlockX(rng.nextInt(16));
            double y = config.y.sample(rng, context);
            double z = chunkPos.getBlockZ(rng.nextInt(16));
            if (y > SQUEEZE_MAX_Y) {
                rng.nextFloat();
                rng.nextFloat();
                continue;
            }
            float yaw = rng.nextFloat() * ((float) Math.PI * 2f);
            float pitch = (rng.nextFloat() - 0.5f) / 4.0f;
            float thickness = 0.3f + rng.nextFloat() * 0.4f;
            double hRadius = 0.5 + rng.nextDouble() * 0.3;
            double vRadius = 0.5 + rng.nextDouble() * 0.3;
            int branchCount = rangeCoord - rng.nextInt(rangeCoord / 3);
            WorldCarver.CarveSkipChecker skipChecker = coraline$makeSkipChecker(config, rng);
            this.createTunnel(context, config, chunk, biomeAccessor,
                    rng.nextLong(), aquifer,
                    x, y, z, hRadius, vRadius,
                    thickness, yaw, pitch, 0, branchCount,
                    this.getYScale(), carvingMask, skipChecker);
        }
    }

    @Unique
    private void coraline$spawnCaveMouth(
            CarvingContext context, CaveCarverConfiguration config,
            ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeAccessor,
            Aquifer aquifer, ChunkPos chunkPos, CarvingMask carvingMask,
            RandomSource rng
    ) {
        if (rng.nextInt(CAVE_MOUTH_CHANCE) != 0) return;
        double x = chunkPos.getBlockX(rng.nextInt(16));
        double z = chunkPos.getBlockZ(rng.nextInt(16));
        double y = 52.0 + rng.nextInt(32);
        int stubCount = 3 + rng.nextInt(3);
        float baseYaw = rng.nextFloat() * ((float) Math.PI * 2f);
        float[] angles = new float[stubCount];
        float cursor = 0f;
        for (int i = 0; i < stubCount; i++) {
            cursor += (float) Math.PI * (0.25f + rng.nextFloat() * 0.42f);
            angles[i] = baseYaw + cursor;
        }
        for (int i = 0; i < stubCount; i++) {
            float stubYaw = angles[i];
            float stubPitch;
            if (i == 0) {
                stubPitch = -(0.2f + rng.nextFloat() * 0.25f);
            } else if (i == stubCount - 1 && rng.nextBoolean()) {
                stubPitch = 0.08f + rng.nextFloat() * 0.1f;
            } else {
                stubPitch = -(rng.nextFloat() * 0.2f);
            }
            double stubHRadius = 0.4 + rng.nextDouble() * 0.45;
            double stubVRadius = 0.5 + rng.nextDouble() * 0.4;
            float stubThickness = 0.8f + rng.nextFloat() * 1.4f;
            int stubBranch = coraline$rangeCoord() / 5 + rng.nextInt(coraline$rangeCoord() / 4);
            WorldCarver.CarveSkipChecker stubChecker = coraline$makeSkipChecker(config, rng);
            this.createTunnel(context, config, chunk, biomeAccessor,
                    rng.nextLong(), aquifer,
                    x, y, z,
                    stubHRadius, stubVRadius,
                    stubThickness, stubYaw, stubPitch,
                    0, stubBranch,
                    this.getYScale(), carvingMask, stubChecker);
        }
    }

    @Unique
    private static int coraline$estimateSurfaceY(CarvingContext context, double x, double z, int scanTopY) {
        DensityFunction density = context.randomState().router().initialDensityWithoutJaggedness();
        int blockX = Mth.floor(x);
        int blockZ = Mth.floor(z);
        int topY = Math.min(scanTopY, context.getMinGenY() + context.getGenDepth() - 1);
        for (int y = topY; y > context.getMinGenY(); y -= SURFACE_SCAN_COARSE_STEP) {
            if (density.compute(new DensityFunction.SinglePointContext(blockX, y, blockZ)) > SURFACE_DENSITY_THRESHOLD) {
                for (int fine = Math.min(y + SURFACE_SCAN_COARSE_STEP - 1, topY); fine >= y; fine--) {
                    if (density.compute(new DensityFunction.SinglePointContext(blockX, fine, blockZ)) > SURFACE_DENSITY_THRESHOLD) {
                        return fine;
                    }
                }
                return y;
            }
        }
        return context.getMinGenY();
    }
}
