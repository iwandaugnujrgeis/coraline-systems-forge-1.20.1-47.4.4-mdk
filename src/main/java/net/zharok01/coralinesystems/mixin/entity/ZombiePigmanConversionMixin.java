package net.zharok01.coralinesystems.mixin.entity;

import com.legacy.rediscovered.entity.ZombiePigmanEntity;
import com.legacy.rediscovered.entity.util.IPigman.Type;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = ZombiePigmanEntity.class, remap = false)
public abstract class ZombiePigmanConversionMixin {

    @Inject(
            method = "finishCure(Lnet/minecraft/server/level/ServerLevel;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/event/ForgeEventFactory;onLivingConvert(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void coraline$wakeUpCuredPigman(ServerLevel level, CallbackInfo ci, byte typeId, Type wow, Mob pigman) {
        pigman.setNoActionTime(0);

        pigman.getNavigation().stop();
    }
}