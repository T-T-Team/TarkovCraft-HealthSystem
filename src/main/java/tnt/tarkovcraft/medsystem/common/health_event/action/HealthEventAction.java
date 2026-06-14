package tnt.tarkovcraft.medsystem.common.health_event.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.function.Function;

public interface HealthEventAction {

    Codec<HealthEventAction> CODEC = MedSystemRegistries.HEALTH_EVENT_ACTION.byNameCodec()
            .dispatch(HealthEventAction::codec, Function.identity());

    boolean apply(HealthEventContext ctx);

    MapCodec<? extends HealthEventAction> codec();
}
