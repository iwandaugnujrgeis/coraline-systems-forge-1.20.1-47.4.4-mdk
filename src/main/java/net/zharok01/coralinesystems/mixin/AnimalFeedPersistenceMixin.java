package net.zharok01.coralinesystems.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Animal.class)
public class AnimalFeedPersistenceMixin {

    /**
     * Marks an animal as persistent the first time a player successfully feeds it.
     *
     * We intercept the return value of mobInteract rather than injecting at HEAD
     * because at RETURN we already know whether the interaction consumed the item
     * (i.e. the animal actually ate something). consumesAction() is true for both
     * InteractionResult.SUCCESS and CONSUME_PARTIAL, covering the adult love-mode
     * path and the baby age-up path in one check.
     *
     * The isFood guard ensures we only react to genuine feeding interactions and
     * not to other right-click actions on the animal (e.g. attaching a lead).
     */
    @ModifyReturnValue(method = "mobInteract", at = @At("RETURN"))
    private InteractionResult coraline$markPersistentOnFeed(InteractionResult original,
                                                            Player player, InteractionHand hand) {
        if (original.consumesAction()) {
            Animal self = (Animal) (Object) this;
            ItemStack held = player.getItemInHand(hand);
            if (self.isFood(held) && !self.isPersistenceRequired()) {
                self.setPersistenceRequired();
            }
        }
        return original;
    }
}