package net.zharok01.coralinesystems.mixin.entity.alexs_mobs;

import com.github.alexthe666.alexsmobs.entity.EntityBlueJay;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityBlueJay.class, remap = false)
public abstract class EntityBlueJayMixin extends Animal {

    protected EntityBlueJayMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow private int blueTime;

    @Shadow @Final private static EntityDataAccessor<Boolean> BLUE_VISUAL_FLAG;

    @Override
    public int getAmbientSoundInterval() {
        return super.getAmbientSoundInterval() * 2;
    }

    @Inject(method = "m_7378_", at = @At("TAIL"))
    private void coraline$restoreBlueVisualFlag(CompoundTag compound, CallbackInfo ci) {
        if (this.blueTime > 0) {
            ((EntityBlueJay) (Object) this).getEntityData().set(BLUE_VISUAL_FLAG, true);
        }
    }
}