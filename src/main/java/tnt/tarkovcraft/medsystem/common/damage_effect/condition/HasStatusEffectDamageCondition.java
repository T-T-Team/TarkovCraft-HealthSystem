package tnt.tarkovcraft.medsystem.common.damage_effect.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.tags.TagKey;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContext;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDamageEffectConditions;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

public record HasStatusEffectDamageCondition(TagKey<StatusEffectType<?>> tag) implements DamageEffectCondition {

    public static final MapCodec<HasStatusEffectDamageCondition> CODEC = TagKey.codec(MedSystemRegistries.Keys.STATUS_EFFECT)
            .xmap(HasStatusEffectDamageCondition::new, HasStatusEffectDamageCondition::tag).fieldOf("tag");

    @Override
    public boolean matches(DamageEffectContext context) {
        HealthContainer container = context.health();
        Limb limb = context.limb();
        return container.getGlobalStatusEffects().hasEffect(this.tag) || limb.getStatusEffects().hasEffect(this.tag);
    }

    @Override
    public DamageEffectConditionType<?> getType() {
        return MedSystemDamageEffectConditions.HAS_EFFECT.value();
    }
}
