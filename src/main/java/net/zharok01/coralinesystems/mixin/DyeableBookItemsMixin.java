package net.zharok01.coralinesystems.mixin;

import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.WritableBookItem;
import net.minecraft.world.item.WrittenBookItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({WritableBookItem.class, WrittenBookItem.class})
public abstract class DyeableBookItemsMixin implements DyeableLeatherItem {
}
