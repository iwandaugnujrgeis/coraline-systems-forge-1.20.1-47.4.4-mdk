package net.zharok01.coralinesystems.client.advancement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class AdvancementBackdrop {

    private record Ore(ResourceLocation sprite, int weight, float minDepth) {}
    private record Blob(ResourceLocation sprite, int weight) {}

    private static final int ROLL_RANGE = 1000;
    private static final int COAL_WEIGHT = 24;
    private static final int BLOB_REGION_SIZE = 6;

    private static final int BASALT_REGION_SIZE = 8;
    private static final int BASALT_RADIUS_SQUARED = 9;
    private static final float BASALT_START_DEPTH = 0.55F;
    private static final float BASALT_MAX_CHANCE = 0.5F;
    private static final float BASALT_FILL_CHANCE = 0.85F;

    private static final ResourceLocation DIRT = block("minecraft", "dirt");
    private static final ResourceLocation STONE = block("minecraft", "stone");
    private static final ResourceLocation BASALT = block("minecraft", "basalt");
    private static final ResourceLocation BEDROCK = block("minecraft", "bedrock");
    private static final ResourceLocation COAL = block("minecraft", "coal_ore");

    private static final List<Ore> ORES = new ArrayList<>();
    private static final List<Blob> BLOBS = new ArrayList<>();
    private static final int BLOB_TOTAL_WEIGHT;

    static {
        ORES.add(new Ore(block("coraline_systems", "suspicious_block"), 2, 0.5F));
        ORES.add(new Ore(block("minecraft", "diamond_ore"), 10, 0.8F));
        ORES.add(new Ore(block("minecraft", "redstone_ore"), 6, 0.6F));
        if (ModList.get().isLoaded("rediscovered")) {
            ORES.add(new Ore(block("rediscovered", "ruby_ore"), 2, 0.0F));
        }
        ORES.add(new Ore(block("minecraft", "lapis_ore"), 4, 0.5F));
        ORES.add(new Ore(block("minecraft", "gold_ore"), 8, 0.4F));
        ORES.add(new Ore(block("minecraft", "iron_ore"), 12, 0.2F));

        BLOBS.add(new Blob(block("minecraft", "gravel"), 28));
        BLOBS.add(new Blob(block("minecraft", "andesite"), 25));
        BLOBS.add(new Blob(block("minecraft", "tuff"), 20));
        BLOBS.add(new Blob(block("minecraft", "granite"), 14));
        if (ModList.get().isLoaded("quark")) {
            BLOBS.add(new Blob(block("quark", "limestone"), 13));
        }
        BLOB_TOTAL_WEIGHT = BLOBS.stream().mapToInt(Blob::weight).sum();
    }

    private static ResourceLocation block(String namespace, String path) {
        return new ResourceLocation(namespace, "block/" + path);
    }

    private static long mixCoordinates(int x, int y) {
        long hash = ((long) x * 312547891L) ^ ((long) y * 87541243L);
        hash = (hash ^ (hash >>> 25)) * 0x7a5bc2d3L;
        hash = (hash ^ (hash >>> 17)) * 0x14f5b2a3L;
        return hash ^ (hash >>> 13);
    }

    public static ResourceLocation pickTileSprite(int gridX, int gridY, int bottomTile) {
        Random columnRandom = new Random(mixCoordinates(gridX, 0) + 57321L);
        int surfaceTile = columnRandom.nextInt(3);
        int bedrockTile = bottomTile + columnRandom.nextInt(3) - 1;

        if (gridY <= surfaceTile) return DIRT;
        if (gridY >= bedrockTile - 1) return BEDROCK;

        float depth = Mth.clamp((float) (gridY - surfaceTile) / (float) (bedrockTile - surfaceTile), 0.0F, 1.0F);
        ResourceLocation rock = isBasaltBlob(gridX, gridY, bottomTile) ? BASALT : rockAt(gridX, gridY, STONE);

        Random oreRandom = new Random(mixCoordinates(gridX, gridY) + 123456789L);
        int roll = oreRandom.nextInt(ROLL_RANGE);

        for (Ore ore : ORES) {
            if (roll < ore.weight()) return depth >= ore.minDepth() ? ore.sprite() : rock;
            roll -= ore.weight();
        }
        if (roll < COAL_WEIGHT) return oreRandom.nextFloat() >= depth ? COAL : rock;
        return rock;
    }

    private static boolean isBasaltBlob(int gridX, int gridY, int bottomTile) {
        int regionX = Math.floorDiv(gridX, BASALT_REGION_SIZE);
        int regionY = Math.floorDiv(gridY, BASALT_REGION_SIZE);

        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetY = -1; offsetY <= 1; offsetY++) {
                int blobRegionX = regionX + offsetX;
                int blobRegionY = regionY + offsetY;

                long blobSeed = mixCoordinates(blobRegionX, blobRegionY) + 555444333L;
                Random blobRandom = new Random(blobSeed);

                int centerX = blobRegionX * BASALT_REGION_SIZE + blobRandom.nextInt(BASALT_REGION_SIZE);
                int centerY = blobRegionY * BASALT_REGION_SIZE + blobRandom.nextInt(BASALT_REGION_SIZE);

                float centerDepth = Mth.clamp(centerY / (float) bottomTile, 0.0F, 1.0F);
                float spawnChance = BASALT_MAX_CHANCE
                        * Mth.clamp((centerDepth - BASALT_START_DEPTH) / (1.0F - BASALT_START_DEPTH), 0.0F, 1.0F);
                if (blobRandom.nextFloat() >= spawnChance) continue;

                int distanceX = gridX - centerX;
                int distanceY = gridY - centerY;
                if (distanceX * distanceX + distanceY * distanceY > BASALT_RADIUS_SQUARED) continue;

                if (new Random(mixCoordinates(gridX, gridY) + blobSeed).nextFloat() < BASALT_FILL_CHANCE) {
                    return true;
                }
            }
        }
        return false;
    }

    private static ResourceLocation rockAt(int gridX, int gridY, ResourceLocation baseRock) {
        int regionX = Math.floorDiv(gridX, BLOB_REGION_SIZE);
        int regionY = Math.floorDiv(gridY, BLOB_REGION_SIZE);
        ResourceLocation rock = baseRock;

        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetY = -1; offsetY <= 1; offsetY++) {
                int blobRegionX = regionX + offsetX;
                int blobRegionY = regionY + offsetY;

                long blobSeed = mixCoordinates(blobRegionX, blobRegionY) + 987654321L;
                Random blobRandom = new Random(blobSeed);
                if (blobRandom.nextFloat() >= 0.25F) continue;

                int typeRoll = blobRandom.nextInt(BLOB_TOTAL_WEIGHT);
                ResourceLocation blobRock = BLOBS.get(0).sprite();
                for (Blob blob : BLOBS) {
                    if (typeRoll < blob.weight()) {
                        blobRock = blob.sprite();
                        break;
                    }
                    typeRoll -= blob.weight();
                }

                int centerX = blobRegionX * BLOB_REGION_SIZE + blobRandom.nextInt(BLOB_REGION_SIZE);
                int centerY = blobRegionY * BLOB_REGION_SIZE + blobRandom.nextInt(BLOB_REGION_SIZE);
                if (centerY < 3) continue;

                int distanceX = Math.abs(gridX - centerX);
                int distanceY = Math.abs(gridY - centerY);
                boolean insideBlob = distanceX <= 2 && distanceY <= 2 && !(distanceX == 2 && distanceY == 2);

                if (insideBlob && new Random(mixCoordinates(gridX, gridY) + blobSeed).nextFloat() < 0.80F) {
                    rock = blobRock;
                }
            }
        }
        return rock;
    }
}