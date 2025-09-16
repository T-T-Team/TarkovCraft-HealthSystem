package tnt.tarkovcraft.medsystem.client.overlay;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.gui.GuiLayer;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.status.BloodData;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;

import java.util.Locale;

public class UnconsciousLayer implements GuiLayer {

    public static final ResourceLocation LAYER_ID = MedicalSystem.resource("layer/unconscious");

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        Entity camera = client.cameraEntity;
        Font font = client.font;
        if (camera == null || client.options.hideGui)
            return;
        if (player.isSpectator() && player == camera)
            return;
        if (player.isCreative()) {
            return;
        }
        Window window = client.getWindow();

        BloodData bloodData = BloodSystem.getBloodData(player); // TODO camera entity support
        float volume = bloodData.getBloodVolume();
        float maxVolume = bloodData.getMaxBloodVolume();

        guiGraphics.drawString(font, String.format(Locale.ROOT, "%.2f/%.2f", volume, maxVolume), 10, 10, 0xFFFFFFFF, true);
        if (bloodData.isUnconscious()) {
            guiGraphics.drawString(font, "Unconscious", 10, 20, 0xFFFFFFFF, true);
        }
    }
}
