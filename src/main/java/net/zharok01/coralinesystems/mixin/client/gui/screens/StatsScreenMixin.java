package net.zharok01.coralinesystems.mixin.client.gui.screens;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.StatsCounter;
import net.zharok01.coralinesystems.client.gui.CoralineScreenLayout;
import net.zharok01.coralinesystems.client.gui.CoralineGeneralStatsList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StatsScreen.class)
public abstract class StatsScreenMixin extends Screen {

    @Unique private static final int COMPACT_DONE_WIDTH = 140;

    @Shadow @Final StatsCounter stats;

    @Unique private CoralineGeneralStatsList coraline$generalList;

    protected StatsScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "initLists", at = @At("TAIL"))
    private void coraline$createGeneralList(CallbackInfo ci) {
        this.coraline$generalList = new CoralineGeneralStatsList(this.minecraft, this, this.stats);
    }

    @ModifyVariable(method = "setActiveList", at = @At("HEAD"), argsOnly = true)
    private ObjectSelectionList<?> coraline$useGeneralList(ObjectSelectionList<?> activeList) {
        if (activeList != null && activeList.getClass().getName().endsWith("StatsScreen$GeneralStatisticsList")) {
            return this.coraline$generalList;
        }
        return activeList;
    }

    @Inject(method = "initButtons", at = @At("TAIL"))
    private void coraline$alignFooterButtons(CallbackInfo ci) {
        int rowLeft = this.width / 2 - CoralineScreenLayout.FOOTER_ROW_WIDTH / 2;
        int tabWidth = (CoralineScreenLayout.FOOTER_ROW_WIDTH - 2 * CoralineScreenLayout.FOOTER_BUTTON_GAP) / 3;

        for (int tabIndex = 0; tabIndex < 3; tabIndex++) {
            Button tab = (Button) this.children().get(tabIndex);
            tab.setX(rowLeft + tabIndex * (tabWidth + CoralineScreenLayout.FOOTER_BUTTON_GAP));
            tab.setY(this.height - CoralineScreenLayout.FOOTER_UPPER_ROW_OFFSET);
            tab.setWidth(tabWidth);
        }

        Button doneButton = (Button) this.children().get(3);
        doneButton.setX(this.width / 2 - COMPACT_DONE_WIDTH / 2);
        doneButton.setY(this.height - CoralineScreenLayout.FOOTER_LOWER_ROW_OFFSET);
        doneButton.setWidth(COMPACT_DONE_WIDTH);
    }
}