package tnt.tarkovcraft.medsystem.client.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector2f;
import org.joml.Vector4i;
import tnt.tarkovcraft.core.client.IconWithLabel;
import tnt.tarkovcraft.core.client.screen.CharacterSubScreen;
import tnt.tarkovcraft.core.client.screen.ColorPalette;
import tnt.tarkovcraft.core.client.screen.renderable.IconWithLabelRenderable;
import tnt.tarkovcraft.core.client.screen.renderable.ShapeRenderable;
import tnt.tarkovcraft.core.util.HorizontalAlignment;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.event.client.RegisterHealthScreenLabelsEvent;
import tnt.tarkovcraft.medsystem.client.MedicalSystemClient;
import tnt.tarkovcraft.medsystem.client.screen.widget.BodyPartHealthWidget;
import tnt.tarkovcraft.medsystem.client.screen.widget.BodyPartWidget;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.EffectVisibility;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health.BodyPartDisplay;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthContainerDefinition;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class HealthScreen extends CharacterSubScreen {

    public static final ResourceLocation HEALTH_ICON = MedicalSystem.resource("textures/icons/health.png");
    public static final float UNIT_SCALE = 10.0F;

    private HealthContainer healthContainer;

    public HealthScreen(Screen parent, UUID userId) {
        super(userId, MedicalSystemClient.HEALTH);
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableOnly(new ShapeRenderable(0, 25, this.width, this.height - 25, ColorPalette.BG_TRANSPARENT_WEAK));

        this.healthContainer = this.minecraft.player.getData(MedSystemDataAttachments.HEALTH_CONTAINER);
        HealthContainerDefinition definition = this.healthContainer.getDefinition();

        // label registration
        List<IconWithLabel> list = new ArrayList<>();
        list.add(new IconWithLabel(
                        HEALTH_ICON,
                        () -> Component.literal(Mth.ceil(healthContainer.getHealth() * UNIT_SCALE) + "/" + Mth.ceil(healthContainer.getMaxHealth() * UNIT_SCALE)),
                        0xFF55FF55, 0xFF55FF55
                )
        );
        NeoForge.EVENT_BUS.post(new RegisterHealthScreenLabelsEvent(list));
        for (int i = 0; i < list.size(); i++) {
            IconWithLabel icon = list.get(i);
            IconWithLabelRenderable renderable = this.addRenderableOnly(new IconWithLabelRenderable(this.font, 5, this.height - 5 - this.font.lineHeight - i * 11, this.width / 3, 10, HorizontalAlignment.LEFT, icon));
            renderable.setIconSize(10);
            renderable.setHorizontalTextOffset(5);
        }

        List<BodyPartDisplay> displays = definition.getDisplayConfiguration();
        Vector2f center = new Vector2f(this.width / 2.0F, this.height / 2.0F);

        List<BodyPartHealthWidget> healthWidgets = new ArrayList<>();
        float scale = (this.width / 256.0F);
        for (BodyPartDisplay display : displays) {
            String name = display.source();
            Limb part = this.healthContainer.getLimb(name);
            if (part == null)
                continue;
            Vector4i pos = display.getPositionForGui(scale, center);
            int x = pos.x;
            int y = pos.y;
            int width = pos.z;
            int height = pos.w;
            int xOffset = (int) ((pos.x + width / 2f) - center.x);
            BodyPartWidget bodyPartWidget = this.addRenderableOnly(new BodyPartWidget(x, y, width, height, part, this.font));
            bodyPartWidget.setScale(3);
            // status effects
            Stream<StatusEffect> stream = part.getStatusEffects().getEffectsStream();
            // add global effects to root body part
            if (this.healthContainer.getRootLimb().getLimbCode().equals(part.getLimbCode())) {
                stream = Stream.concat(
                        this.healthContainer.getGlobalStatusEffects().getEffectsStream(),
                        stream
                );
            }
            List<StatusEffect> effects = stream.filter(ef -> StatusEffectType.isVisible(ef, EffectVisibility.UI))
                    .toList();
            int healthWidth = 80;
            int healthHeight = effects.isEmpty() ? 20 : 33;
            int healthX = getHealthLabelWidgetX(xOffset, x, healthWidth, width);
            int healthY = y + (height - healthHeight) / 2;
            BodyPartHealthWidget healthWidget = new BodyPartHealthWidget(healthX, healthY, healthWidth, healthHeight, this.font, part);
            healthWidget.setHealthUnitScale(UNIT_SCALE);
            healthWidget.setEffects(effects);
            healthWidget.setTextHoverColor(ColorPalette.WHITE);

            healthWidgets.add(healthWidget); // add to list for later addition so it can be rendered on top
        }

        healthWidgets.forEach(this::addRenderableOnly);
    }

    static int getHealthLabelWidgetX(int xOffset, int posX, int labelWidth, int limbWidth) {
        if (xOffset == 0) {
            return posX + (limbWidth - labelWidth) / 2;
        } else if (xOffset > 0) {
            return posX + limbWidth + 2;
        } else {
            return posX - labelWidth - 2;
        }
    }
}
