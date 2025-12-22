package tnt.tarkovcraft.medsystem.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.joml.Vector2f;
import org.joml.Vector4i;
import tnt.tarkovcraft.core.client.screen.ColorPalette;
import tnt.tarkovcraft.core.client.screen.renderable.LabelRenderable;
import tnt.tarkovcraft.core.client.screen.renderable.ShapeRenderable;
import tnt.tarkovcraft.core.util.HorizontalAlignment;
import tnt.tarkovcraft.core.util.helper.TextHelper;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.heal.HealItemAttributes;
import tnt.tarkovcraft.medsystem.client.MedicalSystemClient;
import tnt.tarkovcraft.medsystem.client.config.HealthOverlayConfiguration;
import tnt.tarkovcraft.medsystem.client.overlay.HealthLayer;
import tnt.tarkovcraft.medsystem.client.screen.widget.BodyPartHealthWidget;
import tnt.tarkovcraft.medsystem.client.screen.widget.BodyPartWidget;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.EffectVisibility;
import tnt.tarkovcraft.medsystem.common.health.BodyPartDisplay;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthContainerDefinition;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;
import tnt.tarkovcraft.medsystem.common.init.MedSystemItemComponents;
import tnt.tarkovcraft.medsystem.common.item.InteractionTarget;
import tnt.tarkovcraft.medsystem.network.message.C2S_SelectBodyPart;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class SelectBodyPartScreen extends Screen {

    public static final Component TITLE = TextHelper.createScreenTitle(MedicalSystem.MOD_ID, "select_body_part").withStyle(ChatFormatting.BOLD).withColor(ColorPalette.TEXT_COLOR);
    public static final Component LABEL_ERROR = TextHelper.createScreenComponent(MedicalSystem.MOD_ID, "select_body_part", "error.invalid_item");
    public static final Component LABEL_NOT_HEALABLE = TextHelper.createScreenComponent(MedicalSystem.MOD_ID, "select_body_part", "error.not_healable").withStyle(ChatFormatting.RED);
    public static final Component LABEL_CLICK_TO_SELECT = TextHelper.createScreenComponent(MedicalSystem.MOD_ID, "select_body_part", "text.click_to_select").withStyle(ChatFormatting.GREEN);

    private final boolean selfHealing;
    private final int entityId;

    public SelectBodyPartScreen(boolean selfHealing, int entityID) {
        super(TITLE);
        this.selfHealing = selfHealing;
        this.entityId = entityID;
    }

    @Override
    protected void init() {
        ItemStack itemStack = this.minecraft.player.getMainHandItem();
        Component subtitle;
        LivingEntity target;
        if (this.selfHealing) {
            subtitle = Component.translatable("label.medsystem.healing.self.target");
            target = this.minecraft.player;
        } else {
            Entity entity = this.minecraft.level.getEntity(this.entityId);
            if (!(entity instanceof LivingEntity livingEntity)) {
                this.minecraft.setScreen(null);
                return;
            }
            subtitle = Component.translatable("label.medsystem.healing.other.target", entity.getDisplayName()).withStyle(ChatFormatting.YELLOW);
            target = livingEntity;
        }

        this.addRenderableOnly(new ShapeRenderable(0, 0, this.width, this.height, ColorPalette.BG_TRANSPARENT_DARK));
        LabelRenderable titleLabel = this.addRenderableOnly(LabelRenderable.fromComponent(0, 0, this.width, 20, this.font, TITLE));
        titleLabel.setShadow(true);
        titleLabel.setTextColor(ColorPalette.WHITE);
        titleLabel.setHorizontalAlignment(HorizontalAlignment.CENTER);
        LabelRenderable subtitleLabel = this.addRenderableOnly(LabelRenderable.fromComponent(0, 20, this.width, 10, this.font, subtitle));
        subtitleLabel.setShadow(true);
        subtitleLabel.setTextColor(ColorPalette.WHITE);
        subtitleLabel.setHorizontalAlignment(HorizontalAlignment.CENTER);
        if (itemStack.isEmpty()) {
            this.addError();
            return;
        }
        HealItemAttributes attributes = itemStack.get(MedSystemItemComponents.HEAL_ATTRIBUTES);
        if (attributes == null) {
            this.addError();
            return;
        }
        HealthContainer container = target.getData(MedSystemDataAttachments.HEALTH_CONTAINER);
        HealthContainerDefinition definition = container.getDefinition();
        List<BodyPartDisplay> displays = definition.getDisplayConfiguration();
        Vector2f center = new Vector2f(this.width / 2.0F, this.height / 2.0F);
        float scale = (this.width / 256.0F);
        List<BodyPartHealthWidget> healthWidgets = new ArrayList<>();
        for (BodyPartDisplay display : displays) {
            Limb part = container.getLimbByCode(display.source());
            if (part == null)
                continue;
            Vector4i rect = display.getPositionForGui(scale, center);
            boolean isPartHealable = attributes.canUseOnPart(part, itemStack, container, selfHealing, target);
            BodyPartWidget widget = this.addRenderableWidget(new BodyPartWidget(rect.x, rect.y, rect.z, rect.w, part, this.font));
            widget.setScale(3);
            widget.setColorProvider(value -> {
                HealthOverlayConfiguration overlay = MedicalSystemClient.getConfig().healthOverlay;
                if (isPartHealable) {
                    return HealthLayer.getColor(overlay.deadLimbColor, overlay.colorSchema, value) | 0xFF << 24;
                }
                return Integer.decode(overlay.deadLimbColor) | 0xFF << 24;
            });
            widget.addTooltip(part.getDisplayName().copy().withStyle(ChatFormatting.BOLD, isPartHealable ? ChatFormatting.GREEN : ChatFormatting.RED));

            Stream<StatusEffect> stream = part.getStatusEffects().getEffectsStream();
            if (container.getRootLimb().getLimbCode().equals(part.getLimbCode())) {
                stream = Stream.concat(
                        container.getGlobalStatusEffects().getEffectsStream(),
                        stream
                );
            }
            List<StatusEffect> effects = stream.filter(ef -> StatusEffectType.isVisible(ef, EffectVisibility.UI))
                    .toList();
            int healthWidth = 80;
            int healthHeight = effects.isEmpty() ? 20 : 33;
            int xOffset = (int) ((rect.x + rect.z / 2f) - center.x);
            int healthX = HealthScreen.getHealthLabelWidgetX(xOffset, rect.x, healthWidth, rect.z);
            int healthY = rect.y + (rect.w - healthHeight) / 2;
            BodyPartHealthWidget healthWidget = new BodyPartHealthWidget(healthX, healthY, healthWidth, healthHeight, this.font, part);
            healthWidget.setHealthUnitScale(HealthScreen.UNIT_SCALE);
            healthWidget.setEffects(effects);
            healthWidget.setFrameColor(isPartHealable ? widget.getColor() : 0xFF << 24);
            healthWidget.setFrameHoverColor(isPartHealable ? ColorPalette.YELLOW : 0xFF << 24);
            healthWidget.setTextColor(isPartHealable ? widget.getColor() : 0xFF444444);
            healthWidget.setEffectDetail(false);
            healthWidget.setTextHoverColor(isPartHealable ? ColorPalette.YELLOW : 0xFF999999);
            healthWidget.setClickListener(() -> this.bodyPartClicked(part));
            healthWidgets.add(healthWidget);

            if (isPartHealable) {
                widget.setOnClick(() -> this.bodyPartClicked(part));
                widget.addTooltip(LABEL_CLICK_TO_SELECT);
            } else {
                widget.addTooltip(LABEL_NOT_HEALABLE);
            }
        }
        healthWidgets.forEach(this::addRenderableWidget);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void bodyPartClicked(Limb part) {
        InteractionTarget target = new InteractionTarget(this.selfHealing, this.entityId, part.getLimbCode());
        ClientPacketDistributor.sendToServer(new C2S_SelectBodyPart(target));
        this.minecraft.setScreen(null);
    }

    private void addError() {
        LabelRenderable error = this.addRenderableOnly(LabelRenderable.fromComponent(0, 0, this.width, this.height, this.font, LABEL_ERROR));
        error.setHorizontalAlignment(HorizontalAlignment.CENTER);
        error.setTextColor(ColorPalette.RED);
        error.setShadow(true);
    }
}
