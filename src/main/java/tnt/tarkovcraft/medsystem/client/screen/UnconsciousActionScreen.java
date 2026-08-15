package tnt.tarkovcraft.medsystem.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.client.screen.ColorPalette;
import tnt.tarkovcraft.core.client.screen.renderable.ShapeRenderable;
import tnt.tarkovcraft.core.client.screen.widget.ListWidget;
import tnt.tarkovcraft.core.common.data.duration.DurationUnit;
import tnt.tarkovcraft.core.util.UserActionResult;
import tnt.tarkovcraft.core.util.helper.TextHelper;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.common.interaction.EntityInteraction;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class UnconsciousActionScreen extends Screen {

    private static final Component TITLE = TextHelper.createScreenTitle(MedSystemConstants.MOD_ID, "entity_interaction");

    private final LivingEntity entity;

    public UnconsciousActionScreen(LivingEntity entity) {
        super(TITLE);
        this.entity = entity;
    }

    @Override
    protected void init() {
        this.addRenderableOnly(new ShapeRenderable(0, 0, this.width, this.height, ColorPalette.BG_TRANSPARENT_WEAK));

        List<EntityInteraction> interactions = EntityInteractions.getInteractions();
        int displayCount = Math.min(interactions.size(), 7);
        int displayHeight = displayCount * 20;
        int colWidth = this.width / 6;
        int left = (this.width - colWidth) / 2;
        int top = (this.height - displayHeight) / 2;
        this.addRenderableWidget(new ListWidget<>(left, top, colWidth, displayHeight, interactions, this::createInteractionButton));
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float a) {
        super.renderBackground(graphics, mouseX, mouseY, a);

        int titleWidth = this.font.width(TITLE);
        graphics.drawString(this.font, TITLE, (this.width - titleWidth) / 2, 15, ColorPalette.WHITE);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        if (this.entity.isRemoved() || !this.entity.isAlive()) {
            this.minecraft.setScreen(null);
        }
        double distance = this.minecraft.player.distanceToSqr(this.entity);
        if (distance > EntityInteraction.MAX_DISTANCE_SQR) {
            this.minecraft.setScreen(null);
        }
    }

    private InteractionButton createInteractionButton(EntityInteraction interaction, int index) {
        int buttonWidth = this.width / 6;
        int left = (this.width - buttonWidth) / 2;
        InteractionButton button = new InteractionButton(left, index * 20, buttonWidth, 20, interaction, this::interactionCompleteCallback);
        UserActionResult<Void> evaluationResult = EntityInteractions.evaluateInteraction(this.minecraft.player, this.entity, interaction);
        button.active = evaluationResult.isSuccess();
        if (evaluationResult.isFailure()) {
            Component formattedMessage = evaluationResult.message().plainCopy().withStyle(ChatFormatting.RED);
            button.setTooltip(Tooltip.create(formattedMessage));
            button.setTooltipDelay(Duration.ofMillis(300));
        }

        return button;
    }

    private void interactionCompleteCallback(EntityInteraction interaction) {
        interaction.onActionPerformed(this.minecraft.player, this.entity);
        this.minecraft.setScreen(null);
    }

    private static final class InteractionButton extends AbstractButton {

        private final EntityInteraction interaction;
        private final Consumer<EntityInteraction> callback;
        private long pressStartTs = -1;

        public InteractionButton(int x, int y, int width, int height, EntityInteraction interaction, Consumer<EntityInteraction> callback) {
            super(x, y, width, height, interaction.actionName());
            this.interaction = interaction;
            this.callback = callback;
        }

        @Override
        public void onPress() {
            if (this.pressStartTs > 0) {
                this.pressStartTs = -1;
                return;
            }
            if (this.interaction.actionDuration() <= 0) {
                this.callback.accept(this.interaction);
                return;
            }
            this.pressStartTs = System.currentTimeMillis();
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float a) {
            graphics.blitSprite(SPRITES.get(this.active, this.isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight());
            Font font = Minecraft.getInstance().font;
            Component content = this.getDisplayContent();
            int contentWidth = font.width(content);
            graphics.drawString(font, content, this.getX() + (this.width - contentWidth) / 2, this.getY() + (this.height - 8) / 2, this.active ? 0xFFFFFFFF : 0xFFAAAAAA);

            if (this.isPressed() && this.isFinished()) {
                this.callback.accept(this.interaction);
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
                int durationMs = this.interaction.actionDuration() * 50;
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
            return this.pressStartTs + this.interaction.actionDuration() * 50L < System.currentTimeMillis();
        }
    }
}
