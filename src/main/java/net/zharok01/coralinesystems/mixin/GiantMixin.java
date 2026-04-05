package net.zharok01.coralinesystems.mixin;

import com.legacy.rediscovered.entity.pigman.PigmanEntity;
import net.minecraft.world.entity.EntityType;
//import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
//import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
//import java.util.UUID;

@Mixin(Giant.class)
public abstract class GiantMixin extends Monster {

    protected GiantMixin(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(EntityType<? extends Giant> type, Level level, CallbackInfo ci) {

        // --- Attributes ---
        Objects.requireNonNull(this.getAttribute(Attributes.MOVEMENT_SPEED))
                .setBaseValue(0.2D);

        // Low damage — the kick is a shove, not a killing blow.
        // 4.0 = 2 hearts.

        /*
        Objects.requireNonNull(this.getAttribute(Attributes.ATTACK_DAMAGE))
                .setBaseValue(4.0D);

        // High knockback — this is the whole point of the kick.
        // Vanilla scale: 0 = none, 1 = normal sword, 3+ = very strong.
        // AttributeModifier with ADDITION operation so it stacks cleanly.
        Objects.requireNonNull(this.getAttribute(Attributes.ATTACK_KNOCKBACK))
                .addPermanentModifier(new AttributeModifier(
                        UUID.fromString("a3b4c5d6-e7f8-1234-abcd-000000000001"),
                        "Giant kick knockback",
                        3.0D,
                        AttributeModifier.Operation.ADDITION
                ));
         */

        // --- Goals ---
        // MeleeAttackGoal drives swing() → attackTime on the model, which our
        // HumanoidModelMixin intercepts to play the leg kick animation.
        // speedModifier 1.0, followIfNotSeen false (Giant is big enough to
        // close distance fast; we don't want it to chase forever if it loses sight).

        //this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 32.0F));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0D));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, PigmanEntity.class, true));
    }
}