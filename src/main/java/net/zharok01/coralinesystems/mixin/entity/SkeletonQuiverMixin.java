package net.zharok01.coralinesystems.mixin.entity;

import com.legacy.rediscovered.registry.RediscoveredItems;
import net.minecraft.core.NonNullList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zharok01.coralinesystems.quiver.QuiverContents;
import net.zharok01.coralinesystems.quiver.QuiverVolley;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSkeleton.class)
public abstract class SkeletonQuiverMixin {

    @Unique
    private static final float QUIVER_SPAWN_CHANCE = 0.2F;

    @Unique
    private static final int MIN_QUIVER_ARROW_COUNT = 18;

    @Unique
    private static final int MAX_QUIVER_ARROW_COUNT = 54;

    @Inject(method = "populateDefaultEquipmentSlots", at = @At("TAIL"))
    private void coraline$equipQuiver(RandomSource random, DifficultyInstance difficulty, CallbackInfo ci) {
        if (!((Object) this instanceof Skeleton skeleton) || !skeleton.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
            return;
        }

        if (random.nextFloat() >= QUIVER_SPAWN_CHANCE) {
            return;
        }


        ItemStack quiver = new ItemStack(RediscoveredItems.quiver);
        NonNullList<ItemStack> contents = NonNullList.withSize(QuiverContents.SLOT_COUNT, ItemStack.EMPTY);
        int arrowCount = random.nextInt(MAX_QUIVER_ARROW_COUNT - MIN_QUIVER_ARROW_COUNT + 1)
                + MIN_QUIVER_ARROW_COUNT;
        contents.set(0, new ItemStack(Items.ARROW, arrowCount));
        QuiverContents.write(quiver, contents);
        skeleton.setItemSlot(EquipmentSlot.CHEST, quiver);
        skeleton.setDropChance(EquipmentSlot.CHEST, 1.0F);
    }

    @Inject(method = "performRangedAttack", at = @At("HEAD"))
    private void coraline$beginQuiverVolley(LivingEntity target, float velocity, CallbackInfo ci) {
        if ((Object) this instanceof Skeleton skeleton && QuiverContents.isQuiver(skeleton.getItemBySlot(EquipmentSlot.CHEST))) {
            QuiverVolley.coraline$beginSkeletonVolley(skeleton);
        }
    }

    @Inject(method = "performRangedAttack", at = @At("RETURN"))
    private void coraline$finishQuiverVolley(LivingEntity target, float velocity, CallbackInfo ci) {
        if ((Object) this instanceof Skeleton skeleton && QuiverContents.isQuiver(skeleton.getItemBySlot(EquipmentSlot.CHEST))) {
            QuiverVolley.coraline$finishSkeletonVolley(skeleton);
        }
    }
}
