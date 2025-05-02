package tnt.tarkovcraft.medsystem.client.screen.renderable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.ARGB;
import tnt.tarkovcraft.core.client.screen.renderable.AbstractRenderable;
import tnt.tarkovcraft.medsystem.client.MedicalSystemClient;
import tnt.tarkovcraft.medsystem.client.config.HealthOverlayConfiguration;
import tnt.tarkovcraft.medsystem.client.overlay.HealthLayer;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;

public class BodyPartRenderable extends AbstractRenderable {

    private final BodyPart part;

    private int scale = 2;

    public BodyPartRenderable(int x, int y, int width, int height, BodyPart part) {
        super(x, y, width, height);
        this.part = part;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        HealthOverlayConfiguration overlay = MedicalSystemClient.getConfig().healthOverlay;
        int color = HealthLayer.getColor(overlay.deadLimbColor, overlay.colorSchema, part) | 0xFF << 24;
        graphics.fill(this.x, this.y, this.getRight(), this.getBottom(), ARGB.scaleRGB(color, 0.8F));
        graphics.fill(this.x + this.scale, this.y + this.scale, this.getRight() - this.scale, this.getBottom() - this.scale, color);
    }

    public void setScale(int scale) {
        this.scale = scale;
    }
}
