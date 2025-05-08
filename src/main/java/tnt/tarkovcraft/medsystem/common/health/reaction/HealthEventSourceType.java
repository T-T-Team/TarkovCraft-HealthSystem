package tnt.tarkovcraft.medsystem.common.health.reaction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Objects;

public record HealthEventSourceType<R extends HealthEventSource>(ResourceLocation identifier, MapCodec<R> codec) {

    public static final Codec<HealthEventSource> CODEC = MedSystemRegistries.HEALTH_REACTION.byNameCodec().dispatch(HealthEventSource::getType, HealthEventSourceType::codec);

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HealthEventSourceType<?> that)) return false;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }
}
