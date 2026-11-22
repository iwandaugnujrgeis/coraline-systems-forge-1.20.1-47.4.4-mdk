package net.zharok01.coralinesystems.mixin.accessor;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PotionBrewing.class)
public interface PotionBrewingAccessor {

    @Invoker("addMix")
    static void coraline$invokeAddMix(Potion potionEntry, Item potionIngredient, Potion potionResult) {
        throw new AssertionError();
    }
}