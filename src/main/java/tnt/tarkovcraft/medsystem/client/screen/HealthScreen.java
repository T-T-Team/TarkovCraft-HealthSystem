package tnt.tarkovcraft.medsystem.client.screen;

import net.minecraft.client.gui.components.Tooltip;
import org.joml.Vector2f;
import org.joml.Vector4i;
import tnt.tarkovcraft.core.client.screen.CharacterSubScreen;
import tnt.tarkovcraft.core.client.screen.ColorPalette;
import tnt.tarkovcraft.core.client.screen.renderable.ShapeRenderable;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.core.util.context.ContextKeys;
import tnt.tarkovcraft.medsystem.client.MedicalSystemClient;
import tnt.tarkovcraft.medsystem.client.screen.widget.BodyPartHealthWidget;
import tnt.tarkovcraft.medsystem.client.screen.widget.BodyPartWidget;
import tnt.tarkovcraft.medsystem.common.effect.EffectVisibility;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.BodyPartDisplay;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthContainerDefinition;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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
        Vector2f center = new Vector2f(this.width / 2.0F, this.height / 2.0F);

        List<BodyPartHealthWidget> healthWidgets = new ArrayList<>();
        float scale = (this.width / 256.0F);
        for (BodyPartDisplay display : displays) {
            String name = display.source();
            BodyPart part = container.getBodyPart(name);
            if (part == null)
                return;
            Vector4i pos = display.getPositionForGui(scale, center);
            int x = pos.x;
            int y = pos.y;
            int width = pos.z;
            int height = pos.w;
            int xOffset = (int) ((pos.x + width / 2f) - center.x);
            BodyPartWidget bodyPartWidget = this.addRenderableOnly(new BodyPartWidget(x, y, width, height, part, this.font));
            bodyPartWidget.setScale(3);
            bodyPartWidget.setTooltip(Tooltip.create(part.getDisplayName()));
            bodyPartWidget.setTooltipDelay(Duration.ofMillis(500));
            // status effects
            Stream<StatusEffect> stream = part.getStatusEffects().getEffectsStream();
            // add global effects to root body part
            if (container.getRootBodyPart().getName().equals(part.getName())) {
                stream = Stream.concat(
                        container.getGlobalStatusEffects().getEffectsStream(),
                        stream
                );
            }
            List<StatusEffect> effects = stream.filter(ef -> ef.isActive() && ef.getType().getVisibility().isVisibleInMode(EffectVisibility.UI))
                    .toList();
            int healthWidth = 80;
            int healthHeight = effects.isEmpty() ? 20 : 33;
            int healthX = this.getHealthLabelWidgetX(xOffset, x, healthWidth, width);
            int healthY = y + (height - healthHeight) / 2;
            BodyPartHealthWidget healthWidget = new BodyPartHealthWidget(healthX, healthY, healthWidth, healthHeight, this.font, part);
            healthWidget.setHealthUnitScale(10F);
            healthWidget.setEffects(effects);

            healthWidgets.add(healthWidget); // add to list for later addition so it can be rendered on top
        }

        healthWidgets.forEach(this::addRenderableOnly);
    }

    private int getHealthLabelWidgetX(int xOffset, int posX, int labelWidth, int limbWidth) {
        if (xOffset == 0) {
            return posX + (limbWidth - labelWidth) / 2;
        } else if (xOffset > 0) {
            return posX + limbWidth + 2;
        } else {
            return posX - labelWidth - 2;
        }

    }
}
