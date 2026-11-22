package net.zharok01.coralinesystems.mixin.worldgen.structure;

import com.legacy.rediscovered.world.structure.ObsidianWallPieces;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ObsidianWallPieces.Piece.class, remap = false)
public abstract class ObsidianWallPieceMixin {

    @Redirect(
            method = "m_213694_",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/WorldGenLevel;m_5452_(Lnet/minecraft/world/level/levelgen/Heightmap$Types;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;"
            )
    )
    private BlockPos useOceanFloorHeightmap(WorldGenLevel level, Heightmap.Types ignoredType, BlockPos pos) {
        return level.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR_WG, pos);
    }
}