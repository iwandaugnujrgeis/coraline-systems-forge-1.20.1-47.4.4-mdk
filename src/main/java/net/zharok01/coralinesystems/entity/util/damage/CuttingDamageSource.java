package net.zharok01.coralinesystems.entity.util.damage;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class CuttingDamageSource extends DamageSource {

    public CuttingDamageSource(Holder<DamageType> holder) {
        super(holder);
    }

    @Override
    public @NotNull Component getLocalizedDeathMessage(LivingEntity livingEntity) {
        LivingEntity killer = livingEntity.getKillCredit();
        String key = "death.attack." + this.type().msgId();
        return killer != null
                ? Component.translatable(key + ".player", livingEntity.getDisplayName(), killer.getDisplayName())
                : Component.translatable(key, livingEntity.getDisplayName());
    }
}