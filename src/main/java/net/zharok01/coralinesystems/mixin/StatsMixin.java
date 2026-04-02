package net.zharok01.coralinesystems.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Stats.class)
public abstract class StatsMixin {

    @Inject(method = "makeCustomStat", at = @At("HEAD"), cancellable = true)
    private static void onMakeCustomStat(String id, StatFormatter formatter, CallbackInfoReturnable<ResourceLocation> cir) {
        if (coraline_systems$isBlacklisted(id)) {
            // By returning early, we skip Registry.register(...)
            // and the stat never exists in the game.
            cir.setReturnValue(new ResourceLocation(id));
        }
    }

    @Unique
    private static boolean coraline_systems$isBlacklisted(String id) {
        // We use a switch/case here because it is initialized instantly
        // and doesn't rely on a Set field being loaded yet.
        return switch (id) {
            case
                "clean_armor",
                "clean_banner",
                "clean_shulker_box",
                "damage_blocked_by_shield",
                "enchant_item",
                "interact_with_blast_furnace",
                "interact_with_campfire",
                "interact_with_grindstone",
                "interact_with_smithing_table",
                "interact_with_smoker",
                "open_shulker_box",
                "raid_trigger",
                "talked_to_villager",
                "raid_win",
                "interact_with_anvil",
                "traded_with_villager",
                "interact_with_item_stand",
                "open_prickly_can" -> true;
            default -> false;
        };
    }
}