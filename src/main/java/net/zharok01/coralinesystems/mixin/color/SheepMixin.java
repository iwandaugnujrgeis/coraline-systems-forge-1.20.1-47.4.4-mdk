package net.zharok01.coralinesystems.mixin.color;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.util.CoralineDyeColors;
import net.zharok01.coralinesystems.registry.CoralineBlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(Sheep.class)
public abstract class SheepMixin {

    @SuppressWarnings("removal")
    @Unique
    private static final Function<DyeColor, ResourceLocation> CS_LOOT_TABLES =
            Util.memoize(color -> new ResourceLocation(CoralineSystems.MOD_ID, "entities/sheep/" + color.getName()));

    @ModifyConstant(method = {"getColor", "setColor"}, constant = @Constant(intValue = 15))
    private int coraline$expandColorMask(int original) {
        return original + 16;
    }

    @ModifyConstant(method = "setColor", constant = @Constant(intValue = 240))
    private int coraline$adjustColorClearMask(int original) {
        return original - 16;
    }

    @ModifyConstant(method = {"isSheared", "setSheared"}, constant = @Constant(intValue = 16))
    private int coraline$moveShearedFlag(int original) {
        return original + 16;
    }

    @ModifyConstant(method = "setSheared", constant = @Constant(intValue = -17))
    private int coraline$adjustShearedClearMask(int original) {
        return original - 16;
    }
    
    @WrapOperation(
            method = "shear",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/animal/Sheep;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;I)Lnet/minecraft/world/entity/item/ItemEntity;"
            )
    )
    private ItemEntity coraline$shear(Sheep self, ItemLike item, int count, Operation<ItemEntity> original) {
        if (CoralineDyeColors.isModColor(self.getColor())) {
            ItemLike modWool = CoralineBlocks.getWoolForColor(self.getColor());
            if (modWool != null) {
                return original.call(self, modWool, count);
            }
        }
        return original.call(self, item, count);
    }
    
    @WrapOperation(
            method = "onSheared",
            remap = false,
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack coraline$onSheared(ItemLike item, Operation<ItemStack> original) {
        Sheep self = (Sheep)(Object)this;
        if (CoralineDyeColors.isModColor(self.getColor())) {
            ItemLike modWool = CoralineBlocks.getWoolForColor(self.getColor());
            if (modWool != null) {
                return new ItemStack(modWool);
            }
        }
        return original.call(item);
    }
    
    @Inject(method = "getDefaultLootTable", at = @At("HEAD"), cancellable = true)
    private void coraline$getDefaultLootTable(CallbackInfoReturnable<ResourceLocation> cir) {
        Sheep self = (Sheep)(Object)this;
        if (!self.isSheared() && CoralineDyeColors.isModColor(self.getColor())) {
            cir.setReturnValue(CS_LOOT_TABLES.apply(self.getColor()));
        }
    }

    @Unique
    private static final int DYED_SHEEP_ROLL_RANGE = 500;

    @Unique
    private static final CoralineDyeColors[] DYED_SHEEP_COLORS = CoralineDyeColors.values();

    @Inject(method = "getRandomSheepColor", at = @At("RETURN"), cancellable = true)
    private static void coraline$randomSheepColor(RandomSource random, CallbackInfoReturnable<DyeColor> cir) {
        if (cir.getReturnValue() != DyeColor.WHITE) {
            return;
        }

        int roll = random.nextInt(DYED_SHEEP_ROLL_RANGE);
        if (roll < DYED_SHEEP_COLORS.length) {
            cir.setReturnValue(DYED_SHEEP_COLORS[roll].dyeColor());
        }
    }
}