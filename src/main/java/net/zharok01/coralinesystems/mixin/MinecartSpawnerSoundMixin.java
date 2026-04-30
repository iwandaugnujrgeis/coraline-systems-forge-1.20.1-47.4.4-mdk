package net.zharok01.coralinesystems.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.MinecartSoundInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartSpawner;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(MinecartSpawner.class)
public abstract class MinecartSpawnerSoundMixin extends AbstractMinecart {

    protected MinecartSpawnerSoundMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * WHY THIS IS NEEDED:
     * MinecartSoundInstance is a looping AbstractTickableSoundInstance that ticks
     * automatically once queued, updating its position, volume, and pitch based on
     * the cart's movement. It just needs to be started once via SoundManager.play().
     *
     * AbstractMinecart.tick() contains client-side sound management, but it is gated
     * on the minecart type. MinecartSpawner.getPickResult() returns Items.MINECART
     * for the SPAWNER type (Mojang never added a spawner minecart item), which causes
     * the vanilla type-check in the sound management path to skip it entirely.
     *
     * The fix: lazily start the sound instance ourselves on the first client tick.
     * The null check ensures we only queue it once — the SoundManager then owns it
     * and ticks it every frame until MinecartSoundInstance.tick() calls stop()
     * when the cart is removed.
     */
    @Unique
    private MinecartSoundInstance coraline$soundInstance = null;

    @Inject(method = "tick", at = @At("TAIL"))
    private void coraline$startRidingSound(CallbackInfo ci) {
        if (!this.level().isClientSide()) return;

        if (coraline$soundInstance == null) {
            coraline$soundInstance = new MinecartSoundInstance(this);
            Minecraft.getInstance().getSoundManager().play(coraline$soundInstance);
        }
    }
}