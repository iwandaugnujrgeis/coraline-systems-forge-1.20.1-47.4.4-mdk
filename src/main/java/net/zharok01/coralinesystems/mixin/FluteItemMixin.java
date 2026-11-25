package net.zharok01.coralinesystems.mixin;

import net.mehvahdjukaar.supplementaries.common.items.FluteItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluteItem.class)
public class FluteItemMixin {

    @Inject(method = "interactWithPet", at = @At("HEAD"), cancellable = true, remap = false)
    private static void coraline$preventFluteEntityBinding(
            ItemStack stack,
            Player player,
            Entity target,
            InteractionHand hand,
            CallbackInfoReturnable<Boolean> callbackInfo) {
        callbackInfo.setReturnValue(false);
    }

    @Redirect(
            method = "m_5812_",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/nbt/CompoundTag;m_128441_(Ljava/lang/String;)Z"
            ),
            remap = false
    )
    private boolean coraline$ignoreBoundEntityForFoil(CompoundTag tag, String key) {
        return false;
    }

    @Redirect(
            method = "m_7373_",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;m_41737_(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;"
            ),
            remap = false
    )
    private CompoundTag coraline$ignoreBoundEntityForTooltip(ItemStack stack, String key) {
        return null;
    }

    @Redirect(
            method = "m_7203_",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;m_20984_(DDDZ)Z"
            ),
            remap = false
    )
    private boolean coraline$disableFluteEntityTeleport(LivingEntity entity, double x, double y, double z, boolean showParticles) {
        return false;
    }
}