package net.zharok01.coralinesystems.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zharok01.coralinesystems.quiver.QuiverVolley;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin {
    @Inject(method = "releaseUsing", at = @At("HEAD"))
    private void coraline$beginLoad(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged, CallbackInfo callback) {
        QuiverVolley.begin(level, livingEntity);
    }

    @Inject(method = "releaseUsing", at = @At("RETURN"))
    private void coraline$finishLoad(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged, CallbackInfo callback) {
        QuiverVolley.finishCrossbowLoad(stack, level, livingEntity);
    }

    @Inject(method = "performShooting", at = @At("HEAD"))
    private static void coraline$beginShot(Level level, LivingEntity shooter, InteractionHand usedHand, ItemStack crossbowStack, float velocity, float inaccuracy, CallbackInfo callback) {
        QuiverVolley.beginCrossbowShot(level, shooter, crossbowStack);
    }

    @Inject(method = "performShooting", at = @At("RETURN"))
    private static void coraline$finishShot(Level level, LivingEntity shooter, InteractionHand usedHand, ItemStack crossbowStack, float velocity, float inaccuracy, CallbackInfo callback) {
        QuiverVolley.finishCrossbowShot(crossbowStack, level, shooter);
    }
}