package net.zharok01.coralinesystems.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MapItemSavedData.class)
public abstract class MapItemSavedDataMixin {

    @Shadow @Final @Mutable public int centerX;
    @Shadow @Final @Mutable public int centerZ;

    @Inject(method = "createFresh", at = @At("RETURN"))
    private static void coraline$centerMapOnChunk(double x, double z, byte scale, boolean trackingPosition, boolean unlimitedTracking, ResourceKey<Level> dimension, CallbackInfoReturnable<MapItemSavedData> cir) {
        MapItemSavedDataMixin mapData = (MapItemSavedDataMixin) (Object) cir.getReturnValue();

        mapData.centerX = Mth.floor(x / 16.0) * 16 + 8;
        mapData.centerZ = Mth.floor(z / 16.0) * 16 + 8;
    }
}
