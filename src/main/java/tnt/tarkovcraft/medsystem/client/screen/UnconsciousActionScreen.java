package tnt.tarkovcraft.medsystem.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import tnt.tarkovcraft.core.client.screen.ColorPalette;
import tnt.tarkovcraft.core.client.screen.renderable.ShapeRenderable;
import tnt.tarkovcraft.core.client.screen.widget.ListWidget;
import tnt.tarkovcraft.core.common.data.duration.DurationUnit;
import tnt.tarkovcraft.core.util.UserActionResult;
import tnt.tarkovcraft.core.util.helper.TextHelper;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;
import tnt.tarkovcraft.medsystem.common.interaction.EntityInteraction;
import tnt.tarkovcraft.medsystem.common.interaction.EntityInteractionData;
import tnt.tarkovcraft.medsystem.common.interaction.EntityInteractionType;
import tnt.tarkovcraft.medsystem.network.message.C2S_RequestInteractionState;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

public class UnconsciousActionScreen extends Screen {

    private static final Component TITLE = TextHelper.createScreenTitle(MedSystemConstants.MOD_ID, "entity_interaction");

    private final LivingEntity entity;
    private EntityInteractionData interactionData;

    public UnconsciousActionScreen(LivingEntity entity) {
        super(TITLE);
        this.entity = entity;
    }

    @Override
    protected void init() {
        this.interactionData = this.minecraft.player.getData(MedSystemDataAttachments.INTERACTION_DATA);
        this.addRenderableOnly(new ShapeRenderable(0, 0, this.width, this.height, ColorPalette.BG_TRANSPARENT_WEAK));

        List<EntityInteractionType<?>> interactions = MedSystemRegistries.ENTITY_INTERACTION.stream().toList();
        int displayCount = Math.min(interactions.size(), 7);
        int displayHeight = displayCount * 20;
        int colWidth = this.width / 6;
        int left = (this.width - colWidth) / 2;
        int top = (this.height - displayHeight) / 2;
        this.addRenderableWidget(new ListWidget<>(left, top, colWidth, displayHeight, interactions, this::createInteractionButton));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);

        int titleWidth = this.font.width(TITLE);
        graphics.text(this.font, TITLE, (this.width - titleWidth) / 2, 15, ColorPalette.WHITE);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        if (this.entity.isRemoved() || !this.entity.isAlive()) {
            this.minecraft.gui.setScreen(null);
        }
        double distance = this.minecraft.player.distanceToSqr(this.entity);
        if (distance > EntityInteraction.MAX_DISTANCE_SQR) {
            this.minecraft.gui.setScreen(null);
        }
    }

    private InteractionButton createInteractionButton(EntityInteractionType<?> interactionType, int index) {
        int buttonWidth = this.width / 6;
        int left = (this.width - buttonWidth) / 2;
        EntityInteraction interaction = interactionType.createNewInteractionInstance(this.minecraft.player, this.entity);
        InteractionButton button = new InteractionButton(left, index * 20, buttonWidth, 20, interaction);
        button.setOnInitiate(this::onInteractionStarted);
        button.setOnCancel(this::onInteractCancelled);
        button.setOnFinish(this::interactionCompleteCallback);
        UserActionResult<Void> evaluationResult = interaction.checkAvailability(this.minecraft.player, this.entity);
        button.active = evaluationResult.isSuccess();
        if (evaluationResult.isFailure()) {
            Component formattedMessage = evaluationResult.message().plainCopy().withStyle(ChatFormatting.RED);
            button.setTooltip(Tooltip.create(formattedMessage));
            button.setTooltipDelay(Duration.ofMillis(300));
        }

        return button;
    }

    private void onInteractionStarted(EntityInteraction interaction) {
        long initiationTime = this.minecraft.level.getGameTime();
        this.interactionData.startInteraction(interaction.type(), this.minecraft.player, this.entity, initiationTime);
        ClientPacketDistributor.sendToServer(C2S_RequestInteractionState.start(interaction.type(), this.entity, initiationTime));
    }

    private void onInteractCancelled(EntityInteraction interaction) {
        this.interactionData.cancelInteraction(this.minecraft.player, this.entity);
        ClientPacketDistributor.sendToServer(C2S_RequestInteractionState.cancel(interaction.type(), this.entity));
    }

    private void interactionCompleteCallback(EntityInteraction interaction) {
        this.interactionData.finishInteraction(this.minecraft.player, this.entity);
        ClientPacketDistributor.sendToServer(C2S_RequestInteractionState.finish(interaction.type(), this.entity));
        this.minecraft.gui.setScreen(null);
    }

    @Override
    public void removed() {
        if (this.interactionData.isAnyInteractionActive()) {
            EntityInteraction interaction = this.interactionData.getActiveInteraction();
            this.interactionData.cancelInteraction(this.minecraft.player, this.entity);
            ClientPacketDistributor.sendToServer(C2S_RequestInteractionState.cancel(interaction.type(), this.entity));
        }
    }

    private static final class InteractionButton extends AbstractButton {

        private final EntityInteraction interaction;

        private InteractStateCallback onInitiate = _ -> {};
        private InteractStateCallback onCancel = _ -> {};
        private InteractStateCallback onFinish = _ -> {};
        private long pressStartTs = -1;

        public InteractionButton(int x, int y, int width, int height, EntityInteraction interaction) {
            super(x, y, width, height, interaction.getDisplayName());
            this.interaction = interaction;
        }

        public void setOnInitiate(InteractStateCallback onInitiate) {
            this.onInitiate = onInitiate;
        }

        public void setOnCancel(InteractStateCallback onCancel) {
            this.onCancel = onCancel;
        }

        public void setOnFinish(InteractStateCallback onFinish) {
            this.onFinish = onFinish;
        }

        @Override
        public void onPress(InputWithModifiers input) {
            if (this.pressStartTs > 0) {
                this.pressStartTs = -1;
                this.onCancel.onStateChangedCallback(this.interaction);
                return;
            }
            if (this.interaction.getInteractionDuration() <= 0) {
                this.onFinish.onStateChangedCallback(this.interaction);
                return;
            }
            this.pressStartTs = System.currentTimeMillis();
            this.onInitiate.onStateChangedCallback(this.interaction);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            this.extractDefaultSprite(graphics);
            Font font = Minecraft.getInstance().font;
            Component content = this.getDisplayContent();
            int contentWidth = font.width(content);
            graphics.text(font, content, this.getX() + (this.width - contentWidth) / 2, this.getY() + (this.height - 8) / 2, this.active ? 0xFFFFFFFF : 0xFFAAAAAA);

            if (this.isPressed() && this.isFinished()) {
                this.onFinish.onStateChangedCallback(this.interaction);
                this.pressStartTs = -1;
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
        }

        private Component getDisplayContent() {
            if (this.isPressed()) {
                if (this.isHovered) {
                    return CommonComponents.GUI_CANCEL;
                }
                int durationMs = this.interaction.getInteractionDuration() * 50;
                long elapsed = System.currentTimeMillis() - this.pressStartTs;
                long remaining = durationMs - elapsed;
                int remainingTicks = (int) (remaining / 50L);
                double seconds = remainingTicks / 20.0D;
                return Component.literal(String.format(Locale.ROOT, "%.1f", seconds)).append(DurationUnit.SECONDS.getShortName());
            }
            return this.getMessage();
        }

        private boolean isPressed() {
            return this.pressStartTs > 0;
        }

        private boolean isFinished() {
            return this.pressStartTs + this.interaction.getInteractionDuration() * 50L < System.currentTimeMillis();
        }

        @FunctionalInterface
        public interface InteractStateCallback {
            void onStateChangedCallback(EntityInteraction interaction);
        }
    }
}
