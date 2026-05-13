package net.zharok01.coralinesystems.mixin.fancymenu;

import com.mojang.authlib.GameProfile;
import de.keksuccino.fancymenu.customization.element.elements.playerentity.textures.CapeResourceSupplier;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.zharok01.coralinesystems.client.CoralineCapeRegistry;
import net.minecraft.client.Minecraft;

@Mixin(CapeResourceSupplier.class)
public class FancyMenuCapeMixin {

    @Shadow(remap = false) protected String lastGetterPlayerName;
    @Shadow(remap = false) protected boolean sourceIsPlayerName;

    @Inject(method = "getCapeLocation", at = @At("HEAD"), cancellable = true, remap = false)
    private void forceCoralineCapes(CallbackInfoReturnable<ResourceLocation> cir) {
        if (!this.sourceIsPlayerName || this.lastGetterPlayerName == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (!mc.getUser().getName().equalsIgnoreCase(this.lastGetterPlayerName)) return;

        GameProfile profile = mc.getUser().getGameProfile();
        if (profile.getId() == null) return;

        ResourceLocation cape = CoralineCapeRegistry.getCapeByUUID(profile.getId());
        if (cape != null) {
            cir.setReturnValue(cape);
        }
    }
}