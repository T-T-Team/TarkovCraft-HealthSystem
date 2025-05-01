package tnt.tarkovcraft.medsystem.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Vector2f;
import org.joml.Vector4f;
import tnt.tarkovcraft.core.client.screen.CharacterSubScreen;
import tnt.tarkovcraft.core.client.screen.ColorPalette;
import tnt.tarkovcraft.core.client.screen.renderable.AbstractRenderable;
import tnt.tarkovcraft.core.client.screen.renderable.ShapeRenderable;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.core.util.context.ContextKeys;
import tnt.tarkovcraft.core.util.helper.RenderUtils;
import tnt.tarkovcraft.medsystem.client.MedicalSystemClient;
import tnt.tarkovcraft.medsystem.client.config.HealthOverlayConfiguration;
import tnt.tarkovcraft.medsystem.client.overlay.HealthLayer;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.BodyPartDisplay;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthContainerDefinition;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;

import java.util.List;

public class HealthScreen extends CharacterSubScreen {

    public HealthScreen(Context context) {
        super(context.getOrThrow(ContextKeys.UUID), MedicalSystemClient.HEALTH);
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableOnly(new ShapeRenderable(0, 25, this.width, this.height - 25, ColorPalette.BG_TRANSPARENT_WEAK));

        HealthContainer container = this.minecraft.player.getData(MedSystemDataAttachments.HEALTH_CONTAINER);
        HealthContainerDefinition definition = container.getDefinition();
        List<BodyPartDisplay> displays = definition.getDisplayConfiguration();
        Vector2f center = new Vector2f(this.width / 6.0F, this.height / 2.0F);
        int partIndex = 0;

        for (BodyPartDisplay display : displays) {
            String name = display.source();
            BodyPart part = container.getBodyPart(name);
            if (part == null)
                return;
            Vector4f pos = display.getGuiPosition(1.5F, center);
            BodyPartRenderable renderable = this.addRenderableOnly(new BodyPartRenderable((int) pos.x, (int) pos.y, (int) pos.z, (int) pos.w, part));
            renderable.setScale(3);

            // TODO status effects
            BodyPartHealthRenderable healthRenderable = this.addRenderableOnly(new BodyPartHealthRenderable(this.width / 3 - 15, 35 + partIndex * 40, 100, 30, this.font, name, part));

            ++partIndex;
        }
    }

    public static class BodyPartHealthRenderable extends AbstractRenderable {

        private final Font font;
        private final BodyPart part;
        private final Component label;

        private int frameSize = 1;
        private int frameColor = ColorPalette.WHITE;
        private int backgroundColor = 0xFF << 24;
        private int textColor = ColorPalette.WHITE;

        public BodyPartHealthRenderable(int x, int y, int width, int height, Font font, String name, BodyPart part) {
            super(x, y, width, height);
            this.font = font;
            this.part = part;
            this.label = Component.translatable("medsystem.bodypart." + name).withStyle(ChatFormatting.BOLD);
        }

        public void setFrameSize(int frameSize) {
            this.frameSize = frameSize;
        }

        public void setFrameColor(int frameColor) {
            this.frameColor = frameColor;
        }

        public void setBackgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
        }

        public void setTextColor(int textColor) {
            this.textColor = textColor;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            if (this.frameSize > 0 && RenderUtils.isVisibleColor(this.frameColor)) {
                graphics.fill(this.x, this.y, this.getRight(), this.getBottom(), this.frameColor);
            }
            if (RenderUtils.isVisibleColor(this.backgroundColor)) {
                graphics.fill(this.x + this.frameSize, this.y + this.frameSize, this.getRight() - this.frameSize, this.getBottom() - this.frameSize, this.backgroundColor);
            }
            int textColor = this.part.isDead() ? 0xFF0000 : this.textColor;
            int titleWidth = this.font.width(this.label);
            graphics.drawString(this.font, this.label, this.x + (this.width - titleWidth) / 2, this.y + 2 + this.frameSize, textColor);
            String status = Mth.ceil(this.part.getHealth()) + "/" + Mth.ceil(this.part.getMaxHealth());
            int statusWidth = this.font.width(status);
            graphics.drawString(this.font, status, this.x + (this.width - statusWidth) / 2, this.getBottom() - 14 - this.frameSize, textColor);
            HealthOverlayConfiguration overlay = MedicalSystemClient.getConfig().healthOverlay;
            int background = Integer.decode(overlay.deadLimbColor) | 0xFF << 24;
            int secondaryBackground = ARGB.scaleRGB(background, 0.8F);
            int color = HealthLayer.getColor(overlay.deadLimbColor, overlay.colorSchema, this.part) | 0xFF << 24;
            int secondaryColor = ARGB.scaleRGB(color, 0.8F);
            float f = this.part.getHealthPercent();
            graphics.fillGradient(this.x + this.frameSize + 1, this.getBottom() - this.frameSize - 5, this.getRight() - this.frameSize - 1, this.getBottom() - this.frameSize - 1, background, secondaryBackground);
            int left = this.x + this.frameSize + 2;
            int right = this.getRight() - this.frameSize - 2;
            graphics.fillGradient(left, this.getBottom() - this.frameSize - 4, left + (int) ((right - left) * f), this.getBottom() - this.frameSize - 2, color, secondaryColor);
        }
    }

    public static class BodyPartRenderable extends AbstractRenderable {

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
}
