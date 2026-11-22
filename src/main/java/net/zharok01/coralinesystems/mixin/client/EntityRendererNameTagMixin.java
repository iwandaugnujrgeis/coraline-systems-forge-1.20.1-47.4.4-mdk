package net.zharok01.coralinesystems.mixin.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererNameTagMixin {

    @Unique
    private static final int SEE_THROUGH_TEXT_ALPHA = 0x80;

    @Redirect(
            method = "renderNameTag",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/client/ForgeHooksClient;isNameplateInRenderDistance(Lnet/minecraft/world/entity/Entity;D)Z",
                    remap = false
            )
    )
    private boolean coraline$extendLivingNameTagDistance(Entity entity, double squareDistance) {
        double adjustedSquareDistance = entity instanceof LivingEntity ? squareDistance * 0.25D : squareDistance;
        return ForgeHooksClient.isNameplateInRenderDistance(entity, adjustedSquareDistance);
    }

    @ModifyArgs(
            method = "renderNameTag",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font;drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I"
            )
    )
    private void coraline$increaseSeeThroughNameTagOpacity(Args args) {
        if (args.get(7) == Font.DisplayMode.SEE_THROUGH) {
            int textColor = args.get(3);
            args.set(3, (textColor & 0x00FFFFFF) | (SEE_THROUGH_TEXT_ALPHA << 24));
        }
    }
}