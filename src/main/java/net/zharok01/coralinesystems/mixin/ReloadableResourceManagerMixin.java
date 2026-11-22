package net.zharok01.coralinesystems.mixin;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.Unit;
import net.zharok01.coralinesystems.client.gui.creative_tabs.CoralineCreativeTabLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ReloadableResourceManager.class)
public class ReloadableResourceManagerMixin {

    @Inject(method = "createReload", at = @At("RETURN"))
    private void coraline$onCreateReload(
            Executor backgroundExecutor,
            Executor gameExecutor,
            CompletableFuture<Unit> waitingFor,
            List<PackResources> resourcePacks,
            CallbackInfoReturnable<ReloadInstance> cir) {

        CoralineCreativeTabLoader.INSTANCE.reload();
    }
}
