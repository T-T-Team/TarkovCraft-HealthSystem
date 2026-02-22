package tnt.tarkovcraft.medsystem.common.effect.event.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Objects;

public record StatusEffectEventActionType<E extends StatusEffectEventAction>(ResourceLocation identifier, MapCodec<E> codec) {

    public static final Codec<StatusEffectEventAction> CODEC = MedSystemRegistries.STATUS_EFFECT_EVENT_ACTION.byNameCodec()
            .dispatch(StatusEffectEventAction::getType, StatusEffectEventActionType::codec);

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StatusEffectEventActionType<?> that)) return false;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }
}
