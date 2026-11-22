package net.zharok01.coralinesystems.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.entity.util.damage.CuttingDamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class StonecutterDamageMixin {

    @SuppressWarnings("removal")
    @Unique private static final ResourceKey<DamageType> coraline$cutting = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation("coraline_systems", "cutting")
    );

    @SuppressWarnings("removal")
    @Unique private static final TagKey<Block> coraline$dangerousCutterTag = TagKey.create(
            Registries.BLOCK,
            new ResourceLocation("coraline_systems", "dangerous_cutter")
    );

    @Unique private static final float CUTTER_DAMAGE = 3.0f;

    @Shadow
    protected abstract BlockState asState();

    @Inject(method = "entityInside", at = @At("HEAD"))
    private void coraline$onEntityInside(Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        BlockState state = this.asState();
        if (!state.is(coraline$dangerousCutterTag)) return;

        var shape = state.getCollisionShape(level, pos);

        double enMaxX = entity.getBoundingBox().maxX;
        double enMinX = entity.getBoundingBox().minX;
        double enMaxZ = entity.getBoundingBox().maxZ;
        double enMinZ = entity.getBoundingBox().minZ;

        double shMaxX = shape.max(Direction.Axis.X) + pos.getX();
        double shMinX = shape.min(Direction.Axis.X) + pos.getX();
        double shMaxZ = shape.max(Direction.Axis.Z) + pos.getZ();
        double shMinZ = shape.min(Direction.Axis.Z) + pos.getZ();

        boolean overlapX = (enMaxX > shMinX && enMaxX < shMaxX) || (enMinX > shMinX && enMinX < shMaxX)
                || (shMaxX > enMinX && shMaxX < enMaxX) || (shMinX > enMinX && shMinX < enMaxX);
        boolean overlapZ = (enMaxZ > shMinZ && enMaxZ < shMaxZ) || (enMinZ > shMinZ && enMinZ < shMaxZ)
                || (shMaxZ > enMinZ && shMaxZ < enMaxZ) || (shMinZ > enMinZ && shMinZ < enMaxZ);

        if (!overlapX || !overlapZ) return;

        if (entity.position().y() < shape.max(Direction.Axis.Y) + pos.getY()) return;

        entity.makeStuckInBlock(state, new Vec3(0.8, 0.75, 0.8));

        if (level.isClientSide()) return;
        if (entity instanceof ItemEntity) return;

        var damageTypeRegistry = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        var damageTypeHolder = damageTypeRegistry.getHolderOrThrow(coraline$cutting);
        var damageSource = new CuttingDamageSource(damageTypeHolder);

        boolean isInvulnerable = entity.isInvulnerableTo(damageSource)
                || (entity instanceof Player player && player.getAbilities().invulnerable);

        entity.hurt(damageSource, CUTTER_DAMAGE);

        if (!isInvulnerable) {
            level.playSound(null, pos, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS, 0.3f, 1f);
        }
    }
}