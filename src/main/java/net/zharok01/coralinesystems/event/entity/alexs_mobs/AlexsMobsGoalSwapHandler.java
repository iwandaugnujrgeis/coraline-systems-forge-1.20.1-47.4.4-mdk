package net.zharok01.coralinesystems.event.entity.alexs_mobs;

import com.github.alexthe666.alexsmobs.entity.*;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;
import net.zharok01.coralinesystems.entity.ai.*;

import java.util.List;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AlexsMobsGoalSwapHandler {

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;

        if (event.getEntity() instanceof EntityGrizzlyBear bear) {
            swapGrizzlyBearGoals(bear);
        } else if (event.getEntity() instanceof EntityRaccoon raccoon) {
            swapRaccoonGoals(raccoon);
        } else if (event.getEntity() instanceof EntityCrow crow) {
            swapCrowGoals(crow);
        } else if (event.getEntity() instanceof EntityTasmanianDevil devil) {
            swapTasmanianDevilGoals(devil);
        } else if (event.getEntity() instanceof EntityGorilla gorilla) {
            swapGorillaGoals(gorilla);
        } else if (event.getEntity() instanceof EntitySugarGlider glider) {
            addSugarGliderGoals(glider);
        } else if (event.getEntity() instanceof EntitySeal seal) {
            swapSealGoals(seal);
        }
    }

    private static void swapGrizzlyBearGoals(EntityGrizzlyBear bear) {
        List<WrappedGoal> targetGoalsToRemove = bear.targetSelector.getAvailableGoals().stream()
                .filter(wrapped -> {
                    String name = wrapped.getGoal().getClass().getSimpleName();
                    return name.equals("AttackPlayerGoal")
                            || name.equals("CreatureAITargetItems")
                            || name.equals("NonTameRandomTargetGoal");
                })
                .toList();
        targetGoalsToRemove.forEach(wrapped -> bear.targetSelector.removeGoal(wrapped.getGoal()));

        List<WrappedGoal> moveGoalsToRemove = bear.goalSelector.getAvailableGoals().stream()
                .filter(wrapped -> {
                    String name = wrapped.getGoal().getClass().getSimpleName();
                    return name.equals("TameableAIFollowOwner")
                            || name.equals("SitWhenOrderedToGoal");
                })
                .toList();
        moveGoalsToRemove.forEach(wrapped -> bear.goalSelector.removeGoal(wrapped.getGoal()));

        bear.targetSelector.addGoal(3, new GrizzlyBearProtectCubGoal(bear));
        bear.goalSelector.addGoal(5, new BearEatItemsGoal(bear));
    }

    private static void swapRaccoonGoals(EntityRaccoon raccoon) {
        WrappedGoal followGoal = raccoon.goalSelector.getAvailableGoals().stream()
                .filter(wrapped -> wrapped.getGoal().getClass().getSimpleName().equals("TameableAIFollowOwner"))
                .findFirst()
                .orElse(null);

        if (followGoal != null) {
            raccoon.goalSelector.removeGoal(followGoal.getGoal());
            raccoon.goalSelector.addGoal(followGoal.getPriority(), new SprintingFollowOwnerGoal(raccoon));
        }

        List<WrappedGoal> goalsToRemove = raccoon.goalSelector.getAvailableGoals().stream()
                .filter(wrapped -> {
                    String name = wrapped.getGoal().getClass().getSimpleName();
                    return name.equals("LeapAtTargetGoal")
                            || name.equals("MeleeAttackGoal")
                            || name.equals("RaccoonAIWash")
                            || name.equals("AnimalAILootChests");
                })
                .toList();
        goalsToRemove.forEach(wrapped -> raccoon.goalSelector.removeGoal(wrapped.getGoal()));

        List<WrappedGoal> targetGoalsToRemove = raccoon.targetSelector.getAvailableGoals().stream()
                .filter(wrapped -> wrapped.getGoal().getClass().getSimpleName().equals("CreatureAITargetItems"))
                .toList();
        targetGoalsToRemove.forEach(wrapped -> raccoon.targetSelector.removeGoal(wrapped.getGoal()));

        raccoon.goalSelector.addGoal(5, new RaccoonPebbleThrowGoal(raccoon));
        raccoon.goalSelector.addGoal(6, new RaccoonScatterChestGoal(raccoon));
        raccoon.goalSelector.addGoal(7, new RaccoonEatItemsGoal(raccoon));
    }

    private static void swapCrowGoals(EntityCrow crow) {
        WrappedGoal followGoal = crow.goalSelector.getAvailableGoals().stream()
                .filter(wrapped -> wrapped.getGoal().getClass().getSimpleName().equals("CrowAIFollowOwner"))
                .findFirst()
                .orElse(null);

        if (followGoal != null) {
            crow.goalSelector.removeGoal(followGoal.getGoal());
            crow.goalSelector.addGoal(followGoal.getPriority(), new CrowSprintingFollowOwnerGoal(crow));
        }

        crow.goalSelector.addGoal(6, new CrowEatItemsGoal(crow));
    }

    private static void swapSealGoals(EntitySeal seal) {
        List<WrappedGoal> targetGoalsToRemove = seal.targetSelector.getAvailableGoals().stream()
                .filter(wrapped -> wrapped.getGoal().getClass().getSimpleName().equals("CreatureAITargetItems"))
                .toList();
        targetGoalsToRemove.forEach(wrapped -> seal.targetSelector.removeGoal(wrapped.getGoal()));

        seal.goalSelector.addGoal(6, new SealEatItemsGoal(seal));
    }

    private static void swapTasmanianDevilGoals(EntityTasmanianDevil devil) {
        List<WrappedGoal> targetGoalsToRemove = devil.targetSelector.getAvailableGoals().stream()
                .filter(wrapped -> {
                    String name = wrapped.getGoal().getClass().getSimpleName();
                    return name.equals("NearestAttackableTargetGoal")
                            || name.equals("CreatureAITargetItems");
                })
                .toList();
        targetGoalsToRemove.forEach(wrapped -> devil.targetSelector.removeGoal(wrapped.getGoal()));

        devil.goalSelector.addGoal(2, new TasmanianDevilEatItemsGoal(devil));
    }

    private static void addSugarGliderGoals(EntitySugarGlider glider) {
        glider.goalSelector.addGoal(2, new GliderKiteSpiderGoal(glider));
        glider.goalSelector.addGoal(4, new GliderCollectSticksGoal(glider));
        glider.goalSelector.addGoal(4, new GliderSleepGoal(glider));
    }

    private static void swapGorillaGoals(EntityGorilla gorilla) {
        List<WrappedGoal> targetGoalsToRemove = gorilla.targetSelector.getAvailableGoals().stream()
                .filter(wrapped -> wrapped.getGoal().getClass().getSimpleName().equals("CreatureAITargetItems"))
                .toList();
        targetGoalsToRemove.forEach(wrapped -> gorilla.targetSelector.removeGoal(wrapped.getGoal()));

        List<WrappedGoal> moveGoalsToRemove = gorilla.goalSelector.getAvailableGoals().stream()
                .filter(wrapped -> wrapped.getGoal().getClass().getSimpleName().equals("SitWhenOrderedToGoal"))
                .toList();
        moveGoalsToRemove.forEach(wrapped -> gorilla.goalSelector.removeGoal(wrapped.getGoal()));

        gorilla.goalSelector.addGoal(5, new GorillaEatItemsGoal(gorilla));
    }
}