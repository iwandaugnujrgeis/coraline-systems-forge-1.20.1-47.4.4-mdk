package net.zharok01.coralinesystems.mixin.color;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.material.MapColor;
import net.zharok01.coralinesystems.util.CoralineDyeColors;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(DyeColor.class)
public abstract class DyeColorMixin {

    @Shadow @Mutable @Final
    private static DyeColor[] $VALUES;

    @Invoker("<init>")
    public static DyeColor cs$newColor(
            String enumName, int ordinal,
            int id, String serializedName,
            int textureDiffuseColor,
            MapColor mapColor,
            int fireworkColor, int textColor) {
        throw new AssertionError("Mixin invoker not applied");
    }

    @Inject(
            method = "<clinit>",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/item/DyeColor;$VALUES:[Lnet/minecraft/world/item/DyeColor;",
                    shift = At.Shift.AFTER
            )
    )
    private static void cs$injectCustomColors(CallbackInfo ci) {
        List<DyeColor> colors = new ArrayList<>(Arrays.asList($VALUES));
        int nextOrdinal = colors.get(colors.size() - 1).ordinal() + 1;

        for (CoralineDyeColors entry : CoralineDyeColors.values()) {
            float[] f = entry.getTextureDiffuseColors();
            int packed = ((int)(f[0] * 255) << 16) | ((int)(f[1] * 255) << 8) | (int)(f[2] * 255);

            DyeColor injected = cs$newColor(
                    entry.name(),
                    nextOrdinal++,
                    entry.getId(),
                    entry.getName(),
                    packed,
                    entry.getMapColor(),
                    entry.getFireworkColor(),
                    entry.getTextColor()
            );
            colors.add(injected);
        }

        $VALUES = colors.toArray(new DyeColor[0]);
    }
}