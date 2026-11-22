package net.zharok01.coralinesystems.client.gui.compat.controlify;

import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorFactory;
import dev.isxander.controlify.virtualmouse.VirtualMouseBehaviour;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;

public final class CoralineControlifyCompat {

    public static InputBindingSupplier coraline$toggleRecipeBook;

    public static void coraline$register() {
        ScreenProcessorFactory.registerProvider(AdvancementsScreen.class, CoralineAdvancementsProcessor::new);
        MinecraftForge.EVENT_BUS.register(CoralineRecipeBookHints.class);

        if (ModList.get().isLoaded("picturemode")) {
            MinecraftForge.EVENT_BUS.register(CoralinePictureModeCompat.class);
        }
    }

    private static final class CoralineAdvancementsProcessor extends ScreenProcessor<AdvancementsScreen> {

        private CoralineAdvancementsProcessor(AdvancementsScreen screen) {
            super(screen);
        }

        @Override
        public VirtualMouseBehaviour virtualMouseBehaviour() {
            return VirtualMouseBehaviour.ENABLED;
        }
    }
}