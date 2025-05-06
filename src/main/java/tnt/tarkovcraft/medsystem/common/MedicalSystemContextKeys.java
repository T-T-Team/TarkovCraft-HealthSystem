package tnt.tarkovcraft.medsystem.common;

import net.minecraft.util.context.ContextKey;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;

public final class MedicalSystemContextKeys {

    public static final ContextKey<HealthContainer> HEALTH_CONTAINER = new ContextKey<>(MedicalSystem.resource("health_container"));
    public static final ContextKey<BodyPart> BODY_PART = new ContextKey<>(MedicalSystem.resource("body_part"));
}
