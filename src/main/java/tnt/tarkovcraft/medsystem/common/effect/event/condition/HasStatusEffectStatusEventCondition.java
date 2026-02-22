package tnt.tarkovcraft.medsystem.common.effect.event.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.tags.TagKey;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;
import tnt.tarkovcraft.medsystem.common.effect.event.TriggerResult;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectEventConditions;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

public record HasStatusEffectStatusEventCondition(TagKey<StatusEffectType<?>> tag) implements StatusEffectEventCondition {

    public static final MapCodec<HasStatusEffectStatusEventCondition> CODEC = TagKey.codec(MedSystemRegistries.Keys.STATUS_EFFECT)
            .xmap(HasStatusEffectStatusEventCondition::new, HasStatusEffectStatusEventCondition::tag).fieldOf("tag");

    @Override
    public TriggerResult test(StatusEffectEventContext ctx) {
        HealthContainer container = ctx.getHealthContainer();
        Limb limb = ctx.getLimb();
        return TriggerResult.condition(container.getGlobalStatusEffects().hasEffect(this.tag)
                || limb.getStatusEffects().hasEffect(this.tag));
    }

    @Override
    public StatusEffectEventConditionType<?> getType() {
        return MedSystemStatusEffectEventConditions.HAS_EFFECT.value();
    }
}
