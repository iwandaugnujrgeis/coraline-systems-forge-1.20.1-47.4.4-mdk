package net.zharok01.coralinesystems.mixin;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zharok01.coralinesystems.util.AnimationTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {

    protected PlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @ModifyVariable(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isInvulnerableTo(Lnet/minecraft/world/damagesource/DamageSource;)Z"), ordinal = 0, argsOnly = true)
    private float coralinesystems$onSwordBlocking(float damage, DamageSource source) {
        Player player = (Player) (Object) this;

        // Ensure we don't process damage if they're naturally invulnerable or the damage bypasses armor (e.g. Void, Magic)
        if (!player.isInvulnerableTo(source) && !source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            if (this.coralinesystems$isSwordBlocking() && damage > 0) {
                damage = (1F + damage) * 0.5F; // Cuts the incoming damage essentially in half
            }
        }
        return damage;
    }

    @Unique
    private boolean coralinesystems$isSwordBlocking() {
        if (this.isUsingItem()) {
            ItemStack activeItem = this.getUseItem();
            if (!activeItem.isEmpty()) {
                return activeItem.getItem().getUseAnimation(activeItem) == AnimationTypes.SWORD_BLOCK;
            }
        }
        return false;
    }
}