package tnt.tarkovcraft.medsystem.common.damage_effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.damage_effect.condition.DamageEffectCondition;
import tnt.tarkovcraft.medsystem.common.damage_effect.condition.DamageEffectConditionType;
import tnt.tarkovcraft.medsystem.common.damage_effect.event.DamageEffectEvent;
import tnt.tarkovcraft.medsystem.common.damage_effect.event.DamageEffectEventType;
import tnt.tarkovcraft.medsystem.common.damage_effect.event.NoDamageEffectEvent;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public record DamageEffect(
        DamageEffectContextType contextType,
        List<DamageEffectCondition> conditions,
        DamageEffectEvent event,
        DamageEffectEvent clearEvent
) {

    public static final Codec<DamageEffect> CODEC = RecordCodecBuilder.<DamageEffect>create(instance -> instance.group(
            DamageEffectContextType.CODEC.fieldOf("context_type").forGetter(DamageEffect::contextType),
            DamageEffectConditionType.CODEC.listOf().optionalFieldOf("conditions", Collections.emptyList()).forGetter(DamageEffect::conditions),
            DamageEffectEventType.CODEC.fieldOf("event").forGetter(DamageEffect::event),
            DamageEffectEventType.CODEC.optionalFieldOf("clear_event", NoDamageEffectEvent.INSTANCE).forGetter(DamageEffect::clearEvent)
    ).apply(instance, DamageEffect::new)).validate(effect -> {
        DamageEffectContextType type = effect.contextType();
        try {
            for (DamageEffectCondition filter : effect.conditions) {
                filter.validate(type);
            }
            effect.event.validate(type);
            effect.clearEvent.validate(type);
        } catch (Exception e) {
            return DataResult.error(() -> "Failed to validate damage effect: " + e);
        }
        return DataResult.success(effect);
    });

    public void applyDamageEvent(LivingEntity entity, HealthContainer container, DamageContext damageContext, float totalDamage, Map<Limb, Float> damageDistribution, List<Limb> lostLimbs) {
        for (Map.Entry<Limb, Float> entry : damageDistribution.entrySet()) {
            Limb limb = entry.getKey();
            float damage = entry.getValue();
            DamageEffectContext damageCtx = new DamageEffectContext.ApplyDamageEffectContext(entity, container, limb,
                    damageContext, totalDamage, damage, damageDistribution, lostLimbs);
            if (!this.canApply(damageCtx)) {
                this.clearEvent.apply(damageCtx);
                continue;
            }
            this.event.apply(damageCtx);
        }
    }

    public void applyUpdateEvent(LivingEntity entity, HealthContainer container, Limb limb) {
        DamageEffectContext ctx = new DamageEffectContext.UpdateDamageEffectContext(entity, container, limb);
        if (this.canApply(ctx)) {
            this.event.apply(ctx);
        } else {
            this.clearEvent.apply(ctx);
        }
    }

    private boolean canApply(DamageEffectContext context) {
        for (DamageEffectCondition condition : this.conditions) {
            if (!condition.matches(context)) {
                return false;
            }
        }
        return true;
    }

    public static void validateContext(Object component, DamageEffectContextType currentCtx, DamageEffectContextType... validTypes) {
        boolean isValid = false;
        for (DamageEffectContextType type : validTypes) {
            if (currentCtx == type) {
                isValid = true;
                break;
            }
        }
        if (!isValid) {
            throw new IllegalArgumentException("Component '" + component.getClass().getSimpleName() + "' cannot be used in context '" + currentCtx + "'. Valid context types: " + Arrays.toString(validTypes));
        }
    }
}
