package tnt.tarkovcraft.medsystem.common.health_event.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventParams;
import tnt.tarkovcraft.medsystem.common.health_event.function.HealthEventFunction;
import tnt.tarkovcraft.medsystem.common.health_event.function.HealthEventFunctionType;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectHelper;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectWithDelay;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthEventActions;

import java.util.Collections;
import java.util.List;

public record AddStatusEffectEventAction(StatusEffectWithDelay effect, List<HealthEventFunction> durationModifiers, List<HealthEventFunction> delayModifiers) implements HealthEventAction {

    public static final MapCodec<AddStatusEffectEventAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            StatusEffectWithDelay.CODEC.fieldOf("effect").forGetter(AddStatusEffectEventAction::effect),
            HealthEventFunctionType.CODEC.listOf().optionalFieldOf("duration_modifiers", Collections.emptyList()).forGetter(AddStatusEffectEventAction::durationModifiers),
            HealthEventFunctionType.CODEC.listOf().optionalFieldOf("delay_modifiers", Collections.emptyList()).forGetter(AddStatusEffectEventAction::delayModifiers)
    ).apply(instance, AddStatusEffectEventAction::new));

    @Override
    public boolean apply(HealthEventContext ctx) {
        DamageContext context = ctx.getParameter(HealthEventParams.DAMAGE_CONTEXT);
        StatusEffect statusEffect = this.effect.createInstance();
        int duration = Mth.floor(HealthEventFunctionType.applyFunctions(statusEffect.getDuration(), ctx, this.durationModifiers));
        statusEffect.setDuration(duration);
        if (context != null)
            StatusEffectHelper.setCausingEntityFromSource(statusEffect, context.getSource());
        boolean isGlobalEffect = statusEffect.getType().isGlobalEffect();
        Limb targetLimb = isGlobalEffect ? null : ctx.getLimb();
        if (!isGlobalEffect && !targetLimb.canApplyStatusEffect(statusEffect.getType()))
            return true;
        // at least 1 tick delay is required to prevent CMEs while ticking
        int delay = Math.max(1, Mth.floor(HealthEventFunctionType.applyFunctions(this.effect.getDelay(), ctx, this.delayModifiers)));
        StatusEffectMap effects = isGlobalEffect ? ctx.getHealthContainer().getGlobalStatusEffects() : targetLimb.getStatusEffects();
        StatusEffectHelper.addEffect(effects, ctx.getEntity(), targetLimb, delay, statusEffect);
        return true;
    }

    @Override
    public HealthEventActionType<?> getType() {
        return MedSystemHealthEventActions.ADD_STATUS_EFFECT.value();
    }
}
