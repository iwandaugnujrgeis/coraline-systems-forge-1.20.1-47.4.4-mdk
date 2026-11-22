package net.zharok01.coralinesystems.quiver;

import com.legacy.rediscovered.data.RediscoveredTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class QuiverAmmoSlot extends Slot {
    private final QuiverContainer coraline$quiverContainer;
    private final Player coraline$owner;

    public QuiverAmmoSlot(QuiverContainer container, int slot, int x, int y, Player owner) {
        super(container, slot, x, y);
        this.coraline$quiverContainer = container;
        this.coraline$owner = owner;
    }

    @Override
    public boolean isActive() {
        return QuiverContents.isWornBy(this.coraline$owner);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.coraline$quiverContainer.isBoundToWornQuiver() && stack.is(RediscoveredTags.Items.QUIVER_AMMO);
    }

    @Override
    public boolean mayPickup(Player player) {
        return this.coraline$quiverContainer.isBoundToWornQuiver();
    }
}