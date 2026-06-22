package net.zharok01.coralinesystems.util;

import net.minecraft.world.item.UseAnim;

public class AnimationTypes {
    // Relies on our Mixin to inject the value into the UseAnim Enum before this is called.
    public static final UseAnim SWORD_BLOCK = UseAnim.valueOf("SWORD_BLOCK");
}