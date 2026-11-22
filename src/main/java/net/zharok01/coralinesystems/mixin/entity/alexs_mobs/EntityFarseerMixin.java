package net.zharok01.coralinesystems.mixin.entity.alexs_mobs;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.EntityFarseer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(EntityFarseer.class)
public class EntityFarseerMixin {

    @Overwrite(remap = false)
    private static boolean isFarseerArea(ServerLevelAccessor iServerWorld, BlockPos pos) {
        if (!AMConfig.restrictFarseerSpawns) {
            return true;
        }

        double farlandsStart = 12550821.0D;
        boolean inFarlands = Math.abs(pos.getX()) >= farlandsStart || Math.abs(pos.getZ()) >= farlandsStart;
        return inFarlands || iServerWorld.getWorldBorder().getDistanceToBorder(pos.getX(), pos.getZ()) < AMConfig.farseerBorderSpawnDistance;
    }
}