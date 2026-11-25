package net.zharok01.coralinesystems.mixin.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.zharok01.coralinesystems.util.interfaces.CowMilkDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MushroomCow.class)
public abstract class MushroomCowMixin {

    @Inject(method = "checkMushroomSpawnRules", at = @At("HEAD"), cancellable = true)
    private static void coraline$checkMonsterStyleSpawnRules(
            EntityType<MushroomCow> mushroomCow, LevelAccessor level, MobSpawnType spawnType, BlockPos pos,
            RandomSource random, CallbackInfoReturnable<Boolean> cir) {
        if (level instanceof ServerLevelAccessor serverLevel) {
            BlockState ground = level.getBlockState(pos.below());
            cir.setReturnValue(
                    serverLevel.getDifficulty() != Difficulty.PEACEFUL
                            && Mob.checkMobSpawnRules(mushroomCow, level, spawnType, pos, random)
                            && (ground.is(BlockTags.MOOSHROOMS_SPAWNABLE_ON) || ground.is(Blocks.PODZOL))
            );
        }
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void coraline$limitStewMilking(Player player, InteractionHand hand,
                                           CallbackInfoReturnable<InteractionResult> cir) {
        MushroomCow mushroomCow = (MushroomCow) (Object) this;
        if (player.getItemInHand(hand).is(Items.BOWL) && !mushroomCow.isBaby()) {
            CowMilkDuck duck = (CowMilkDuck) (Object) this;
            if (!duck.coraline$canReceiveStewServing()) {
                cir.setReturnValue(InteractionResult.FAIL);
            } else if (!mushroomCow.level().isClientSide) {
                duck.coraline$recordStewServing();
            }
        }
    }
}
