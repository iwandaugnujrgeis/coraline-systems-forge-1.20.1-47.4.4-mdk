package net.zharok01.coralinesystems.entity.projectile;

import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.zharok01.coralinesystems.registry.CoralineItems;
import net.zharok01.coralinesystems.registry.IsotopicEntities;

public class ThrownPlatypusEgg extends ThrownModdedEgg {

    public ThrownPlatypusEgg(EntityType<? extends ThrownPlatypusEgg> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownPlatypusEgg(Level level, LivingEntity shooter) {
        super(IsotopicEntities.THROWN_PLATYPUS_EGG.get(), shooter, level);
    }

    @Override
    protected EntityType<? extends Animal> getBabyEntityType() {
        return AMEntityRegistry.PLATYPUS.get();
    }

    @Override
    protected Item getDefaultItem() {
        return CoralineItems.PLATYPUS_EGG.get();
    }
}