package tnt.tarkovcraft.medsystem.client.overlay;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.gui.GuiLayer;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;

public class UnconsciousLayer implements GuiLayer {

    public static final ResourceLocation LAYER_ID = MedicalSystem.resource("layer/unconscious");
    public static final Component TEXT = Component.translatable("label.medsystem.unconscious.info");

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        Font font = client.font;
        if (client.options.hideGui)
            return;
        if (player.isSpectator() || player.isCreative())
            return;
        Window window = client.getWindow();

        if (BloodSystem.isEntityUnconscious(player)) {
            int textWidth = font.width(TEXT);
            guiGraphics.drawString(font, TEXT, (window.getGuiScaledWidth() - textWidth) / 2, 30, 0xFFFFFFFF, true);
        }
    }
}
