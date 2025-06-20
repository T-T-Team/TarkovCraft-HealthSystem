package tnt.tarkovcraft.medsystem.client.screen.widget;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import tnt.tarkovcraft.core.client.screen.ColorPalette;
import tnt.tarkovcraft.core.client.screen.SharedScreenState;
import tnt.tarkovcraft.core.client.screen.listener.SimpleClickListener;
import tnt.tarkovcraft.core.common.data.duration.Duration;
import tnt.tarkovcraft.core.common.data.duration.DurationFormatSettings;
import tnt.tarkovcraft.core.common.data.duration.DurationFormats;
import tnt.tarkovcraft.core.common.data.duration.DurationUnit;
import tnt.tarkovcraft.core.util.helper.MathHelper;
import tnt.tarkovcraft.core.util.helper.RenderUtils;
import tnt.tarkovcraft.medsystem.client.MedicalSystemClient;
import tnt.tarkovcraft.medsystem.client.config.HealthOverlayConfiguration;
import tnt.tarkovcraft.medsystem.client.overlay.HealthLayer;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class BodyPartHealthWidget extends AbstractWidget {

    private final Font font;
    private final BodyPart part;

    private int frameSize = 1;
    private int frameColor = ColorPalette.WHITE;
    private int frameHoverColor = ColorPalette.YELLOW;
    private int backgroundColor = 0xFF << 24;
    private int textColor = ColorPalette.WHITE;
    private int textHoverColor = ColorPalette.YELLOW;
    private float healthScale = 1.0F;
    private SimpleClickListener onClick;
    private List<StatusEffect> effects;
    private SharedScreenState<BodyPart> hoverState;

    public BodyPartHealthWidget(int x, int y, int width, int height, Font font, BodyPart part) {
        super(x, y, width, height, part.getDisplayName().copy().withStyle(ChatFormatting.BOLD));
        this.font = font;
        this.part = part;
    }

    public void setHoverState(SharedScreenState<BodyPart> hoverState) {
        this.hoverState = hoverState;
    }

    public void setEffects(List<StatusEffect> effects) {
        this.effects = effects;
    }

    public void setHealthScale(float healthScale) {
        this.healthScale = healthScale;
    }

    public void setClickListener(SimpleClickListener onClick) {
        this.onClick = onClick;
    }

    public void setFrameSize(int frameSize) {
        this.frameSize = frameSize;
    }

    public void setFrameColor(int frameColor) {
        this.frameColor = frameColor;
    }

    public void setFrameHoverColor(int frameHoverColor) {
        this.frameHoverColor = frameHoverColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public void setTextHoverColor(int textHoverColor) {
        this.textHoverColor = textHoverColor;
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return this.onClick != null;
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        this.onClick.onClick();
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        boolean partHovered = false;
        if (this.hoverState != null) {
            if (this.isHovered) {
                this.hoverState.setState(this, part);
            } else {
                this.hoverState.clearState(this);
            }
            partHovered = this.hoverState.getState() != null && this.hoverState.getState().getName().equals(this.part.getName());
        }
        if (this.frameSize > 0 && RenderUtils.isVisibleColor(this.frameColor)) {
            int frameColor = partHovered || this.isHovered ? this.frameHoverColor : this.frameColor;
            graphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), frameColor);
        }
        if (RenderUtils.isVisibleColor(this.backgroundColor)) {
            graphics.fill(this.getX() + this.frameSize, this.getY() + this.frameSize, this.getRight() - this.frameSize, this.getBottom() - this.frameSize, this.backgroundColor);
        }
        int textColor = this.part.isDead() ? 0xFFFF0000 : partHovered ? this.textHoverColor : this.textColor;
        int titleWidth = this.font.width(this.getMessage());
        graphics.drawString(this.font, this.getMessage(), this.getX() + (this.width - titleWidth) / 2, this.getY() + 5 + this.frameSize, textColor);
        String status = Mth.ceil(this.part.getHealth() * this.healthScale) + "/" + Mth.ceil(this.part.getMaxHealth() * this.healthScale);
        int statusWidth = this.font.width(status);
        graphics.drawString(this.font, status, this.getX() + (this.width - statusWidth) / 2, this.getBottom() - 14 - this.frameSize, textColor);
        HealthOverlayConfiguration overlay = MedicalSystemClient.getConfig().healthOverlay;
        int background = Integer.decode(overlay.deadLimbColor) | 0xFF << 24;
        int secondaryBackground = ARGB.scaleRGB(background, 0.8F);
        int color = HealthLayer.getColor(overlay.deadLimbColor, overlay.colorSchema, this.part) | 0xFF << 24;
        int secondaryColor = ARGB.scaleRGB(color, 0.8F);
        float f = this.part.getHealthPercent();
        graphics.fillGradient(this.getX() + this.frameSize + 1, this.getBottom() - this.frameSize - 5, this.getRight() - this.frameSize - 1, this.getBottom() - this.frameSize - 1, background, secondaryBackground);
        int left = this.getX() + this.frameSize + 2;
        int right = this.getRight() - this.frameSize - 2;
        graphics.fillGradient(left, this.getBottom() - this.frameSize - 4, left + (int) ((right - left) * f), this.getBottom() - this.frameSize - 2, color, secondaryColor);

        if (this.effects != null && !this.effects.isEmpty()) {
            for (int i = 0; i < this.effects.size(); i++) {
                int row = i % 3;
                int col = i / 3;
                StatusEffect effect = this.effects.get(i);
                StatusEffectType<?> type = effect.getType();
                int ex = this.getRight() + col * 12;
                int ey = this.getY() + row * 12;
                RenderUtils.blitFull(graphics, type.getIcon(), ex, ey, ex + 12, ey + 12, -1);
                if (MathHelper.isWithinBounds(mouseX, mouseY, ex, ey, 12, 12)) {
                    List<Component> tooltip = new ArrayList<>();
                    tooltip.add(type.getDisplayName().copy().withStyle(type.getEffectType()));
                    effect.addAdditionalInfo(tooltip::add);
                    if (!effect.isInfinite()) {
                        DurationFormatSettings settings = new DurationFormatSettings();
                        settings.setIncludeZeroValues(true);
                        settings.setUnits(Arrays.asList(DurationUnit.HOURS, DurationUnit.MINUTES, DurationUnit.SECONDS));
                        tooltip.add(Duration.format(effect.getDuration(), settings, DurationFormats.TIME).copy().withStyle(ChatFormatting.DARK_GRAY));
                    }
                    graphics.setTooltipForNextFrame(this.font, tooltip, Optional.empty(), mouseX, mouseY);
                }
            }
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
