package net.zharok01.coralinesystems.mixin;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.zharok01.coralinesystems.util.NewResourceFilterSection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPackResourceManager.class)
public abstract class MultiPackResourceManagerMixin {

    @Final
    @Shadow private Map<String, FallbackResourceManager> namespacedManagers;

    @Final
    @Shadow private List<PackResources> packs;

    @SuppressWarnings("null")
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(PackType type, List<PackResources> packs,
                        CallbackInfo ci) {
        this.namespacedManagers.replaceAll(
                (e, v) -> new FallbackResourceManager(type, e));

        List<String> all_namespaces =
                this.packs.stream()
                        .flatMap(
                                (pack) -> { return pack.getNamespaces(type).stream(); })
                        .distinct()
                        .toList();

        for (PackResources pack : this.packs) {
            NewResourceFilterSection filters = this.coraline$getNewPackFilterSection(pack);
            Set<String> pack_namespaces = pack.getNamespaces(type);
            Predicate<ResourceLocation> predicate = filters != null ? filters::isFiltered : null;

            for (String ns : all_namespaces) {
                boolean has_ns_files = pack_namespaces.contains(ns);
                boolean has_filters =
                        filters != null && filters.isNamespaceFiltered(ns);
                if (has_ns_files || has_filters) {
                    FallbackResourceManager manager = this.namespacedManagers.get(ns);
                    if (has_ns_files && has_filters) {
                        manager.push(pack, predicate);
                    } else if (has_ns_files) {
                        manager.push(pack);
                    } else {
                        manager.pushFilterOnly(pack.packId(), predicate);
                    }
                }
            }
        }
    }

    @Unique @Nullable
    private NewResourceFilterSection coraline$getNewPackFilterSection(PackResources pack) {
        try {
            return pack.getMetadataSection(NewResourceFilterSection.TYPE);
        } catch (IOException ioexception) {
            return null;
        }
    }
}