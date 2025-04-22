package tnt.tarkovcraft.medsystem.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import tnt.tarkovcraft.medsystem.MedicalSystem;

@Mod(value = MedicalSystem.MOD_ID, dist = Dist.CLIENT)
public final class MedicalSystemClient {

    public MedicalSystemClient(IEventBus modEventBus, ModContainer container) {

    }
}
