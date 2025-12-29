package net.zharok01.coralinesystems.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RespawnAnchorBlock.class)
public class RespawnAnchorFuelMixin {

    @Inject(method = "isRespawnFuel", at = @At("HEAD"), cancellable = true)
    private static void overrideFuel(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Item soulHeart = ForgeRegistries.ITEMS.getValue(new ResourceLocation("alexsmobs", "soul_heart"));

        if (soulHeart != null && stack.is(soulHeart)) {
            cir.setReturnValue(true);
        }

        else { cir.setReturnValue(false); }
    }
}
