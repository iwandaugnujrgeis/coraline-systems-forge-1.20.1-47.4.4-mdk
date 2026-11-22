package net.zharok01.coralinesystems.mixin.client.gui.screens;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.zharok01.coralinesystems.util.CoralineWorldInfo;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen {

    @Shadow @Final WorldCreationUiState uiState;
    @Shadow @Nullable private GridLayout bottomButtons;

    @Shadow protected abstract void onCreate();
    @Shadow public abstract void popScreen();

    @Unique private EditBox coraline$nameEdit;
    @Unique private EditBox coraline$seedEdit;

    protected CreateWorldScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void coraline$flattenScreen(CallbackInfo ci) {
        ci.cancel();

        this.uiState.setAllowCheats(true);

        this.coraline$nameEdit = new EditBox(this.font, 0, 0, 208, 20, Component.translatable("selectWorld.enterName"));
        this.coraline$nameEdit.setValue(this.uiState.getName());
        this.coraline$nameEdit.setResponder(this.uiState::setName);
        this.uiState.addListener(state -> this.coraline$nameEdit.setTooltip(Tooltip.create(Component.translatable("selectWorld.targetFolder", Component.literal(state.getTargetFolder()).withStyle(ChatFormatting.ITALIC)))));
        this.setInitialFocus(this.coraline$nameEdit);
        this.addRenderableWidget(this.coraline$nameEdit);

        CycleButton<WorldCreationUiState.SelectedGameMode> gameModeButton = CycleButton.builder((WorldCreationUiState.SelectedGameMode mode) -> mode.displayName)
                .withValues(WorldCreationUiState.SelectedGameMode.SURVIVAL, WorldCreationUiState.SelectedGameMode.HARDCORE, WorldCreationUiState.SelectedGameMode.CREATIVE)
                .create(0, 0, 210, 20, Component.translatable("selectWorld.gameMode"), (button, mode) -> this.uiState.setGameMode(mode));
        gameModeButton.setValue(this.uiState.getGameMode());
        this.uiState.addListener(state -> {
            gameModeButton.setValue(state.getGameMode());
            gameModeButton.active = !state.isDebug();
            gameModeButton.setTooltip(Tooltip.create(state.getGameMode().getInfo()));
        });
        this.addRenderableWidget(gameModeButton);

        CycleButton<Difficulty> difficultyButton = CycleButton.builder(Difficulty::getDisplayName)
                .withValues(Difficulty.values())
                .create(0, 0, 210, 20, Component.translatable("options.difficulty"), (button, difficulty) -> this.uiState.setDifficulty(difficulty));
        difficultyButton.setValue(this.uiState.getDifficulty());
        this.uiState.addListener(state -> {
            difficultyButton.setValue(this.uiState.getDifficulty());
            difficultyButton.active = !this.uiState.isHardcore();
            difficultyButton.setTooltip(Tooltip.create(this.uiState.getDifficulty().getInfo()));
        });
        this.addRenderableWidget(difficultyButton);

        this.coraline$seedEdit = new EditBox(this.font, 0, 0, 308, 20, Component.translatable("selectWorld.enterSeed"));
        this.coraline$seedEdit.setHint(Component.translatable("selectWorld.seedInfo").withStyle(ChatFormatting.DARK_GRAY));
        this.coraline$seedEdit.setValue(this.uiState.getSeed());
        this.coraline$seedEdit.setResponder(this.uiState::setSeed);
        this.addRenderableWidget(this.coraline$seedEdit);

        CycleButton.ValueListSupplier<WorldCreationUiState.WorldTypeEntry> valueSupplier = new CycleButton.ValueListSupplier<>() {
            @Override
            public @NotNull List<WorldCreationUiState.WorldTypeEntry> getSelectedList() {
                return CycleButton.DEFAULT_ALT_LIST_SELECTOR.getAsBoolean() ? CreateWorldScreenMixin.this.uiState.getAltPresetList() : CreateWorldScreenMixin.this.uiState.getNormalPresetList();
            }

            @Override
            public @NotNull List<WorldCreationUiState.WorldTypeEntry> getDefaultList() {
                return CreateWorldScreenMixin.this.uiState.getNormalPresetList();
            }
        };

        CycleButton<WorldCreationUiState.WorldTypeEntry> worldTypeButton = CycleButton.builder(WorldCreationUiState.WorldTypeEntry::describePreset)
                .withValues(valueSupplier)
                .create(0, 0, 150, 20, Component.translatable("selectWorld.mapType"), (button, entry) -> this.uiState.setWorldType(entry));
        worldTypeButton.setValue(this.uiState.getWorldType());
        this.uiState.addListener(state -> {
            WorldCreationUiState.WorldTypeEntry entry = state.getWorldType();
            worldTypeButton.setValue(entry);
            if (entry.isAmplified()) {
                worldTypeButton.setTooltip(Tooltip.create(Component.translatable("generator.minecraft.amplified.info")));
            } else {
                worldTypeButton.setTooltip(null);
            }
            worldTypeButton.active = this.uiState.getWorldType().preset() != null;
        });
        this.addRenderableWidget(worldTypeButton);

        Button customizeTypeButton = Button.builder(Component.translatable("selectWorld.customizeType"), button -> {
            PresetEditor presetEditor = this.uiState.getPresetEditor();
            if (presetEditor != null && this.minecraft != null) {
                this.minecraft.setScreen(presetEditor.createEditScreen((CreateWorldScreen)(Object)this, this.uiState.getSettings()));
            }
        }).build();
        this.uiState.addListener(state -> customizeTypeButton.active = !state.isDebug() && state.getPresetEditor() != null);
        this.addRenderableWidget(customizeTypeButton);

        this.bottomButtons = new GridLayout().columnSpacing(10);
        GridLayout.RowHelper rowHelper = this.bottomButtons.createRowHelper(2);
        rowHelper.addChild(Button.builder(Component.translatable("selectWorld.create"), button -> this.onCreate()).build());
        rowHelper.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> this.popScreen()).build());
        this.bottomButtons.visitWidgets(widget -> {
            widget.setTabOrderGroup(1);
            this.addRenderableWidget(widget);
        });

        this.uiState.onChanged();
        this.repositionElements();
    }

    @Inject(method = "repositionElements", at = @At("HEAD"), cancellable = true)
    private void coraline$onRepositionElements(CallbackInfo ci) {
        ci.cancel();
        if (this.bottomButtons != null) {
            this.bottomButtons.arrangeElements();
            FrameLayout.centerInRectangle(this.bottomButtons, 0, this.height - 36, this.width, 36);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void coraline$onTick(CallbackInfo ci) {
        ci.cancel();
        if (this.coraline$nameEdit != null) {
            this.coraline$nameEdit.tick();
        }
        if (this.coraline$seedEdit != null) {
            this.coraline$seedEdit.tick();
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void coraline$onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            cir.setReturnValue(true);
        } else if (keyCode == 257 || keyCode == 335) {
            this.onCreate();
            cir.setReturnValue(true);
        } else {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "onCreate", at = @At("HEAD"))
    private void coraline$rememberWorldType(CallbackInfo ci) {
        Holder<WorldPreset> preset = this.uiState.getWorldType().preset();
        CoralineWorldInfo.coraline$pendingWorldType = preset == null ? null : preset.unwrapKey().map(ResourceKey::location).orElse(null);
    }
}