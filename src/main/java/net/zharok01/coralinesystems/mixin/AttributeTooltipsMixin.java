package net.zharok01.coralinesystems.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.quark.content.client.resources.AttributeDisplayType;
import org.violetmoon.quark.content.client.tooltip.AttributeTooltips;

@Mixin(value = AttributeTooltips.class, remap = false)
public class AttributeTooltipsMixin {

    @Inject(
            method = "format",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void cs_fixAttributeFormatting(Attribute attribute, double value, AttributeDisplayType displayType, CallbackInfoReturnable<MutableComponent> cir) {
        ResourceLocation id = ForgeRegistries.ATTRIBUTES.getKey(attribute);
        if (id == null) {
            return;
        }

        String namespace = id.getNamespace();
        String path = id.getPath();

        if ("rediscovered".equals(namespace) && ("fire_resistance".equals(path) || "explosion_resistance".equals(path))) {
            double modifierAmount = value;

            if (displayType != AttributeDisplayType.PERCENTAGE && displayType != AttributeDisplayType.DIFFERENCE) {
                modifierAmount -= attribute.getDefaultValue();
            }

            String prefix = modifierAmount > 0.0 ? "+" : "";
            String formattedStr = prefix + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(modifierAmount * 100.0) + "%";

            MutableComponent component = Component.literal(formattedStr)
                    .withStyle(modifierAmount < 0.0 ? ChatFormatting.RED : ChatFormatting.WHITE);

            cir.setReturnValue(component);
        }
        else if (displayType == AttributeDisplayType.MULTIPLIER) {
            AttributeSupplier playerAttributes = DefaultAttributes.getSupplier(EntityType.PLAYER);

            if (playerAttributes.hasAttribute(attribute)) {
                double baseValue = playerAttributes.getBaseValue(attribute);
                double scaledValue = value / baseValue;

                double percentage = (scaledValue - 1.0) * 100.0;

                String prefix = percentage > 0.0 ? "+" : "";
                String formattedStr = prefix + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(percentage) + "%";

                MutableComponent component = Component.literal(formattedStr)
                        .withStyle(percentage < 0.0 ? ChatFormatting.RED : ChatFormatting.WHITE);

                cir.setReturnValue(component);
            }
        }
    }
}