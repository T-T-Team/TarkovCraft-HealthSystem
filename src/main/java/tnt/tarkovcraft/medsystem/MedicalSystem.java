package tnt.tarkovcraft.medsystem;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

@Mod(MedicalSystem.MOD_ID)
public final class MedicalSystem {

    public static final String MOD_ID = "medsystem";
    public static final Logger LOGGER = LogManager.getLogger("TarkovCraftMedicalSystem");
    public static final Marker MARKER = MarkerManager.getMarker("MedicalSystem");

    public MedicalSystem(IEventBus modEventBus, ModContainer container) {

    }
}
