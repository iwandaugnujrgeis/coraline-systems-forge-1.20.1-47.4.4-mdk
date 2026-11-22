package net.zharok01.coralinesystems.entity.ai;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;

public class BreedEggDropGoal extends BreedGoal {
    private final ItemStack eggItem;

    public BreedEggDropGoal(Animal animal, double speedModifier, ItemStack eggItem) {
        super(animal, speedModifier);
        this.eggItem = eggItem;
    }

    public BreedEggDropGoal(Animal animal, double speedModifier, Class<? extends Animal> partnerClass, ItemStack eggItem) {
        super(animal, speedModifier, partnerClass);
        this.eggItem = eggItem;
    }

    @Override
    protected void breed() {
        ServerPlayer serverplayer = this.animal.getLoveCause();
        if (serverplayer == null && this.partner.getLoveCause() != null) {
            serverplayer = this.partner.getLoveCause();
        }

        if (serverplayer != null) {
            serverplayer.awardStat(Stats.ANIMALS_BRED);
            CriteriaTriggers.BRED_ANIMALS.trigger(serverplayer, this.animal, this.partner, this.animal);
        }

        this.animal.setAge(6000);
        this.partner.setAge(6000);
        this.animal.resetLove();
        this.partner.resetLove();

        this.animal.spawnAtLocation(this.eggItem.copy());

        if (this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            this.level.addFreshEntity(new ExperienceOrb(this.level, this.animal.getX(), this.animal.getY(), this.animal.getZ(), this.animal.getRandom().nextInt(7) + 1));
        }
    }
}
