package net.zharok01.coralinesystems.mixin.color;

import com.google.common.collect.ImmutableList;
import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.minecraft.world.item.DyeColor;
import net.zharok01.coralinesystems.util.CoralineDyeColors;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = BlocksColorAPI.class, remap = false)
public abstract class BlocksColorAPIMixin {

    @Shadow @Mutable @Final
    public static List<DyeColor> SORTED_COLORS;

    @Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
    private static void coraline$stripCoralineColors(CallbackInfo ci) {
        SORTED_COLORS = SORTED_COLORS.stream()
                .filter(color -> CoralineDyeColors.fromDyeColor(color) == null)
                .collect(ImmutableList.toImmutableList());
    }
}