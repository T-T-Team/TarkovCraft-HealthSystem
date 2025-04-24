package tnt.tarkovcraft.medsystem.client.screen;

import tnt.tarkovcraft.core.client.screen.CharacterSubScreen;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.core.util.context.ContextKeys;
import tnt.tarkovcraft.medsystem.client.MedicalSystemClient;

public class HealthScreen extends CharacterSubScreen {

    public HealthScreen(Context context) {
        super(context.getOrThrow(ContextKeys.UUID), MedicalSystemClient.HEALTH);
    }

    @Override
    protected void init() {
        super.init();

    }
}
