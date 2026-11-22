package net.zharok01.coralinesystems.item;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.zharok01.coralinesystems.entity.ObsidianBoatEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public class ObsidianBoatItem extends Item {

    private static final Predicate<Entity> ENTITY_PREDICATE = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);

    public ObsidianBoatItem() {
        super(new Item.Properties().fireResistant().stacksTo(1));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);

        if (hitResult.getType() == BlockHitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemStack);
        }

        Vec3 viewVector = player.getViewVector(1.0F);
        List<Entity> nearby = level.getEntities(player, player.getBoundingBox().expandTowards(viewVector.scale(5.0D)).inflate(1.0D), ENTITY_PREDICATE);
        Vec3 eyePos = player.getEyePosition();
        for (Entity entity : nearby) {
            if (entity.getBoundingBox().inflate(entity.getPickRadius()).contains(eyePos)) {
                return InteractionResultHolder.pass(itemStack);
            }
        }

        if (hitResult.getType() != BlockHitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(itemStack);
        }

        Vec3 location = hitResult.getLocation();
        ObsidianBoatEntity boat = new ObsidianBoatEntity(level, location.x, location.y, location.z);
        boat.yRotO = player.yRotO;
        boat.setYRot(player.getYRot());

        if (!level.noCollision(boat, boat.getBoundingBox().inflate(-0.1D))) {
            return InteractionResultHolder.fail(itemStack);
        }

        if (!level.isClientSide) {
            level.addFreshEntity(boat);
            level.gameEvent(player, GameEvent.ENTITY_PLACE, location);
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
}