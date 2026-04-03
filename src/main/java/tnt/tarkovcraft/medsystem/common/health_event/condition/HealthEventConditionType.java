package tnt.tarkovcraft.medsystem.common.health_event.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Objects;

public record HealthEventConditionType<F extends HealthEventCondition>(Identifier identifier, MapCodec<F> codec) {

    public static final Codec<HealthEventCondition> CODEC = MedSystemRegistries.HEALTH_EVENT_CONDITION.byNameCodec()
            .dispatch(HealthEventCondition::getType, HealthEventConditionType::codec);

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HealthEventConditionType<?> that)) return false;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }
}
