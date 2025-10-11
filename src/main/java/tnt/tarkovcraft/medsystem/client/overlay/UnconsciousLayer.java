package tnt.tarkovcraft.medsystem.client.overlay;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.gui.GuiLayer;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.status.BloodData;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;

import java.util.List;

public class UnconsciousLayer implements GuiLayer {

    public static final ResourceLocation LAYER_ID = MedicalSystem.resource("layer/unconscious");

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        Font font = client.font;
        if (client.options.hideGui)
            return;
        if (player.isSpectator())
            return;
        Window window = client.getWindow();

        BloodData data = BloodSystem.getBloodData(player);
        if (data.isUnconscious()) {
            BloodData.UnconsciousInfo info = data.getUnconsciousInfo();
            Component reason = info.reason();
            List<FormattedCharSequence> lines = font.split(reason, window.getGuiScaledWidth() / 3 * 2);
            for (int i = 0; i < lines.size(); i++) {
                FormattedCharSequence line = lines.get(i);
                int textWidth = font.width(line);
                guiGraphics.drawString(font, line, (window.getGuiScaledWidth() - textWidth) / 2, 30 + i * 11, 0xFFFFFFFF, true);
            }
        }
    }
}
