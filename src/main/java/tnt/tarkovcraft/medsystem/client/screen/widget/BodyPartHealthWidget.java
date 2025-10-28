package tnt.tarkovcraft.medsystem.client.screen.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import tnt.tarkovcraft.core.client.screen.ColorPalette;
import tnt.tarkovcraft.core.client.screen.listener.SimpleClickListener;
import tnt.tarkovcraft.core.util.helper.MathHelper;
import tnt.tarkovcraft.core.util.helper.RenderUtils;
import tnt.tarkovcraft.medsystem.client.MedicalSystemClient;
import tnt.tarkovcraft.medsystem.client.config.HealthOverlayConfiguration;
import tnt.tarkovcraft.medsystem.client.overlay.HealthLayer;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.EffectVisibility;
import tnt.tarkovcraft.medsystem.common.health.Limb;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BodyPartHealthWidget extends AbstractWidget {

    private final Font font;
    private final Limb part;

    private int frameSize = 1;
    private int frameColor = ColorPalette.WHITE;
    private int frameHoverColor = ColorPalette.YELLOW;
    private int backgroundColor = 0xFF << 24;
    private int textColor = ColorPalette.WHITE;
    private int textHoverColor = ColorPalette.YELLOW;
    private float healthScale = 1.0F;
    private int effectScale = 12;
    private SimpleClickListener onClick;
    private List<StatusEffect> effects;
    private boolean effectDetail = true;

    public BodyPartHealthWidget(int x, int y, int width, int height, Font font, Limb part) {
        super(x, y, width, height, CommonComponents.EMPTY);
        this.font = font;
        this.part = part;
    }

    public void setEffects(List<StatusEffect> effects) {
        this.effects = effects;
    }

    public void setEffectIconSize(int effectIconSize) {
        this.effectScale = effectIconSize;
    }

    public void setHealthUnitScale(float healthScale) {
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

    public void setEffectDetail(boolean effectDetail) {
        this.effectDetail = effectDetail;
    }

    @Override
    protected boolean isValidClickButton(MouseButtonInfo info) {
        return this.onClick != null;
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        this.onClick.onClick();
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Frame
        if (this.frameSize > 0 && RenderUtils.isVisibleColor(this.frameColor)) {
            int frameColor = this.isHovered ? this.frameHoverColor : this.frameColor;
            graphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), frameColor);
        }
        // Background fill
        if (RenderUtils.isVisibleColor(this.backgroundColor)) {
            graphics.fill(this.getX() + this.frameSize, this.getY() + this.frameSize, this.getRight() - this.frameSize, this.getBottom() - this.frameSize, this.backgroundColor);
        }
        // Health status %{currHealth}/${maxHealth} or hovered shows part name
        Component status = this.getStatusTitle();
        int statusWidth = this.font.width(status);
        int textColor = this.part.isDead() ? 0xFFFF0000 : this.isHovered ? this.textHoverColor : this.textColor;
        graphics.drawString(this.font, status, this.getX() + (this.width - statusWidth) / 2, this.getY() + 3 + this.frameSize, textColor);
        // Health bar setup
        HealthOverlayConfiguration overlay = MedicalSystemClient.getConfig().healthOverlay;
        int background = Integer.decode(overlay.deadLimbColor) | 0xFF << 24;
        int secondaryBackground = ARGB.scaleRGB(background, 0.8F);
        int color = HealthLayer.getColor(overlay.deadLimbColor, overlay.colorSchema, this.part) | 0xFF << 24;
        int secondaryColor = ARGB.scaleRGB(color, 0.8F);
        float f = this.part.getHealthPercent();
        // Health bar background
        graphics.fillGradient(this.getX() + this.frameSize + 1, this.getY() + this.frameSize + 13, this.getRight() - this.frameSize - 1, this.getY() + this.frameSize + 17, background, secondaryBackground);
        // Health bar foreground - current health
        int left = this.getX() + this.frameSize + 2;
        int right = this.getRight() - this.frameSize - 2;
        graphics.fillGradient(left, this.getY() + this.frameSize + 14, left + (int) ((right - left) * f), this.getY() + this.frameSize + 16, color, secondaryColor);
        // Status effects - we need 14px for render (12scale+2border) + 17 for health bar offset + 2xFrameSize
        if (this.height >= (19 + this.effectScale + this.frameSize * 2) && this.effects != null && !this.effects.isEmpty()) {
            List<StatusEffect> visibleEffects = this.effects.stream().filter(effect -> StatusEffectType.isVisible(effect, EffectVisibility.UI)).toList();
            if (visibleEffects.isEmpty())
                return;
            int bounds = this.width - this.frameSize - 2;
            int maxEffects = Math.min(visibleEffects.size(), bounds / this.effectScale);
            for (int i = 0; i < maxEffects; i++) {
                StatusEffect effect = visibleEffects.get(i);
                StatusEffectType<?> type = effect.getType();
                int effectX = this.getX() + this.frameSize + 1 + i * this.effectScale;
                int effectY = this.getBottom() - this.frameSize - 1 - this.effectScale;
                // effect icon
                RenderUtils.blitFull(graphics, type.getIcon(effect), effectX, effectY, effectX + this.effectScale, effectY + this.effectScale);
                // hover effects
                if (this.effectDetail && MathHelper.isWithinBounds(mouseX, mouseY, effectX, effectY, this.effectScale, this.effectScale)) {
                    List<Component> tooltip = new ArrayList<>();
                    tooltip.add(type.getDisplayName(effect).copy().withStyle(type.getEffectType()));
                    effect.addAdditionalInfo(tooltip::add);
                    if (effect.hasVisibleDuration() && !effect.isInfinite()) {
                        tooltip.add(StatusEffect.getDurationLabel(effect.getDuration()));
                    }
                    graphics.setTooltipForNextFrame(this.font, tooltip, Optional.empty(), mouseX, mouseY);
                }
            }
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    private Component getStatusTitle() {
        return this.isHovered
                ? this.part.getDisplayName()
                : Component.literal(Mth.ceil(this.part.getHealth() * this.healthScale) + "/" + Mth.ceil(this.part.getMaxHealth() * this.healthScale));
    }
}
