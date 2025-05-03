package tnt.tarkovcraft.medsystem.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.network.message.C2S_SelectBodyPart;
import tnt.tarkovcraft.medsystem.network.message.S2C_OpenBodyPartSelectScreen;

import java.util.Locale;

public final class MedicalSystemNetwork {

    public static final int VERSION = 1;
    public static final String NETWORK_ID = "MedicalSystemNetwork@" + VERSION;

    public static ResourceLocation createId(Class<? extends CustomPacketPayload> type) {
        String name = type.getSimpleName().toLowerCase(Locale.ROOT);
        return MedicalSystem.resource("net/" + name);
    }

    public static void onRegistration(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registry = event.registrar(NETWORK_ID).executesOn(HandlerThread.MAIN);

        registry.playToClient(S2C_OpenBodyPartSelectScreen.TYPE, S2C_OpenBodyPartSelectScreen.CODEC, S2C_OpenBodyPartSelectScreen::handleMessage);

        registry.playToServer(C2S_SelectBodyPart.TYPE, C2S_SelectBodyPart.CODEC, C2S_SelectBodyPart::handleMessage);
    }
}
