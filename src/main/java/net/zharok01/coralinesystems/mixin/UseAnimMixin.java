package net.zharok01.coralinesystems.mixin;

import net.minecraft.world.item.UseAnim;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.ArrayList;
import java.util.Arrays;

@Mixin(UseAnim.class)
@Unique
public class UseAnimMixin {

    @Shadow @Final @Mutable
    private static UseAnim[] $VALUES;

    @Unique
    private static final UseAnim SWORD_BLOCK = coralinesystems$addAnim("SWORD_BLOCK");

    @Invoker("<init>")
    public static UseAnim coralinesystems$invokeInit(String name, int id) {
        throw new AssertionError();
    }

    @Unique
    private static UseAnim coralinesystems$addAnim(String animName) {
        assert UseAnimMixin.$VALUES != null;
        ArrayList<UseAnim> animations = new ArrayList<>(Arrays.asList(UseAnimMixin.$VALUES));
        UseAnim animation = coralinesystems$invokeInit(animName, animations.get(animations.size() - 1).ordinal() + 1);
        animations.add(animation);
        UseAnimMixin.$VALUES = animations.toArray(new UseAnim[0]);
        return animation;
    }
}