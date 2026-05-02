package net.zharok01.coralinesystems.mixin.alexsmobs;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.EntityFarseer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(EntityFarseer.class)
public class EntityFarseerMixin {

    /**
     * @author zharok_01
     * @reason Redirect Farseer spawns to the Farlands (12,550,821+) instead of just the World Border.
     */
    @Overwrite(remap = false)
    private static boolean isFarseerArea(ServerLevelAccessor iServerWorld, BlockPos pos) {
        // Respect the config toggle from Alex's Mobs
        if (!AMConfig.restrictFarseerSpawns) {
            return true;
        }

        // The edge of the Farlands where the "noise" traditionally breaks
        double farlandsStart = 12550821.0D;

        // Check absolute values to cover all four directions (North, South, East, West)
        boolean inFarlands = Math.abs(pos.getX()) >= farlandsStart || Math.abs(pos.getZ()) >= farlandsStart;

        // Return true if in Farlands OR if near the vanilla world border
        return inFarlands || iServerWorld.getWorldBorder().getDistanceToBorder(pos.getX(), pos.getZ()) < AMConfig.farseerBorderSpawnDistance;
    }
}