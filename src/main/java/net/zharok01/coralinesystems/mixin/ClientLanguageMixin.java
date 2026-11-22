package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Mixin(ClientLanguage.class)
public class ClientLanguageMixin {

    @Redirect(
            method = "loadFrom",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/packs/resources/ResourceManager;getNamespaces()Ljava/util/Set;"
            )
    )
    private static Set<String> coralineSystems$reorderNamespacesForLang(ResourceManager instance) {
        Set<String> originalNamespaces = instance.getNamespaces();

        List<String> orderedNamespaces = new ArrayList<>(originalNamespaces);

        if (orderedNamespaces.remove("coraline_systems")) {
            orderedNamespaces.add("coraline_systems");
        }

        return new LinkedHashSet<>(orderedNamespaces);
    }
}