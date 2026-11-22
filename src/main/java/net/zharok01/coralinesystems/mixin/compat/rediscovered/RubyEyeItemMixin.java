package net.zharok01.coralinesystems.mixin.compat.rediscovered;

import com.legacy.rediscovered.item.RubyEyeItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = RubyEyeItem.class, remap = false)
public class RubyEyeItemMixin {

    @Unique
    @SuppressWarnings("removal")
    private static final ResourceKey<Structure> BRICK_PYRAMID_KEY =
            ResourceKey.create(Registries.STRUCTURE, new ResourceLocation("rediscovered", "brick_pyramid"));

    @Redirect(
            method = "m_7203_",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/levelgen/structure/BuiltinStructures;f_276588_:Lnet/minecraft/resources/ResourceKey;")
    )
    private ResourceKey<Structure> coraline$redirectUseTarget() {
        return BRICK_PYRAMID_KEY;
    }

    @Redirect(
            method = "findAndStorePoi",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/levelgen/structure/BuiltinStructures;f_276588_:Lnet/minecraft/resources/ResourceKey;")
    )
    private static ResourceKey<Structure> coraline$redirectPoiTarget() {
        return BRICK_PYRAMID_KEY;
    }
}