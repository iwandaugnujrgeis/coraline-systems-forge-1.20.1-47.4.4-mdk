package net.zharok01.coralinesystems.registry;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.zharok01.coralinesystems.client.gui.NameTagScreen;

@OnlyIn(Dist.CLIENT)
public final class CoralineClientHooks {

    private CoralineClientHooks() {
    }

    public static void openNameTagScreen(ItemStack itemStack, InteractionHand hand) {
        Minecraft.getInstance().setScreen(new NameTagScreen(itemStack, hand));
    }
}