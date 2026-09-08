package tnt.tarkovcraft.medsystem.integration.core;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import tnt.tarkovcraft.core.client.hint.KeybindOnScreenHint;
import tnt.tarkovcraft.medsystem.client.MedicalSystemClient;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;

public final class GiveUpOnScreenHint extends KeybindOnScreenHint {

    public GiveUpOnScreenHint() {
        super(MedicalSystemClient.KEY_GIVE_UP);
    }

    @Override
    public void tick(Minecraft client) {
        Player player = client.player;
        this.setVisible(BloodSystemManager.canSkipUnconsciousMode(player));
    }
}
