package tnt.tarkovcraft.medsystem.common.damage_effect.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Objects;

public record DamageEffectConditionType<F extends DamageEffectCondition>(ResourceLocation identifier, MapCodec<F> codec) {

    public static final Codec<DamageEffectCondition> CODEC = MedSystemRegistries.DAMAGE_EFFECT_CONDITION.byNameCodec().dispatch(DamageEffectCondition::getType, DamageEffectConditionType::codec);

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DamageEffectConditionType<?> that)) return false;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }
}
