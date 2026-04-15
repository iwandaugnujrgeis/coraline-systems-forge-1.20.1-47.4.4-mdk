package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.phys.AABB;
import net.zharok01.coralinesystems.content.entity.custom.HelperEntity;
import net.zharok01.coralinesystems.registry.CoralineTriggers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxBlockEntityMixin {

    @Final
    @Shadow private NonNullList<ItemStack> items;

    @Inject(method = "startPlaying", at = @At("TAIL"))
    private void coraline_startPlaying(CallbackInfo ci) {
        JukeboxBlockEntity jukebox = (JukeboxBlockEntity) (Object) this;
        Level level = jukebox.getLevel();

        if (level != null && !level.isClientSide) {
            ItemStack stack = this.items.get(0);
            if (stack.getItem() instanceof RecordItem record) {
                BlockPos pos = jukebox.getBlockPos();

                //Find Helpers within 16 blocks:
                List<HelperEntity> helpers = level.getEntitiesOfClass(
                        HelperEntity.class,
                        new AABB(pos).inflate(16.0D)
                );

                for (HelperEntity helper : helpers) {
                    helper.setDancingDuration(record.getLengthInTicks());
                }

                //Fire the Advancement once as long as at least one Helper started dancing!
                if (!helpers.isEmpty() && level instanceof ServerLevel serverLevel) {
                    serverLevel.getEntitiesOfClass(
                            net.minecraft.server.level.ServerPlayer.class,
                            new AABB(pos).inflate(16.0D)
                    ).stream().findFirst().ifPresent(CoralineTriggers.HELPER_DANCING::trigger);
                }
            }
        }
    }

    @Inject(method = "stopPlaying", at = @At("TAIL"))
    private void coraline_stopPlaying(CallbackInfo ci) {
        JukeboxBlockEntity jukebox = (JukeboxBlockEntity) (Object) this;
        Level level = jukebox.getLevel();

        if (level != null && !level.isClientSide) {
            BlockPos pos = jukebox.getBlockPos();

            List<HelperEntity> helpers = level.getEntitiesOfClass(
                    HelperEntity.class,
                    new AABB(pos).inflate(16.0D)
            );

            for (HelperEntity helper : helpers) {
                //If the Jukebox is broken or stops, stop dancing immediately!
                helper.setDancingDuration(0);
            }
        }
    }
}