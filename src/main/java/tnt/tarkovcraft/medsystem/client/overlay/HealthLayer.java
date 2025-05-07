package tnt.tarkovcraft.medsystem.client.overlay;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector2f;
import org.joml.Vector4f;
import tnt.tarkovcraft.core.client.screen.ColorPalette;
import tnt.tarkovcraft.core.util.helper.RenderUtils;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.client.MedicalSystemClient;
import tnt.tarkovcraft.medsystem.client.config.HealthOverlayConfiguration;
import tnt.tarkovcraft.medsystem.common.effect.EffectVisibility;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.health.*;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HealthLayer implements LayeredDraw.Layer {

    public static final ResourceLocation LAYER_ID = MedicalSystem.resource("layer/health");

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        HealthOverlayConfiguration overlay = MedicalSystemClient.getConfig().healthOverlay;
        if (!overlay.enabled)
            return;

        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        Entity camera = client.cameraEntity;
        if (camera == null || client.options.hideGui)
            return;
        if (player.isSpectator() && player == camera)
            return;
        if (player.isCreative()) {
            return;
        }
        Window window = client.getWindow();

        if (!HealthSystem.hasCustomHealth(camera))
            return;
        float scale = overlay.scale;
        float overlayWidth = 60 * scale;
        float overlayHeight = 110 * scale;
        Vector2f overlayPos = overlay.getPosition(0.0F, 0.0F, window.getGuiScaledWidth(), window.getGuiScaledHeight(), overlayWidth, overlayHeight);
        Vector2f center = new Vector2f(overlayPos.x + overlayWidth / 2.0F, overlayPos.y + overlayHeight / 2.0F);
        HealthContainer container = camera.getData(MedSystemDataAttachments.HEALTH_CONTAINER);
        HealthContainerDefinition definition = container.getDefinition();
        List<BodyPartDisplay> displays = definition.getDisplayConfiguration();
        for (BodyPartDisplay display : displays) {
            BodyPart health = container.getBodyPart(display.source());
            if (health == null)
                return;
            Vector4f pos = display.getGuiPosition(scale, center);
            int color = overlay.transparency << 24 | getColor(overlay.deadLimbColor, overlay.colorSchema, health);
            RenderUtils.fill(graphics, pos.x, pos.y, pos.x + pos.z, pos.y + pos.w, ARGB.scaleRGB(color, 0.8F));
            RenderUtils.fill(graphics, pos.x + 2, pos.y + 2, pos.x + pos.z - 2, pos.y + pos.w - 2, color);
        }

        Stream<StatusEffect> effectStream = container.getStatusEffectStream().filter(effect -> effect.getType().getVisibility().isVisibleInMode(EffectVisibility.ALWAYS));
        Map<StatusEffectType<?>, List<StatusEffect>> effects = effectStream.collect(Collectors.groupingBy(StatusEffect::getType, LinkedHashMap::new, Collectors.toList()));
        int index = 0;
        for (Map.Entry<StatusEffectType<?>, List<StatusEffect>> entry : effects.entrySet()) {
            StatusEffectType<?> type = entry.getKey();
            List<StatusEffect> effectList = entry.getValue();
            ResourceLocation icon = type.getIcon();
            int x = (int) (overlayPos.x() + overlayWidth);
            int y = (int) (overlayPos.y() + index++ * 12);

            RenderUtils.blitFull(graphics, icon, x, y, x + 12, y + 12, -1);
            int count = effectList.size();
            if (count > 1) {
                String text = String.valueOf(count);
                graphics.drawString(client.font, text, x + 12 - client.font.width(text), y + 4, ColorPalette.WHITE);
            }
        }
    }

    public static int getColor(String deadLimbColor, String[] colorSchema, BodyPart part) {
        if (part.isDead()) {
            return Integer.decode(deadLimbColor);
        }
        if (colorSchema.length == 0) {
            return 0xBBBBBB;
        }
        if (colorSchema.length == 1) {
            return Integer.decode(colorSchema[0]);
        }
        float percent = 1.0F - part.getHealthPercent();
        if (percent <= 0.0F) {
            return Integer.decode(colorSchema[0]);
        } else if (percent >= 1.0F) {
            return Integer.decode(colorSchema[colorSchema.length - 1]);
        }
        float step = 1.0F / (colorSchema.length - 1);
        int stepIndex = Mth.floor(percent / step);
        int baseColor = Integer.decode(colorSchema[stepIndex]);
        int transitionColor = Integer.decode(colorSchema[stepIndex + 1]);
        float transitionPercent = (percent - stepIndex * step) / step;
        return ARGB.lerp(transitionPercent, baseColor, transitionColor) & 0xFFFFFF;
    }
}
