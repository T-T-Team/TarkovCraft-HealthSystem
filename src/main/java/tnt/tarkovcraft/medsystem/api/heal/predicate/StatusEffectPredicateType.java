package tnt.tarkovcraft.medsystem.api.heal.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Objects;

public record StatusEffectPredicateType<P extends StatusEffectPredicate>(Identifier identifier, MapCodec<P> codec) {

    public static final Codec<StatusEffectPredicate> CODEC = MedSystemRegistries.STATUS_EFFECT_PREDICATE.byNameCodec().dispatch(StatusEffectPredicate::getType, StatusEffectPredicateType::codec);

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StatusEffectPredicateType<?> that)) return false;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }

    @Override
    public String toString() {
        return this.identifier.toString();
    }
}
