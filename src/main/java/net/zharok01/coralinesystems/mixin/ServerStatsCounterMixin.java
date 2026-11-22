package net.zharok01.coralinesystems.mixin;

import com.mojang.datafixers.DataFixer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.player.Player;
import net.zharok01.coralinesystems.util.CoralineStatBlacklist;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerStatsCounter.class)
public abstract class ServerStatsCounterMixin extends StatsCounter {

    @Inject(method = "setValue", at = @At("HEAD"), cancellable = true)
    private void coraline$ignoreBlacklistedStats(Player player, Stat<?> stat, int value, CallbackInfo ci) {
        if (CoralineStatBlacklist.isBlacklisted(stat)) {
            ci.cancel();
        }
    }

    @Inject(method = "parseLocal", at = @At("RETURN"))
    private void coraline$dropBlacklistedStats(DataFixer fixerUpper, String json, CallbackInfo ci) {
        this.stats.keySet().removeIf(CoralineStatBlacklist::isBlacklisted);
    }
}