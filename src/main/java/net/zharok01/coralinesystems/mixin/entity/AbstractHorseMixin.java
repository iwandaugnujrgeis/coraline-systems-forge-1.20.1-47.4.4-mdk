package net.zharok01.coralinesystems.mixin.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import net.zharok01.coralinesystems.registry.CoralineTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorse.class)
public abstract class AbstractHorseMixin {

    @Shadow
    private void eating() {
    }

    @Inject(method = "isFood", at = @At("HEAD"), cancellable = true)
    private void coraline$isFood(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.is(CoralineTags.HORSE_FOOD_ITEMS)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "handleEating", at = @At("HEAD"), cancellable = true)
    private void coraline$handleEating(Player player, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.is(CoralineTags.HORSE_FOOD_ITEMS)) {
            AbstractHorse horse = (AbstractHorse) (Object) this;
            boolean flag = false;

            if (horse.getHealth() < horse.getMaxHealth()) {
                horse.heal(2.0F);
                flag = true;
            }

            if (horse.isBaby()) {
                horse.level().addParticle(ParticleTypes.HAPPY_VILLAGER, horse.getRandomX(1.0D), horse.getRandomY() + 0.5D, horse.getRandomZ(1.0D), 0.0D, 0.0D, 0.0D);
                if (!horse.level().isClientSide) {
                    horse.ageUp(30);
                }
                flag = true;
            }

            if ((flag || !horse.isTamed()) && horse.getTemper() < horse.getMaxTemper()) {
                flag = true;
                if (!horse.level().isClientSide) {
                    horse.modifyTemper(3);
                }
            }

            if (flag) {
                this.eating();
                horse.gameEvent(GameEvent.EAT);
            }

            cir.setReturnValue(flag);
        }
    }
}