package net.zharok01.coralinesystems.mixin;

import javax.annotation.Nonnull;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.zharok01.coralinesystems.registry.CoralineItems;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Inject(method = "hasMix", at = @At("HEAD"), cancellable = true)
    private static void coraline$disableSoulstealBrewing(@Nonnull ItemStack input, @Nonnull ItemStack reagent, CallbackInfoReturnable<Boolean> cir) {
        if (coraline$isSoulstealBrewing(input, reagent)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "mix", at = @At("HEAD"), cancellable = true)
    private static void coraline$blockSoulstealMix(@Nonnull ItemStack reagent, @Nonnull ItemStack potion, CallbackInfoReturnable<ItemStack> cir) {
        if (coraline$isSoulstealBrewing(potion, reagent)) {
            cir.setReturnValue(potion);
        }
    }

    private static boolean coraline$isSoulstealBrewing(@Nonnull ItemStack input, @Nonnull ItemStack reagent) {
        return coraline$isSoulstealPotion(input)
                || (PotionUtils.getPotion(input) == Potions.AWKWARD && reagent.is(CoralineItems.SOUL_HEART.get()));
    }

    // ASSUMPTION: Alex's Mobs stores potion registry IDs in the standard Potion item tag.
    private static boolean coraline$isSoulstealPotion(@Nonnull ItemStack itemStack) {
        net.minecraft.nbt.CompoundTag potionTag = itemStack.getTag();
        if (potionTag == null) {
            return false;
        }

        return switch (potionTag.getString("Potion")) {
            case "alexsmobs:soulsteal", "alexsmobs:long_soulsteal", "alexsmobs:strong_soulsteal" -> true;
            default -> false;
        };
    }
}