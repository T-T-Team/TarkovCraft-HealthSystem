package tnt.tarkovcraft.medsystem.common.health_event.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Objects;

public record HealthEventActionType<E extends HealthEventAction>(ResourceLocation identifier, MapCodec<E> codec) {

    public static final Codec<HealthEventAction> CODEC = MedSystemRegistries.HEALTH_EVENT_ACTION.byNameCodec()
            .dispatch(HealthEventAction::getType, HealthEventActionType::codec);

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HealthEventActionType<?> that)) return false;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }
}
