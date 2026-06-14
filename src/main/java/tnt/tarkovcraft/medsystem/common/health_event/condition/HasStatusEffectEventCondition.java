package tnt.tarkovcraft.medsystem.common.health_event.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.tags.TagKey;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventResult;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

public record HasStatusEffectEventCondition(TagKey<StatusEffectType<?>> tag) implements HealthEventCondition {

    public static final MapCodec<HasStatusEffectEventCondition> CODEC = TagKey.codec(MedSystemRegistries.Keys.STATUS_EFFECT)
            .xmap(HasStatusEffectEventCondition::new, HasStatusEffectEventCondition::tag).fieldOf("tag");

    @Override
    public HealthEventResult test(HealthEventContext ctx) {
        HealthContainer container = ctx.getHealthContainer();
        Limb limb = ctx.getLimb();
        return HealthEventResult.condition(container.getGlobalStatusEffects().hasEffect(this.tag)
                || limb.getStatusEffects().hasEffect(this.tag));
    }

    @Override
    public MapCodec<? extends HealthEventCondition> codec() {
        return CODEC;
    }
}
