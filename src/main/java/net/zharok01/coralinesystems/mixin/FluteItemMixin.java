package net.zharok01.coralinesystems.mixin;

import net.mehvahdjukaar.supplementaries.common.items.FluteItem;
import net.minecraft.world.entity.LivingEntity;
import net.zharok01.coralinesystems.util.helper.PetTeleportHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FluteItem.class)
public class FluteItemMixin {

    @Redirect(
            method = "m_7203_",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;m_20984_(DDDZ)Z"
            ),
            remap = false
    )
    private boolean coraline$onFluteTeleport(LivingEntity pet, double x, double y, double z, boolean showParticles) {
        double oldX = pet.getX();
        double oldY = pet.getY();
        double oldZ = pet.getZ();

        boolean teleported = pet.randomTeleport(x, y, z, showParticles);

        if (teleported) {
            PetTeleportHelper.playTeleportEffects(pet, oldX, oldY, oldZ);
        }

        return teleported;
    }
}