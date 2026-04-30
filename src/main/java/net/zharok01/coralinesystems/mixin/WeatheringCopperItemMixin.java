package net.zharok01.coralinesystems.mixin;

import com.teamabnormals.caverns_and_chasms.common.item.copper.WeatheringCopperItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = WeatheringCopperItem.class, remap = false)
public interface WeatheringCopperItemMixin {

    /**
     * @author
     * @reason
     */

    @Overwrite(remap = false)
    default void updateOxidation(ItemStack stack, Level level) {
    }
}