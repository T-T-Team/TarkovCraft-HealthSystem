package tnt.tarkovcraft.medsystem.common.health_event.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.tags.TagKey;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.LimbContainer;
import tnt.tarkovcraft.medsystem.common.health.LimbType;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventResult;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Optional;

public record HasStatusEffectEventCondition(TagKey<StatusEffectType<?>> tag, Optional<LimbType> limbType) implements HealthEventCondition {

    public static final MapCodec<HasStatusEffectEventCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TagKey.codec(MedSystemRegistries.Keys.STATUS_EFFECT).fieldOf("tag").forGetter(t -> t.tag),
            LimbType.CODEC.optionalFieldOf("limb").forGetter(t -> t.limbType)
    ).apply(instance, HasStatusEffectEventCondition::new));

    @Override
    public HealthEventResult test(HealthEventContext ctx) {
        HealthContainer container = ctx.getHealthContainer();
        LimbContainer limbContainer = container.getLimbContainer();
        return HealthEventResult.condition(
                this.limbType.map(limbType -> limbContainer.getLimbsByType(limbType).anyMatch(limb -> limb.getStatusEffects().hasEffect(this.tag)))
                        .orElseGet(() -> container.getGlobalStatusEffects().hasEffect(this.tag))
        );
    }

    @Override
    public MapCodec<? extends HealthEventCondition> codec() {
        return CODEC;
    }
}
