package net.zharok01.coralinesystems.util.helper;

import com.github.alexthe666.alexsmobs.entity.EntityTasmanianDevil;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class EatItemsHelper {

    public static final int BITE_DELAY_TICKS = 10;
    public static final int COOLDOWN_TICKS = 40;

    public static void eatOne(Mob mob, ItemEntity target, SoundEvent sound) {
        ItemStack eaten = target.getItem();

        mob.playSound(sound, 1.0F, 0.9F + mob.getRandom().nextFloat() * 0.2F);

        if (mob.level() instanceof ServerLevel serverLevel) {
            double x = mob.getX();
            double y = mob.getY() + mob.getBbHeight() * 0.75D;
            double z = mob.getZ();

            serverLevel.sendParticles(ParticleTypes.HEART, x, y, z, 3, 0.25D, 0.25D, 0.25D, 0.0D);
            serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, eaten), x, y, z, 8, 0.15D, 0.15D, 0.15D, 0.1D);

            applyFoodEffects(mob, eaten);
        }

        ItemStack remaining = eaten.copy();
        remaining.shrink(1);
        if (remaining.isEmpty()) {
            target.discard();
        } else {
            target.setItem(remaining);
        }
    }

    public static void applyFoodEffects(Mob mob, ItemStack food) {
        FoodProperties foodProperties = food.getFoodProperties(mob);
        if (foodProperties == null) return;

        boolean safeRottenFlesh = mob instanceof EntityTasmanianDevil && food.is(Items.ROTTEN_FLESH);
        for (Pair<MobEffectInstance, Float> effect : foodProperties.getEffects()) {
            if (safeRottenFlesh && effect.getFirst().getEffect() == MobEffects.POISON) continue;

            if (mob.getRandom().nextFloat() < effect.getSecond()) {
                mob.addEffect(new MobEffectInstance(effect.getFirst()));
            }
        }
    }
}