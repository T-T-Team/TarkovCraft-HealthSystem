package tnt.tarkovcraft.medsystem.client.screen;

import net.minecraft.client.gui.components.Tooltip;
import org.joml.Vector2f;
import org.joml.Vector4f;
import tnt.tarkovcraft.core.client.screen.CharacterSubScreen;
import tnt.tarkovcraft.core.client.screen.ColorPalette;
import tnt.tarkovcraft.core.client.screen.renderable.ShapeRenderable;
import tnt.tarkovcraft.core.client.screen.widget.ListWidget;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.core.util.context.ContextKeys;
import tnt.tarkovcraft.medsystem.client.MedicalSystemClient;
import tnt.tarkovcraft.medsystem.client.screen.widget.BodyPartWidget;
import tnt.tarkovcraft.medsystem.client.screen.widget.BodyPartHealthWidget;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.BodyPartDisplay;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthContainerDefinition;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;

import java.time.Duration;
import java.util.List;

public class HealthScreen extends CharacterSubScreen {

    private double bodyPartScroll;

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

        int left = this.width / 3 - 15;
        ListWidget<BodyPartHealthWidget> list = this.addRenderableWidget(new ListWidget<>(left, 35, this.width - left, this.height - 35, displays, (display, index) -> this.createBodyPartWidget(display, container, index)));
        list.setAdditionalItemSpacing(5);
        list.setScroll(this.bodyPartScroll);
        list.setScrollListener((x, y) -> this.bodyPartScroll = y);

        for (BodyPartDisplay display : displays) {
            String name = display.source();
            BodyPart part = container.getBodyPart(name);
            if (part == null)
                return;
            Vector4f pos = display.getGuiPosition(1.5F, center);
            BodyPartWidget bodyPartWidget = this.addRenderableOnly(new BodyPartWidget((int) pos.x, (int) pos.y, (int) pos.z, (int) pos.w, part));
            bodyPartWidget.setScale(3);
            bodyPartWidget.setTooltip(Tooltip.create(part.getDisplayName()));
            bodyPartWidget.setTooltipDelay(Duration.ofMillis(500));
        }
    }

    private BodyPartHealthWidget createBodyPartWidget(BodyPartDisplay display, HealthContainer container, int index) {
        int left = this.width / 3 - 15;
        BodyPart part = container.getBodyPart(display.source());
        return new BodyPartHealthWidget(left, index * 35, 100, 30, this.font, part);
    }

}
