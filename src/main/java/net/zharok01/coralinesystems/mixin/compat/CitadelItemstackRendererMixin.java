package net.zharok01.coralinesystems.mixin.compat;

import com.github.alexthe666.citadel.client.CitadelItemstackRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.IForgeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CitadelItemstackRenderer.class, remap = false)
public class CitadelItemstackRendererMixin {

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(
            method = {
                    "renderByItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
                    "m_108829_(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/registries/IForgeRegistry;getValue(Lnet/minecraft/resources/ResourceLocation;)Ljava/lang/Object;",
                    ordinal = 0
            )
    )
    private Object coraline$fallbackToAirWhenRegistryIsCleared(IForgeRegistry<?> registry, ResourceLocation id) {
        Object value = registry.getValue(id);
        return value != null ? value : Items.AIR;
    }
}