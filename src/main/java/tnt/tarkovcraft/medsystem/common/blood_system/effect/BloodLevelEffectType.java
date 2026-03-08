package tnt.tarkovcraft.medsystem.common.blood_system.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Objects;

public record BloodLevelEffectType<E extends BloodLevelEffect>(Identifier identifier, MapCodec<E> codec) {

    public static final Codec<BloodLevelEffect> CODEC = MedSystemRegistries.BLOOD_LEVEL_EFFECT.byNameCodec()
            .dispatch(BloodLevelEffect::getType, BloodLevelEffectType::codec);

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BloodLevelEffectType<?> that)) return false;
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
