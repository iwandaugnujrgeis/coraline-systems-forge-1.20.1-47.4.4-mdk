package net.zharok01.coralinesystems.mixin.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.zharok01.coralinesystems.registry.CoralineTags;
import net.zharok01.coralinesystems.util.CoralineBannedMobs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
public class ZombieMixin {

    @Shadow
    @Final
    private static EntityDataAccessor<Boolean> DATA_DROWNED_CONVERSION_ID;

    @Shadow
    private int conversionTime;

    @ModifyVariable(method = "setCanBreakDoors", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private boolean coraline$forceBreakDoors(boolean original) {
        return true;
    }

    @SuppressWarnings("null")
    @Inject(method = "tick", at = @At("HEAD"))
    private void coraline$clearBannedDrownedConversion(CallbackInfo ci) {
        Zombie zombie = (Zombie) (Object) this;
        if (!zombie.level().isClientSide && coraline$isDrownedBanned() && zombie.isUnderWaterConverting()) {
            conversionTime = -1;
            zombie.getEntityData().set(DATA_DROWNED_CONVERSION_ID, false);
        }
    }

    @Inject(method = "convertsInWater", at = @At("HEAD"), cancellable = true)
    private void coraline$preventStartingDrownedConversion(CallbackInfoReturnable<Boolean> cir) {
        if (coraline$isDrownedBanned()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "convertToZombieType", at = @At("HEAD"), cancellable = true)
    private void coraline$preventBannedZombieConversion(EntityType<? extends Zombie> targetType, CallbackInfo ci) {
        if (CoralineBannedMobs.isBanned(targetType)) {
            ci.cancel();
        }
    }

    @Unique
    private static boolean coraline$isDrownedBanned() {
        return CoralineBannedMobs.isBanned(EntityType.DROWNED);
    }

    @SuppressWarnings({ "deprecation", "null" })
    @Inject(method = "populateDefaultEquipmentSlots", at = @At("TAIL"))
    private void coraline$populateCustomEquipment(RandomSource random, DifficultyInstance difficulty, CallbackInfo ci) {
        Zombie zombie = (Zombie) (Object) this;

        if (zombie.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
            if (random.nextFloat() < 0.10F) {
                BuiltInRegistries.ITEM.getTag(CoralineTags.ZOMBIE_HOLDABLE_ITEMS)
                        .flatMap(tag -> tag.getRandomElement(random)).ifPresent(holder -> {
                            zombie.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(holder.value()));
                        });
            }
        }
    }
}