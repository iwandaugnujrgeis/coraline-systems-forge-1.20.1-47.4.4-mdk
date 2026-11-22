package net.zharok01.coralinesystems.entity.projectile;

import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.zharok01.coralinesystems.registry.CoralineItems;
import net.zharok01.coralinesystems.registry.IsotopicEntities;

public class ThrownCaimanEgg extends ThrownModdedEgg {

    public ThrownCaimanEgg(EntityType<? extends ThrownCaimanEgg> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownCaimanEgg(Level level, LivingEntity shooter) {
        super(IsotopicEntities.THROWN_CAIMAN_EGG.get(), shooter, level);
    }

    @Override
    protected EntityType<? extends Animal> getBabyEntityType() {
        return AMEntityRegistry.CAIMAN.get();
    }

    @Override
    protected Item getDefaultItem() {
        return CoralineItems.CAIMAN_EGG.get();
    }
}