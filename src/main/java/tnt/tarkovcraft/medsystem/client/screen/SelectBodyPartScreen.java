package tnt.tarkovcraft.medsystem.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector2f;
import org.joml.Vector4f;
import tnt.tarkovcraft.core.client.screen.ColorPalette;
import tnt.tarkovcraft.core.client.screen.renderable.AbstractTextRenderable;
import tnt.tarkovcraft.core.client.screen.renderable.ShapeRenderable;
import tnt.tarkovcraft.core.util.helper.TextHelper;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.heal.HealItemAttributes;
import tnt.tarkovcraft.medsystem.client.screen.widget.BodyPartWidget;
import tnt.tarkovcraft.medsystem.common.effect.EffectVisibility;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.BodyPartDisplay;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthContainerDefinition;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;
import tnt.tarkovcraft.medsystem.common.init.MedSystemItemComponents;
import tnt.tarkovcraft.medsystem.network.message.C2S_SelectBodyPart;

import java.util.List;
import java.util.stream.Collectors;

public class SelectBodyPartScreen extends Screen {

    public static final Component TITLE = TextHelper.createScreenTitle(MedicalSystem.MOD_ID, "select_body_part").withStyle(ChatFormatting.BOLD).withColor(ColorPalette.TEXT_COLOR);
    public static final Component LABEL_ERROR = TextHelper.createScreenComponent(MedicalSystem.MOD_ID, "select_body_part", "error.invalid_item");
    public static final Component LABEL_NOT_HEALABLE = TextHelper.createScreenComponent(MedicalSystem.MOD_ID, "select_body_part", "error.not_healable").withStyle(ChatFormatting.RED);
    public static final Component LABEL_CLICK_TO_SELECT = TextHelper.createScreenComponent(MedicalSystem.MOD_ID, "select_body_part", "text.click_to_select").withStyle(ChatFormatting.GREEN);
    public static final Component LABEL_STATUS_EFFECTS = TextHelper.createScreenComponent(MedicalSystem.MOD_ID, "select_body_part", "text.status_effects").withStyle(ChatFormatting.GRAY);

    public SelectBodyPartScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {
        ItemStack itemStack = this.minecraft.player.getMainHandItem();
        this.addRenderableOnly(new ShapeRenderable(0, 0, this.width, this.height, ColorPalette.BG_TRANSPARENT_DARK));
        this.addRenderableOnly(new AbstractTextRenderable.CenteredComponent(0, 0, this.width, 30, ColorPalette.WHITE, true, this.font, TITLE));
        if (itemStack.isEmpty()) {
            this.addError();
            return;
        }
        HealItemAttributes attributes = itemStack.get(MedSystemItemComponents.HEAL_ATTRIBUTES);
        if (attributes == null) {
            this.addError();
            return;
        }
        HealthContainer container = this.minecraft.player.getData(MedSystemDataAttachments.HEALTH_CONTAINER);
        HealthContainerDefinition definition = container.getDefinition();
        List<BodyPartDisplay> displays = definition.getDisplayConfiguration();
        Vector2f center = new Vector2f(this.width / 2.0F, this.height / 2.0F);
        for (BodyPartDisplay display : displays) {
            BodyPart part = container.getBodyPart(display.source());
            Vector4f rect = display.getGuiPosition(2.0F, center);
            BodyPartWidget widget = this.addRenderableWidget(new BodyPartWidget((int) rect.x, (int) rect.y, (int) rect.z, (int) rect.w, part, this.font));
            boolean isPartHealable = attributes.canUseOnPart(part, itemStack, container);
            widget.addTooltip(part.getDisplayName().copy().withStyle(ChatFormatting.BOLD, isPartHealable ? ChatFormatting.GREEN : ChatFormatting.RED));

            List<Component> statusEffectLabels = part.getStatusEffects().getEffectsStream()
                    .filter(effect -> effect.getType().getVisibility().isVisibleInMode(EffectVisibility.UI))
                    .map(effect -> Component.literal("- ").append(effect.getType().getDisplayName().copy()).withStyle(ChatFormatting.DARK_GRAY))
                    .collect(Collectors.toList());
            if (!statusEffectLabels.isEmpty()) {
                widget.addTooltip(LABEL_STATUS_EFFECTS);
                statusEffectLabels.forEach(widget::addTooltip);
            }
            if (isPartHealable) {
                widget.setOnClick(() -> this.bodyPartClicked(part));
                widget.addTooltip(LABEL_CLICK_TO_SELECT);
            } else {
                widget.addTooltip(LABEL_NOT_HEALABLE);
            }
        }
    }

    private void bodyPartClicked(BodyPart part) {
        PacketDistributor.sendToServer(new C2S_SelectBodyPart(part.getName()));
        this.minecraft.setScreen(null);
    }

    private void addError() {
        this.addRenderableOnly(new AbstractTextRenderable.CenteredComponent(0, 0, this.width, this.height, ColorPalette.RED, true, this.font, LABEL_ERROR));
    }
}
