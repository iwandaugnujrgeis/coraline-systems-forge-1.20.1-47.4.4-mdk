package net.zharok01.coralinesystems.mixin;

import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitherSkeleton.class)
public abstract class WitherSkeletonMixin extends AbstractSkeleton {

    protected WitherSkeletonMixin(EntityType<? extends AbstractSkeleton> type, Level level) {
        super(type, level);
    }

    @Inject(method = "populateDefaultEquipmentSlots", at = @At("TAIL"))
    private void coraline$replaceStoneSword(RandomSource random, DifficultyInstance difficulty, CallbackInfo ci) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
    }
}