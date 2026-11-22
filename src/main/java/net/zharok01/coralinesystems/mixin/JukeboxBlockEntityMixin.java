package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.phys.AABB;
import net.zharok01.coralinesystems.entity.GiantJammingAccess;
import net.zharok01.coralinesystems.entity.HelperEntity;
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

        if (level == null || level.isClientSide) return;

        ItemStack stack = this.items.get(0);
        if (!(stack.getItem() instanceof RecordItem record)) return;

        BlockPos pos = jukebox.getBlockPos();
        AABB range = new AABB(pos).inflate(64.0D);

        List<HelperEntity> helpers = level.getEntitiesOfClass(HelperEntity.class, range);
        for (HelperEntity helper : helpers) {
            helper.setDancingDuration(record.getLengthInTicks());
        }

        for (Giant giant : level.getEntitiesOfClass(Giant.class, range)) {
            ((GiantJammingAccess) giant).coraline$setDancingDuration(record.getLengthInTicks());
        }

        if (!helpers.isEmpty()) {
            List<ServerPlayer> players = ((ServerLevel) level).getEntitiesOfClass(ServerPlayer.class, range);
            if (!players.isEmpty()) {
                CoralineTriggers.HELPER_DANCING.trigger(players.get(0));
            }
        }
    }

    @Inject(method = "stopPlaying", at = @At("TAIL"))
    private void coraline_stopPlaying(CallbackInfo ci) {
        JukeboxBlockEntity jukebox = (JukeboxBlockEntity) (Object) this;
        Level level = jukebox.getLevel();

        if (level == null || level.isClientSide) return;

        BlockPos pos = jukebox.getBlockPos();
        for (HelperEntity helper : level.getEntitiesOfClass(HelperEntity.class, new AABB(pos).inflate(64.0D))) {
            helper.setDancingDuration(0);
        }

        for (Giant giant : level.getEntitiesOfClass(Giant.class, new AABB(pos).inflate(64.0D))) {
            ((GiantJammingAccess) giant).coraline$setDancingDuration(0);
        }
    }
}