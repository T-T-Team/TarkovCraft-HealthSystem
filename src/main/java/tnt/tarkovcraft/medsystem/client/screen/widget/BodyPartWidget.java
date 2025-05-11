package tnt.tarkovcraft.medsystem.client.screen.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import tnt.tarkovcraft.core.client.screen.SharedScreenHoverState;
import tnt.tarkovcraft.core.client.screen.TooltipHelper;
import tnt.tarkovcraft.core.client.screen.listener.SimpleClickListener;
import tnt.tarkovcraft.medsystem.client.MedicalSystemClient;
import tnt.tarkovcraft.medsystem.client.config.HealthOverlayConfiguration;
import tnt.tarkovcraft.medsystem.client.overlay.HealthLayer;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

public class BodyPartWidget extends AbstractWidget {

    private final BodyPart part;

    private int scale = 2;
    private ToIntFunction<BodyPart> colorProvider;
    private SimpleClickListener onClick;

    private TooltipHelper tooltipHelper;
    private SharedScreenHoverState<BodyPart> hoverState;
    private List<FormattedCharSequence> customTooltip = new ArrayList<>();

    public BodyPartWidget(int x, int y, int width, int height, BodyPart part) {
        super(x, y, width, height, part.getDisplayName());
        this.part = part;
        this.colorProvider = bodypart -> {
            HealthOverlayConfiguration overlay = MedicalSystemClient.getConfig().healthOverlay;
            return HealthLayer.getColor(overlay.deadLimbColor, overlay.colorSchema, bodypart) | 0xFF << 24;
        };
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int color = this.colorProvider.applyAsInt(this.part);
        if (this.hoverState != null) {
            if (this.isHovered) {
                this.hoverState.setState(this, this.part);
            } else {
                this.hoverState.clearState(this);
            }
            BodyPart statePart = this.hoverState.getState();
            if (statePart != null && statePart.getName().equals(this.part.getName())) {
                color = ARGB.lerp(0.5F, color, 0xFFFFFFFF);
            }
        }
        graphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), ARGB.scaleRGB(color, 0.8F));
        graphics.fill(this.getX() + this.scale, this.getY() + this.scale, this.getRight() - this.scale, this.getBottom() - this.scale, color);

        if (this.tooltipHelper != null && this.isHovered()) {
            this.tooltipHelper.setForNextRenderPass(this.customTooltip);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return this.onClick != null;
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        this.onClick.onClick();
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public void setColorProvider(ToIntFunction<BodyPart> colorProvider) {
        this.colorProvider = colorProvider;
    }

    public void setOnClick(SimpleClickListener onClick) {
        this.onClick = onClick;
    }

    public void setTooltipHelper(TooltipHelper tooltipHelper) {
        this.tooltipHelper = tooltipHelper;
    }

    public void setHoverState(SharedScreenHoverState<BodyPart> hoverState) {
        this.hoverState = hoverState;
    }

    public TooltipHelper getTooltipHelper() {
        return tooltipHelper;
    }

    public void addTooltip(Component line) {
        if (this.tooltipHelper == null) {
            throw new UnsupportedOperationException("Tooltip helper is not set on widget!");
        }
        this.customTooltip.addAll(this.tooltipHelper.split(line));
    }
}
