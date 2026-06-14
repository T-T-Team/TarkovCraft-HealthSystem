package tnt.tarkovcraft.medsystem.common.health_event.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventResult;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.function.Function;

public interface HealthEventCondition {

    Codec<HealthEventCondition> CODEC = MedSystemRegistries.HEALTH_EVENT_CONDITION.byNameCodec()
            .dispatch(HealthEventCondition::codec, Function.identity());

    HealthEventResult test(HealthEventContext ctx);

    MapCodec<? extends HealthEventCondition> codec();
}
