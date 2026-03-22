package tnt.tarkovcraft.medsystem.client.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
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
import tnt.tarkovcraft.medsystem.client.config.HealthDisplayType;
import tnt.tarkovcraft.medsystem.client.config.MedSystemClientConfig;
import tnt.tarkovcraft.medsystem.client.screen.widget.LimbHealthWidget;
import tnt.tarkovcraft.medsystem.client.screen.widget.LimbWidget;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodConfiguration;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.EffectVisibility;
import tnt.tarkovcraft.medsystem.common.health.*;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HealthScreen extends CharacterSubScreen implements HealthContainerScreen {

    public static final Identifier HEALTH_ICON = MedicalSystem.createIdentifier("textures/icons/health.png");
    public static final Identifier DROPLET_ICON = MedicalSystem.createIdentifier("textures/icons/droplet.png");

    private HealthContainer healthContainer;

    public HealthScreen(Screen parent, UUID userId) {
        super(userId, MedicalSystemClient.HEALTH);
    }

    @Override
    public void onHealthContainerUpdated(IAttachmentHolder holder, HealthContainer container) {
        this.init(this.width, this.height);
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableOnly(new ShapeRenderable(0, 25, this.width, this.height - 25, ColorPalette.BG_TRANSPARENT_WEAK));

        this.healthContainer = this.minecraft.player.getData(MedSystemDataAttachments.HEALTH_CONTAINER);
        LimbContainer limbContainer = this.healthContainer.getLimbContainer();
        HealthContainerDefinition definition = this.healthContainer.getDefinition();

        // label registration
        List<IconWithLabel> list = new ArrayList<>();
        MedSystemClientConfig config = MedicalSystemClient.getConfig();
        float healthScale = config.healthDisplayType == HealthDisplayType.HEARTS
                ? 0.5F : (float) Math.pow(10, config.numericHealthScale);
        // Current health display
        list.add(new IconWithLabel(
                HEALTH_ICON,
                () -> Component.literal(Mth.floor(limbContainer.getHealth() * healthScale) + "/" + Mth.floor(limbContainer.getMaxHealth() * healthScale)),
                0xFF55FF55, 0xFF55FF55
        ));
        // Blood type indicator
        EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(this.minecraft.player);
        if (bloodSystem != null) {
            Identifier bloodTypeId = bloodSystem.getBloodType();
            BloodConfiguration configuration = MedicalSystem.BLOOD_SYSTEM.getConfig();
            configuration.getOptions(bloodTypeId).ifPresent(options -> {
                Component styledLabel = options.getStylizedLabel();
                list.add(new IconWithLabel(
                        DROPLET_ICON,
                        () -> styledLabel,
                        ARGB.opaque(options.color()), 0xFFFFFFFF
                ));
            });
        }
        // Additional labels
        NeoForge.EVENT_BUS.post(new RegisterHealthScreenLabelsEvent(list));
        for (int i = 0; i < list.size(); i++) {
            IconWithLabel icon = list.get(i);
            IconWithLabelRenderable renderable = this.addRenderableOnly(new IconWithLabelRenderable(this.font, 5, this.height - 5 - this.font.lineHeight - i * 12, this.width / 3, 10, HorizontalAlignment.LEFT, icon));
            renderable.setIconSize(10);
            renderable.setHorizontalTextOffset(3);
        }

        Vector2f center = new Vector2f(this.width / 2.0F, this.height / 2.0F);
        List<LimbHealthWidget> healthWidgets = new ArrayList<>();
        float scale = (this.height / 256.0F) * 2.0F;
        HealthContainerDisplay display = definition.display();
        display.accept((limbCode, data) -> {
            Limb limb = this.healthContainer.getLimbByCode(limbCode);
            Vector4i pos = data.getGuiPos(scale, center);
            int x = pos.x;
            int y = pos.y;
            int width = pos.z;
            int height = pos.w;
            int xOffset = (int) ((pos.x + width / 2f) - center.x);
            LimbWidget limbWidget = this.addRenderableOnly(new LimbWidget(x, y, width, height, limb, this.font));
            limbWidget.setScale(3);
            // status effects
            List<StatusEffect> effects = limb.getStatusEffects().getEffectsStream().filter(ef -> StatusEffectType.isVisible(ef, EffectVisibility.UI))
                    .toList();
            int healthWidth = 80;
            int healthHeight = effects.isEmpty() ? 20 : 33;
            int healthX = getHealthLabelWidgetX(xOffset, x, healthWidth, width);
            int healthY = y + (height - healthHeight) / 2;
            LimbHealthWidget healthWidget = new LimbHealthWidget(healthX, healthY, healthWidth, healthHeight, this.font, limb);
            healthWidget.setEffects(effects);
            healthWidget.setTextHoverColor(ColorPalette.WHITE);

            healthWidgets.add(healthWidget); // add to list for later addition so it can be rendered on top
        });

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
