package net.zharok01.coralinesystems.mixin.compat.quark;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.violetmoon.zeta.block.ZetaGlassBlock;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(value = ZetaGlassBlock.class, remap = false)
public abstract class ZetaGlassBeaconMixin extends Block {

    public ZetaGlassBeaconMixin(Properties properties) {
        super(properties);
    }

    @Unique
    private static final Map<Block, float[]> coraline$colorCache = new ConcurrentHashMap<>();

    @Unique
    private static final float[] coraline$noColor = new float[0];

    @Override
    @Nullable
    @SuppressWarnings("deprecation")
    public float[] getBeaconColorMultiplier(BlockState state, LevelReader level, BlockPos pos, BlockPos beaconPos) {
        float[] cachedColor = coraline$colorCache.computeIfAbsent(this, block -> {
            ResourceLocation location = BuiltInRegistries.BLOCK.getKey(block);
            if ("quark".equals(location.getNamespace())) {
                String path = location.getPath();
                if (path.endsWith("_framed_glass")) {
                    String colorName = path.substring(0, path.length() - "_framed_glass".length());
                    for (DyeColor dye : DyeColor.values()) {
                        if (dye.getName().equals(colorName)) {
                            return dye.getTextureDiffuseColors();
                        }
                    }
                }
            }
            return coraline$noColor;
        });

        return cachedColor == coraline$noColor ? null : cachedColor;
    }
}