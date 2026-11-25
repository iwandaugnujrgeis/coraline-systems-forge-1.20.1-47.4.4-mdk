package net.zharok01.coralinesystems.mixin.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.zharok01.coralinesystems.util.CoralineSpawnCategories;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin {

    @Inject(method = "getCategory", at = @At("HEAD"), cancellable = true)
    private void coraline$useMonsterSpawnCategoryForMooshrooms(CallbackInfoReturnable<MobCategory> cir) {
        if (CoralineSpawnCategories.coraline$isMooshroom((EntityType<?>) (Object) this)) {
            cir.setReturnValue(MobCategory.MONSTER);
        }
    }
}
