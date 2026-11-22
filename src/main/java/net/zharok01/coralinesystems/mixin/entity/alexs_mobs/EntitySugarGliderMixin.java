package net.zharok01.coralinesystems.mixin.entity.alexs_mobs;

import com.github.alexthe666.alexsmobs.entity.EntitySugarGlider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.zharok01.coralinesystems.entity.ai.SleepingSugarGlider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntitySugarGlider.class, remap = false)
public class EntitySugarGliderMixin implements SleepingSugarGlider {

    @Unique private static final String SLEEPING_NBT_KEY = "CoralineSleeping";

    @Unique
    private static final EntityDataAccessor<Boolean> coraline$SLEEPING =
            SynchedEntityData.defineId(EntitySugarGlider.class, EntityDataSerializers.BOOLEAN);

    @Redirect(
            method = "Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;m_6071_(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
            at = @At(value = "INVOKE",
                    target = "Lcom/github/alexthe666/alexsmobs/entity/EntitySugarGlider;setCommand(I)V")
    )
    private void coraline$skipWandering(EntitySugarGlider instance, int command) {
        instance.setCommand(command == 0 ? 1 : command);
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "m_8097_()V", at = @At("TAIL"))
    private void coraline$defineSleeping(CallbackInfo ci) {
        ((Entity) (Object) this).getEntityData().define(coraline$SLEEPING, false);
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "m_7380_(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
    private void coraline$saveSleeping(CompoundTag tag, CallbackInfo ci) {
        tag.putBoolean(SLEEPING_NBT_KEY, this.coraline$isSleeping());
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "m_7378_(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
    private void coraline$loadSleeping(CompoundTag tag, CallbackInfo ci) {
        this.coraline$setSleeping(tag.getBoolean(SLEEPING_NBT_KEY));
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "m_7515_()Lnet/minecraft/sounds/SoundEvent;", at = @At("HEAD"), cancellable = true)
    private void coraline$purrWhileSleeping(CallbackInfoReturnable<SoundEvent> cir) {
        if (this.coraline$isSleeping()) {
            cir.setReturnValue(SoundEvents.FOX_SLEEP);
        }
    }

    @Override
    public boolean coraline$isSleeping() {
        return ((Entity) (Object) this).getEntityData().get(coraline$SLEEPING);
    }

    @Override
    public void coraline$setSleeping(boolean sleeping) {
        ((Entity) (Object) this).getEntityData().set(coraline$SLEEPING, sleeping);
    }
}