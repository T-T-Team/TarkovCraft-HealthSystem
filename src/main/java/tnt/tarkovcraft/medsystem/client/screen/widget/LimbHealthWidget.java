package tnt.tarkovcraft.medsystem.client.screen.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import tnt.tarkovcraft.core.client.screen.ColorPalette;
import tnt.tarkovcraft.core.client.screen.listener.SimpleClickListener;
import tnt.tarkovcraft.core.util.helper.MathHelper;
import tnt.tarkovcraft.core.util.helper.RenderUtils;
import tnt.tarkovcraft.medsystem.client.MedicalSystemClient;
import tnt.tarkovcraft.medsystem.client.config.HealthDisplayType;
import tnt.tarkovcraft.medsystem.client.config.HealthOverlayConfiguration;
import tnt.tarkovcraft.medsystem.client.config.MedSystemClientConfig;
import tnt.tarkovcraft.medsystem.client.overlay.HealthLayer;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.EffectVisibility;
import tnt.tarkovcraft.medsystem.common.health.Limb;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LimbHealthWidget extends AbstractWidget {

    private final Font font;
    private final Limb limb;

    private int frameSize = 1;
    private int frameColor = ColorPalette.WHITE;
    private int frameHoverColor = ColorPalette.YELLOW;
    private int backgroundColor = 0xFF << 24;
    private int textColor = ColorPalette.WHITE;
    private int textHoverColor = ColorPalette.YELLOW;
    private int effectScale = 12;
    private SimpleClickListener onClick;
    private List<StatusEffect> effects;
    private boolean effectDetail = true;
    private DisplayComponent displayComponent = this.getDefaultComponent();

    public LimbHealthWidget(int x, int y, int width, int height, Font font, Limb limb) {
        super(x, y, width, height, CommonComponents.EMPTY);
        this.font = font;
        this.limb = limb;
    }

    public void setEffects(List<StatusEffect> effects) {
        this.effects = effects;
    }

    public void setEffectIconSize(int effectIconSize) {
        this.effectScale = effectIconSize;
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

    public void setDisplayComponent(DisplayComponent displayComponent) {
        this.displayComponent = displayComponent;
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

        // content
        this.displayComponent.renderContents(graphics, this);

        // Status effects - we need 14 px for render (12scale+2border) + 17 for health bar offset + 2xFrameSize
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

    private Component getStatusTitle(int scale) {
        return this.isHovered
                ? this.limb.getDisplayName()
                : Component.literal(Mth.floor(this.limb.getHealth() * scale) + "/" + Mth.floor(this.limb.getMaxHealth() * scale));
    }

    private DisplayComponent getDefaultComponent() {
        MedSystemClientConfig config = MedicalSystemClient.getConfig();
        return config.healthDisplayType == HealthDisplayType.NUMERIC ? DisplayComponent.NUMERIC : DisplayComponent.ICON;
    }

    public sealed interface DisplayComponent {

        DisplayComponent NUMERIC = new NumericDisplayComponent();
        DisplayComponent ICON = new IconDisplayComponent();

        void renderContents(GuiGraphics graphics, LimbHealthWidget parent);

        final class NumericDisplayComponent implements DisplayComponent {

            @Override
            public void renderContents(GuiGraphics graphics, LimbHealthWidget parent) {
                MedSystemClientConfig config = MedicalSystemClient.getConfig();
                int scale = (int) Math.pow(10, config.numericHealthScale);

                // health status
                Component status = parent.getStatusTitle(scale);
                int statusWidth = parent.font.width(status);
                int textColor = parent.limb.isDead() ? 0xFFFF0000 : parent.isHovered ? parent.textHoverColor : parent.textColor;
                graphics.drawString(parent.font, status, parent.getX() + (parent.width - statusWidth) / 2, parent.getY() + 3 + parent.frameSize, textColor);

                HealthOverlayConfiguration overlay = config.healthOverlay;
                int background = Integer.decode(overlay.deadLimbColor) | 0xFF << 24;
                int secondaryBackground = ARGB.scaleRGB(background, 0.8F);
                int color = HealthLayer.getColor(overlay.deadLimbColor, overlay.colorSchema, parent.limb) | 0xFF << 24;
                int secondaryColor = ARGB.scaleRGB(color, 0.8F);
                float f = parent.limb.getHealthPercent();

                // healthbar bg
                graphics.fillGradient(parent.getX() + parent.frameSize + 1, parent.getY() + parent.frameSize + 13, parent.getRight() - parent.frameSize - 1, parent.getY() + parent.frameSize + 17, background, secondaryBackground);

                // healthbar fg
                int left = parent.getX() + parent.frameSize + 2;
                int right = parent.getRight() - parent.frameSize - 2;
                graphics.fillGradient(left, parent.getY() + parent.frameSize + 14, left + (int) ((right - left) * f), parent.getY() + parent.frameSize + 16, color, secondaryColor);
            }
        }

        final class IconDisplayComponent implements DisplayComponent {

            @Override
            public void renderContents(GuiGraphics graphics, LimbHealthWidget parent) {
                Limb limb = parent.limb;
                if (parent.isHovered) {
                    Component displayName = limb.getDisplayName();
                    int width = parent.font.width(displayName);
                    int left = parent.getX() + parent.frameSize + (parent.width - width - parent.frameSize) / 2;
                    graphics.drawString(parent.font, displayName, left, parent.getY() + parent.frameSize + 5, parent.textHoverColor);
                } else {
                    int maxHealth = Mth.ceil(limb.getMaxHealth());
                    int health = Mth.ceil(limb.getHealth());
                    int hearts = Mth.ceil(maxHealth / 2.0F);
                    boolean vital = limb.isVital();
                    for (int i = 0; i < hearts; i++) {
                        // background
                        int left = parent.getX() + parent.frameSize + 1 + i * 9;
                        int top = parent.getY() + parent.frameSize + 5;
                        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Gui.HeartType.CONTAINER.getSprite(vital, false, false), left, top, 9, 9);

                        // health
                        int healthOffset = 2 * i;
                        if (health > healthOffset) {
                            boolean halfHeart = health - healthOffset == 1;
                            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Gui.HeartType.NORMAL.getSprite(vital, halfHeart, false), left, top, 9, 9);
                        }
                    }
                }
            }
        }
    }
}
