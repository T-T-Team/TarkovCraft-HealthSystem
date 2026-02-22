package tnt.tarkovcraft.medsystem.common.effect.event.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Objects;

public record StatusEffectEventConditionType<F extends StatusEffectEventCondition>(Identifier identifier, MapCodec<F> codec) {

    public static final Codec<StatusEffectEventCondition> CODEC = MedSystemRegistries.STATUS_EFFECT_EVENT_CONDITION.byNameCodec()
            .dispatch(StatusEffectEventCondition::getType, StatusEffectEventConditionType::codec);

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StatusEffectEventConditionType<?> that)) return false;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }
}
