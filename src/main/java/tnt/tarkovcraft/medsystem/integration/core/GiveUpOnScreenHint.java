package tnt.tarkovcraft.medsystem.integration.core;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import tnt.tarkovcraft.core.client.hint.KeybindOnScreenHint;
import tnt.tarkovcraft.medsystem.client.MedicalSystemClient;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;

public class GiveUpOnScreenHint extends KeybindOnScreenHint {

    private boolean active;

    public GiveUpOnScreenHint() {
        super(MedicalSystemClient.KEY_GIVE_UP);
    }

    @Override
    public void onHintUpdate() {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        this.active = BloodSystem.canGiveUp(player);
    }

    @Override
    public boolean isDisabled() {
        return !this.active;
    }
}
