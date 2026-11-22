package net.zharok01.coralinesystems.mixin.sounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class CreativeModeMusicMixin {

    @Shadow public LocalPlayer player;

    @Inject(method = "getSituationalMusic", at = @At("RETURN"), cancellable = true)
    private void coraline$suppressCreativeMusic(CallbackInfoReturnable<Music> cir) {
        if (cir.getReturnValue() != Musics.CREATIVE) return;

        Holder<Biome> biome = this.player.level().getBiome(this.player.blockPosition());
        cir.setReturnValue(biome.value().getBackgroundMusic().orElse(Musics.GAME));
    }
}