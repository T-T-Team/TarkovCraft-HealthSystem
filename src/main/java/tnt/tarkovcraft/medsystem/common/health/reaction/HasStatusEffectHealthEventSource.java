package tnt.tarkovcraft.medsystem.common.health.reaction;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.core.util.context.ContextKeys;
import tnt.tarkovcraft.medsystem.common.MedicalSystemContextKeys;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthReactions;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

public class HasStatusEffectHealthEventSource implements HealthEventSource {

    public static final MapCodec<HasStatusEffectHealthEventSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            MedSystemRegistries.STATUS_EFFECT.holderByNameCodec().fieldOf("effect").forGetter(t -> t.type)
    ).apply(instance, HasStatusEffectHealthEventSource::new));

    private final Holder<StatusEffectType<?>> type;

    public HasStatusEffectHealthEventSource(Holder<StatusEffectType<?>> type) {
        this.type = type;
    }

    @Override
    public boolean canReact(Context context) {
        return context.get(ContextKeys.LIVING_ENTITY).map(entity -> {
            StatusEffectType<?> effectType = this.type.value();
            BodyPart part = context.getOrDefault(MedicalSystemContextKeys.BODY_PART, null);
            if (effectType.isGlobalEffect()) {
                HealthContainer container = context.getOrThrow(MedicalSystemContextKeys.HEALTH_CONTAINER);
                return container.getGlobalStatusEffects().hasEffect(this.type);
            } else {
                if (part == null) {
                    return false;
                }
                return part.getStatusEffects().hasEffect(this.type);
            }
        }).orElse(false);
    }

    @Override
    public HealthEventSourceType<?> getType() {
        return MedSystemHealthReactions.HAS_EFFECT.get();
    }
}
