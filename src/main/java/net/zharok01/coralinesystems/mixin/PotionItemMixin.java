package net.zharok01.coralinesystems.mixin;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PotionItem.class)
public abstract class PotionItemMixin extends Item {

    @Unique
    private static final String coraline$NO_EFFECTS_KEY = "effect.none";

    public PotionItemMixin(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(@Nonnull ItemStack itemStack) {
        return super.isFoil(itemStack) || !PotionUtils.getMobEffects(itemStack).isEmpty();
    }

    @Inject(method = "appendHoverText", at = @At("TAIL"))
    private void coraline$removeNoEffectsTooltip(ItemStack itemStack, @Nullable Level level,
            List<Component> tooltipComponents, TooltipFlag isAdvanced, CallbackInfo callbackInfo) {
        if (!(itemStack.is(Items.POTION) || itemStack.is(Items.SPLASH_POTION))
                || PotionUtils.getPotion(itemStack) != Potions.WATER) return;

        tooltipComponents.removeIf(line ->
                line.getContents() instanceof TranslatableContents translatable
                        && translatable.getKey().equals(coraline$NO_EFFECTS_KEY));
    }
}