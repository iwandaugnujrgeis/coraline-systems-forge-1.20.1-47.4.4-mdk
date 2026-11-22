package net.zharok01.coralinesystems.event.entity.alexs_mobs;

import com.github.alexthe666.alexsmobs.entity.EntityCaiman;
import com.github.alexthe666.alexsmobs.entity.EntityPlatypus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.zharok01.coralinesystems.CoralineSystems;

import java.util.List;

@Mod.EventBusSubscriber(modid = CoralineSystems.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AlexsMobsBreedingHandler {
    private static final int DEFAULT_BREED_PRIORITY = 2;

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;

        if (event.getEntity() instanceof EntityPlatypus platypus) {
            coraline$useBabyBreeding(platypus);

            if (platypus.hasEgg()) {
                CompoundTag tag = new CompoundTag();
                platypus.addAdditionalSaveData(tag);
                tag.putBoolean("HasEgg", false);
                platypus.readAdditionalSaveData(tag);
            }
        } else if (event.getEntity() instanceof EntityCaiman caiman) {
            coraline$useBabyBreeding(caiman);
        }
    }

    private static void coraline$useBabyBreeding(Animal animal) {
        List<WrappedGoal> goals = animal.goalSelector.getAvailableGoals().stream().toList();

        boolean hasVanillaBreedGoal = goals.stream()
                .anyMatch(wrapped -> wrapped.getGoal().getClass() == BreedGoal.class);

        List<WrappedGoal> eggGoals = goals.stream()
                .filter(wrapped -> coraline$isEggGoal(wrapped.getGoal()))
                .toList();

        int priority = eggGoals.stream()
                .filter(wrapped -> wrapped.getGoal() instanceof BreedGoal)
                .mapToInt(WrappedGoal::getPriority)
                .findFirst()
                .orElse(DEFAULT_BREED_PRIORITY);

        eggGoals.forEach(wrapped -> animal.goalSelector.removeGoal(wrapped.getGoal()));

        if (!hasVanillaBreedGoal) {
            animal.goalSelector.addGoal(priority, new BreedGoal(animal, 1.0));
        }
    }

    private static boolean coraline$isEggGoal(Goal goal) {
        return (goal instanceof BreedGoal && goal.getClass() != BreedGoal.class)
                || goal.getClass().getSimpleName().equals("LayEggGoal");
    }
}