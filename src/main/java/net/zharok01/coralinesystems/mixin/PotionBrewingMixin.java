package net.zharok01.coralinesystems.mixin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PotionBrewing.class)
public class PotionBrewingMixin {

    @Final
    @Shadow
    private static List<PotionBrewing.Mix<Potion>> POTION_MIXES;

    @Shadow
    private static void addMix(Potion potionEntry, Item potionIngredient, Potion potionResult) {
    }

    @Inject(method = "bootStrap", at = @At("TAIL"))
    private static void coraline$rebaseBrewingOnWater(CallbackInfo ci) {
        POTION_MIXES.removeIf(mix -> mix.from.get() == Potions.AWKWARD
                || mix.to.get() == Potions.AWKWARD);

        addMix(Potions.WATER, Items.GOLDEN_CARROT, Potions.NIGHT_VISION);
        addMix(Potions.WATER, Items.MAGMA_CREAM, Potions.FIRE_RESISTANCE);
        addMix(Potions.WATER, Items.RABBIT_FOOT, Potions.LEAPING);
        addMix(Potions.WATER, Items.TURTLE_HELMET, Potions.TURTLE_MASTER);
        addMix(Potions.WATER, Items.SUGAR, Potions.SWIFTNESS);
        addMix(Potions.WATER, Items.PUFFERFISH, Potions.WATER_BREATHING);
        addMix(Potions.WATER, Items.GLISTERING_MELON_SLICE, Potions.HEALING);
        addMix(Potions.WATER, Items.SPIDER_EYE, Potions.POISON);
        addMix(Potions.WATER, Items.GHAST_TEAR, Potions.REGENERATION);
        addMix(Potions.WATER, Items.BLAZE_POWDER, Potions.STRENGTH);
        addMix(Potions.WATER, Items.FEATHER, Potions.SLOW_FALLING);
    }
}