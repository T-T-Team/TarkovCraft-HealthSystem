package tnt.tarkovcraft.medsystem.common.damage_effect.event;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Objects;

public record DamageEffectEventType<E extends DamageEffectEvent>(ResourceLocation identifier, MapCodec<E> codec) {

    public static final Codec<DamageEffectEvent> CODEC = MedSystemRegistries.DAMAGE_EFFECT_EVENT.byNameCodec().dispatch(DamageEffectEvent::getType, DamageEffectEventType::codec);

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DamageEffectEventType<?> that)) return false;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }
}
