package net.zharok01.coralinesystems.mixin;

import com.legacy.rediscovered.item.RubyEyeItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = RubyEyeItem.class, remap = false)
public class RubyEyeItemMixin {

    // Define the ResourceKey for the Brick Pyramid
    private static final ResourceKey<Structure> BRICK_PYRAMID_KEY =
            ResourceKey.create(Registries.STRUCTURE, new ResourceLocation("rediscovered", "brick_pyramid"));

    /**
     * Intercepts the assignment in the 'use' method (obfuscated as m_7203_)
     * where it defaults to TRAIL_RUINS (obfuscated as f_276588_)
     */
    @Redirect(
            method = "m_7203_",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/levelgen/structure/BuiltinStructures;f_276588_:Lnet/minecraft/resources/ResourceKey;")
    )
    private ResourceKey<Structure> coraline$redirectUseTarget() {
        return BRICK_PYRAMID_KEY;
    }

    /**
     * Intercepts the assignment in findAndStorePoi
     * (Custom method, so it keeps its name, but the field is still obfuscated)
     */
    @Redirect(
            method = "findAndStorePoi",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/levelgen/structure/BuiltinStructures;f_276588_:Lnet/minecraft/resources/ResourceKey;")
    )
    private static ResourceKey<Structure> coraline$redirectPoiTarget() {
        return BRICK_PYRAMID_KEY;
    }
}