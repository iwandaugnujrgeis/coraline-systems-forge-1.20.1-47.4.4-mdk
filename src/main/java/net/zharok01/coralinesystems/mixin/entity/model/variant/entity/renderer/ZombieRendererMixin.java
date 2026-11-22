package net.zharok01.coralinesystems.mixin.entity.model.variant.entity.renderer;

import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.zharok01.coralinesystems.client.entity.variant.CoralineMobVariant;
import net.zharok01.coralinesystems.client.entity.variant.CoralineVariantHolder;
import net.zharok01.coralinesystems.client.entity.variant.CoralineVariants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractZombieRenderer.class)
public class ZombieRendererMixin {

    @Inject(
            method = "getTextureLocation(Lnet/minecraft/world/entity/monster/Zombie;)Lnet/minecraft/resources/ResourceLocation;",
            at = @At("HEAD"),
            cancellable = true
    )

    @SuppressWarnings("removal")
    private void coralineSystems$getTexture(Zombie zombie, CallbackInfoReturnable<ResourceLocation> cir) {
        if (!(zombie instanceof CoralineVariantHolder holder)) {
            return;
        }

        CoralineMobVariant variant = CoralineVariants.getVariantFromNametag(EntityType.ZOMBIE, zombie.hasCustomName() ? zombie.getName().getString() : null);
        if (variant == null) {
            variant = holder.coralineSystems$getVariant();
        }

        if (variant.isDefault()) {
            return;
        }

        cir.setReturnValue(new ResourceLocation(variant.id().getNamespace(),
                "textures/entity/zombie/" + variant.id().getPath() + ".png"));
    }
}
