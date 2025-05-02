package tnt.tarkovcraft.medsystem.client.screen.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.util.ARGB;
import tnt.tarkovcraft.medsystem.client.MedicalSystemClient;
import tnt.tarkovcraft.medsystem.client.config.HealthOverlayConfiguration;
import tnt.tarkovcraft.medsystem.client.overlay.HealthLayer;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;

public class BodyPartWidget extends AbstractWidget {

    private final BodyPart part;

    private int scale = 2;

    public BodyPartWidget(int x, int y, int width, int height, BodyPart part) {
        super(x, y, width, height, part.getDisplayName());
        this.part = part;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        HealthOverlayConfiguration overlay = MedicalSystemClient.getConfig().healthOverlay;
        int color = HealthLayer.getColor(overlay.deadLimbColor, overlay.colorSchema, part) | 0xFF << 24;
        graphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), ARGB.scaleRGB(color, 0.8F));
        graphics.fill(this.getX() + this.scale, this.getY() + this.scale, this.getRight() - this.scale, this.getBottom() - this.scale, color);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    public void setScale(int scale) {
        this.scale = scale;
    }
}
