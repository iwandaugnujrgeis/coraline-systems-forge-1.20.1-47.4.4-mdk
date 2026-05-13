package net.zharok01.coralinesystems.mixin.fancymenu;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import de.keksuccino.fancymenu.customization.element.elements.playerentity.textures.SkinResourceSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkinResourceSupplier.class)
public class FancyMenuSkinMixin {

    @Shadow(remap = false) protected String lastGetterPlayerName;
    @Shadow(remap = false) protected boolean sourceIsPlayerName;

    @Inject(method = "getSkinLocation", at = @At("HEAD"), cancellable = true, remap = false)
    private void mirrorGameSkin(CallbackInfoReturnable<ResourceLocation> cir) {
        if (!this.sourceIsPlayerName || this.lastGetterPlayerName == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (!mc.getUser().getName().equalsIgnoreCase(this.lastGetterPlayerName)) return;

        GameProfile profile = mc.getUser().getGameProfile();
        if (profile.getId() == null) return;

        // Capture the skin location from the game's own skin manager.
        // registerSkins() fires the callback synchronously when the skin
        // is already in the local cache (which it is after login).
        ResourceLocation[] captured = { null };
        mc.getSkinManager().registerSkins(profile, (type, location, texture) -> {
            if (type == MinecraftProfileTexture.Type.SKIN) {
                captured[0] = location;
            }
        }, false); // false = don't require secure profile

        if (captured[0] != null) {
            cir.setReturnValue(captured[0]);
        }
        // If null, the skin isn't cached yet — fall through to FancyMenu's
        // own async fetch as a safe fallback. No harm done.
    }
}