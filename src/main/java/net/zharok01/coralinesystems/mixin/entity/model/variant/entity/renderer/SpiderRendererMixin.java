package net.zharok01.coralinesystems.mixin.entity.model.variant.entity.renderer;

import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Spider;
import net.zharok01.coralinesystems.client.entity.variant.CoralineMobVariant;
import net.zharok01.coralinesystems.client.entity.variant.CoralineVariantHolder;
import net.zharok01.coralinesystems.client.entity.variant.CoralineVariants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpiderRenderer.class)
public class SpiderRendererMixin {

    @Inject(
            method = "getTextureLocation(Lnet/minecraft/world/entity/monster/Spider;)Lnet/minecraft/resources/ResourceLocation;",
            at = @At("HEAD"),
            cancellable = true
    )

    @SuppressWarnings("removal")
    private void coralineSystems$getTexture(Spider spider, CallbackInfoReturnable<ResourceLocation> cir) {
        CoralineMobVariant variant = CoralineVariants.getVariantFromNametag(EntityType.SPIDER, spider.hasCustomName() ? spider.getName().getString() : null);
        if (variant == null) {
            variant = ((CoralineVariantHolder) spider).coralineSystems$getVariant();
        }

        if (variant.isDefault()) {
            return;
        }

        cir.setReturnValue(new ResourceLocation(variant.id().getNamespace(),
                "textures/entity/spider/" + variant.id().getPath() + ".png"));
    }
}
