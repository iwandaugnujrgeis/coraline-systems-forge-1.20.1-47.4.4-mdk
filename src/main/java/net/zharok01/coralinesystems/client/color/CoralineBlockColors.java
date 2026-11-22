package net.zharok01.coralinesystems.client.color;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.block.state.BlockState;
import net.zharok01.coralinesystems.block.BrewingCauldronColors;
import org.jetbrains.annotations.Nullable;

public final class CoralineBlockColors {

    /*
    public static final BlockColor WOOD_SORREL = (BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) ->
            level != null && pos != null
                    ? BiomeColors.getAverageGrassColor(level, pos)
                    : GrassColor.get(0.5D, 1.0D);
    */

    public static final BlockColor CAULDRON_CONTENT = (BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) ->
            BrewingCauldronColors.getContentColor(state, tintIndex);

    private CoralineBlockColors() {
    }
}