package net.zharok01.coralinesystems.entity.ai;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;

public class UniversalBreedEggDropGoal extends BreedGoal {
    private final ItemStack eggItem;

    // Standard constructor
    public UniversalBreedEggDropGoal(Animal animal, double speedModifier, ItemStack eggItem) {
        super(animal, speedModifier);
        this.eggItem = eggItem;
    }

    // Constructor for cross-breeding specific classes (if needed later)
    public UniversalBreedEggDropGoal(Animal animal, double speedModifier, Class<? extends Animal> partnerClass, ItemStack eggItem) {
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
            // We pass 'this.animal' as the child to prevent NullPointerExceptions in advancements triggers
            CriteriaTriggers.BRED_ANIMALS.trigger(serverplayer, this.animal, this.partner, this.animal);
        }

        // Reset love and age them up so they can't breed again immediately
        this.animal.setAge(6000);
        this.partner.setAge(6000);
        this.animal.resetLove();
        this.partner.resetLove();

        // --- THE MAGIC: Spawn the egg item instead of a baby ---
        this.animal.spawnAtLocation(this.eggItem.copy());

        // Drop Experience Orbs (Standard Vanilla Behavior)
        if (this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            this.level.addFreshEntity(new ExperienceOrb(this.level, this.animal.getX(), this.animal.getY(), this.animal.getZ(), this.animal.getRandom().nextInt(7) + 1));
        }
    }
}