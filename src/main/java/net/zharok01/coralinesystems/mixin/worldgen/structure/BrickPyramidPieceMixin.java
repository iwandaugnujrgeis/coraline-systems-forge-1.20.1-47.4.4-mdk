package net.zharok01.coralinesystems.mixin.worldgen.structure;

import com.legacy.rediscovered.entity.dragon.RedDragonBossEntity;
import com.legacy.rediscovered.registry.RediscoveredEntityTypes;
import com.legacy.rediscovered.world.structure.BrickPyramidStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.block.TesseractBlockEntity;
import net.zharok01.coralinesystems.registry.CoralineBlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrickPyramidStructure.Piece.class)
public class BrickPyramidPieceMixin {

    @Inject(method = "handleDataMarker", at = @At("HEAD"), cancellable = true, remap = false)
    private void coraline$interceptPylonMarker(String key, BlockPos pos, ServerLevelAccessor level,
                                               RandomSource rand, BoundingBox bounds, CallbackInfo ci) {
        if (!"pylon".equals(key)) return;
        ci.cancel();

        BlockPos altarPos = pos.immutable();
        BlockPos tesseractPos = pos.above(7).immutable();

        level.setBlock(altarPos, CoralineBlocks.ALTAR.get().defaultBlockState(), 3);
        level.setBlock(tesseractPos, CoralineBlocks.TESSERACT.get().defaultBlockState(), 3);

        RedDragonBossEntity dragon = RediscoveredEntityTypes.RED_DRAGON_BOSS.create(level.getLevel());
        if (dragon == null) return;

        Vec3 dragonSpawnPos = Vec3.atCenterOf(tesseractPos).add(0, 30, 0);
        dragon.moveTo(dragonSpawnPos);
        dragon.homePos = altarPos;
        dragon.setPylonTime(1200);
        level.addFreshEntity(dragon);

        ServerLevel sl = level.getLevel();
        if (sl.getBlockEntity(tesseractPos) instanceof TesseractBlockEntity tesseract) {
            tesseract.initFromStructure(dragon.getUUID(), altarPos);
        }

        ChunkPos homeChunk = new ChunkPos(altarPos);
        sl.getChunkSource().addRegionTicket(RedDragonBossEntity.RED_DRAGON_TICKET, homeChunk, 9, homeChunk);
    }
}