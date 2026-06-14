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
import tnt.tarkovcraft.core.api.client.SynchronizableScreen;
import tnt.tarkovcraft.core.client.screen.ColorPalette;
import tnt.tarkovcraft.core.client.screen.renderable.LabelRenderable;
import tnt.tarkovcraft.core.client.screen.renderable.ShapeRenderable;
import tnt.tarkovcraft.core.util.HorizontalAlignment;
import tnt.tarkovcraft.core.util.helper.TextHelper;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.api.heal.HealItemAttributes;
import tnt.tarkovcraft.medsystem.client.MedicalSystemClient;
import tnt.tarkovcraft.medsystem.client.config.HealthOverlayConfiguration;
import tnt.tarkovcraft.medsystem.client.overlay.HealthLayer;
import tnt.tarkovcraft.medsystem.client.screen.widget.LimbHealthWidget;
import tnt.tarkovcraft.medsystem.client.screen.widget.LimbWidget;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.EffectVisibility;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthContainerDefinition;
import tnt.tarkovcraft.medsystem.common.health.HealthContainerDisplay;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;
import tnt.tarkovcraft.medsystem.common.init.MedSystemItemComponents;
import tnt.tarkovcraft.medsystem.common.item.InteractionTarget;
import tnt.tarkovcraft.medsystem.network.message.C2S_SelectLimb;

import java.util.ArrayList;
import java.util.List;

public class SelectLimbScreen extends Screen implements SynchronizableScreen {

    public static final Component TITLE = TextHelper.createScreenTitle(MedSystemConstants.MOD_ID, "select_body_part").withStyle(ChatFormatting.BOLD).withColor(ColorPalette.TEXT_COLOR);
    public static final Component LABEL_ERROR = TextHelper.createScreenComponent(MedSystemConstants.MOD_ID, "select_body_part", "error.invalid_item");
    public static final Component LABEL_NOT_HEALABLE = TextHelper.createScreenComponent(MedSystemConstants.MOD_ID, "select_body_part", "error.not_healable").withStyle(ChatFormatting.RED);
    public static final Component LABEL_CLICK_TO_SELECT = TextHelper.createScreenComponent(MedSystemConstants.MOD_ID, "select_body_part", "text.click_to_select").withStyle(ChatFormatting.GREEN);

    private final boolean selfHealing;
    private final int entityId;

    public SelectLimbScreen(boolean selfHealing, int entityID) {
        super(TITLE);
        this.selfHealing = selfHealing;
        this.entityId = entityID;
    }

    @Override
    public void sync(DataSource source) {
        if (source.equals(HealthContainer.HEALTH))
            this.init(this.width, this.height);
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
        HealthContainerDisplay display = definition.display();
        Vector2f center = new Vector2f(this.width / 2.0F, this.height / 2.0F);
        float scale = (this.width / 256.0F);
        List<LimbHealthWidget> healthWidgets = new ArrayList<>();
        display.accept((limbCode, data) -> {
            Limb limb = container.getLimbByCode(limbCode);
            Vector4i rect = data.getGuiPos(scale, center);
            boolean isLimbHealable = attributes.canUseOnLimb(limb, itemStack, container, target);
            LimbWidget widget = this.addRenderableWidget(new LimbWidget(rect.x, rect.y, rect.z, rect.w, limb, this.font));
            widget.setScale(3);
            widget.setColorProvider(value -> {
                HealthOverlayConfiguration overlay = MedicalSystemClient.getConfig().healthOverlay;
                if (isLimbHealable) {
                    return HealthLayer.getColor(overlay.deadLimbColor, overlay.colorSchema, value) | 0xFF << 24;
                }
                return Integer.decode(overlay.deadLimbColor) | 0xFF << 24;
            });
            widget.addTooltip(limb.getDisplayName().copy().withStyle(ChatFormatting.BOLD, isLimbHealable ? ChatFormatting.GREEN : ChatFormatting.RED));

            List<StatusEffect> effects = limb.getStatusEffects().getEffectsStream()
                    .filter(ef -> StatusEffectType.isVisible(ef, EffectVisibility.UI))
                    .toList();
            int healthWidth = 80;
            int healthHeight = effects.isEmpty() ? 20 : 33;
            int xOffset = (int) ((rect.x + rect.z / 2f) - center.x);
            int healthX = HealthScreen.getHealthLabelWidgetX(xOffset, rect.x, healthWidth, rect.z);
            int healthY = rect.y + (rect.w - healthHeight) / 2;
            LimbHealthWidget healthWidget = new LimbHealthWidget(healthX, healthY, healthWidth, healthHeight, this.font, limb);
            healthWidget.setEffects(effects);
            healthWidget.setFrameColor(isLimbHealable ? widget.getColor() : 0xFF << 24);
            healthWidget.setFrameHoverColor(isLimbHealable ? ColorPalette.YELLOW : 0xFF << 24);
            healthWidget.setTextColor(isLimbHealable ? widget.getColor() : 0xFF444444);
            healthWidget.setEffectDetail(false);
            healthWidget.setTextHoverColor(isLimbHealable ? ColorPalette.YELLOW : 0xFF999999);
            healthWidget.setClickListener(() -> this.limbClicked(limb));
            healthWidgets.add(healthWidget);

            if (isLimbHealable) {
                widget.setOnClick(() -> this.limbClicked(limb));
                widget.addTooltip(LABEL_CLICK_TO_SELECT);
            } else {
                widget.addTooltip(LABEL_NOT_HEALABLE);
            }
        });
        healthWidgets.forEach(this::addRenderableWidget);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void limbClicked(Limb part) {
        InteractionTarget target = new InteractionTarget(this.selfHealing, this.entityId, part.getLimbCode());
        ClientPacketDistributor.sendToServer(new C2S_SelectLimb(target));
        this.minecraft.setScreen(null);
    }

    private void addError() {
        LabelRenderable error = this.addRenderableOnly(LabelRenderable.fromComponent(0, 0, this.width, this.height, this.font, LABEL_ERROR));
        error.setHorizontalAlignment(HorizontalAlignment.CENTER);
        error.setTextColor(ColorPalette.RED);
        error.setShadow(true);
    }
}
