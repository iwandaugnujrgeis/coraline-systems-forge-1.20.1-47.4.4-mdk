package net.zharok01.coralinesystems.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zharok01.coralinesystems.quiver.QuiverVolley;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BowItem.class)
public abstract class BowItemMixin {
    @Inject(method = "releaseUsing", at = @At("HEAD"))
    private void coraline$beginVolley(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged, CallbackInfo callback) {
        QuiverVolley.begin(level, livingEntity);
    }

    @Inject(method = "releaseUsing", at = @At("RETURN"))
    private void coraline$finishVolley(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged, CallbackInfo callback) {
        QuiverVolley.finish(stack, level, livingEntity, (BowItem) (Object) this);
    }
}
