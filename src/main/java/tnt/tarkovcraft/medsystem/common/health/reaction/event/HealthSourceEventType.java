package tnt.tarkovcraft.medsystem.common.health.reaction.event;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Objects;

public record HealthSourceEventType<R extends HealthSourceEvent>(ResourceLocation identifier, MapCodec<R> codec) {

    public static final Codec<HealthSourceEvent> CODEC = MedSystemRegistries.HEALTH_REACTION_RESPONSE.byNameCodec().dispatch(HealthSourceEvent::getType, HealthSourceEventType::codec);

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HealthSourceEventType<?> that)) return false;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }
}
