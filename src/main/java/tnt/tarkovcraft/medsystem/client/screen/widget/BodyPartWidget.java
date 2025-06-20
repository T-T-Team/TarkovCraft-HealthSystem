package tnt.tarkovcraft.medsystem.client.screen.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import tnt.tarkovcraft.core.client.screen.SharedScreenState;
import tnt.tarkovcraft.core.client.screen.listener.SimpleClickListener;
import tnt.tarkovcraft.medsystem.client.MedicalSystemClient;
import tnt.tarkovcraft.medsystem.client.config.HealthOverlayConfiguration;
import tnt.tarkovcraft.medsystem.client.overlay.HealthLayer;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;

public class BodyPartWidget extends AbstractWidget {

    private final BodyPart part;
    private final Font font;

    private int scale = 2;
    private ToIntFunction<BodyPart> colorProvider;
    private SimpleClickListener onClick;

    private SharedScreenState<BodyPart> hoverState;
    private List<Component> customTooltip = new ArrayList<>();

    public BodyPartWidget(int x, int y, int width, int height, BodyPart part, Font font) {
        super(x, y, width, height, part.getDisplayName());
        this.part = part;
        this.font = font;
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

        if (!this.customTooltip.isEmpty() && this.isHovered()) {
            graphics.setTooltipForNextFrame(this.font, this.customTooltip, Optional.empty(), mouseX, mouseY);
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

    public void setHoverState(SharedScreenState<BodyPart> hoverState) {
        this.hoverState = hoverState;
    }

    public void addTooltip(Component line) {
        this.customTooltip.add(line);
    }
}
