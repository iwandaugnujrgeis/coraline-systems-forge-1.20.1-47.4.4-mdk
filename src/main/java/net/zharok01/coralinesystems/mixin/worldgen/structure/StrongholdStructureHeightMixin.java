package net.zharok01.coralinesystems.mixin.worldgen.structure;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(StrongholdStructure.class)
public abstract class StrongholdStructureHeightMixin {

    @Redirect(
            method = "generatePieces",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/structure/pieces/StructurePiecesBuilder;moveBelowSeaLevel(IILnet/minecraft/util/RandomSource;I)I"
            )
    )
    private static int coraline$customStrongholdYPlacement(StructurePiecesBuilder builder, int seaLevel, int minY, RandomSource random, int topOffset) {
        BoundingBox box = builder.getBoundingBox();
        int strongholdHeight = box.getYSpan();

        int minTopY = minY + strongholdHeight + 10;
        int maxTopY = seaLevel + 4;

        if (maxTopY > minTopY) {
            int targetTopY = minTopY + random.nextInt(maxTopY - minTopY + 1);
            int currentTopY = box.maxY();
            int deltaY = targetTopY - currentTopY;

            builder.offsetPiecesVertically(deltaY);
            return deltaY;
        } else {
            return builder.moveBelowSeaLevel(seaLevel, minY, random, 0);
        }
    }
}