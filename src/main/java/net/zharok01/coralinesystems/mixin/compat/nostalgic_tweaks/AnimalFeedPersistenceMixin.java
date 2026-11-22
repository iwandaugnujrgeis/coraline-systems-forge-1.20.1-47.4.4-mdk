package net.zharok01.coralinesystems.mixin.compat.nostalgic_tweaks;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.zharok01.coralinesystems.util.helper.AnimalPersistenceHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Animal.class)
public class AnimalFeedPersistenceMixin {

    @Inject(method = "usePlayerItem", at = @At("HEAD"))
    private void coraline$markPersistentOnFeed(Player player, InteractionHand hand, ItemStack stack, CallbackInfo ci) {
        Animal self = (Animal) (Object) this;
        if (!self.level().isClientSide && AnimalPersistenceHelper.coraline$shouldPersist(self)) {
            self.setPersistenceRequired();
        }
    }
}