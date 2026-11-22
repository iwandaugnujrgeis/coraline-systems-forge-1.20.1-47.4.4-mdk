package net.zharok01.coralinesystems.entity.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.zharok01.coralinesystems.registry.CoralineItems;
import net.zharok01.coralinesystems.registry.IsotopicEntities;

public class ThrownTurtleEgg extends ThrownModdedEgg {

    public ThrownTurtleEgg(EntityType<? extends ThrownTurtleEgg> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownTurtleEgg(Level level, LivingEntity shooter) {
        super(IsotopicEntities.THROWN_TURTLE_EGG.get(), shooter, level);
    }

    @Override
    protected EntityType<? extends Animal> getBabyEntityType() {
        return EntityType.TURTLE;
    }

    @Override
    protected Item getDefaultItem() {
        return CoralineItems.TURTLE_EGG.get();
    }
}