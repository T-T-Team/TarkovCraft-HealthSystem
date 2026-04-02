package tnt.tarkovcraft.medsystem.common.effect.event.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventParams;
import tnt.tarkovcraft.medsystem.common.effect.event.function.StatusEffectEventFunction;
import tnt.tarkovcraft.medsystem.common.effect.event.function.StatusEffectEventFunctionType;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectHelper;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectWithDelay;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectEventActions;

import java.util.Collections;
import java.util.List;

public record AddStatusEffectStatusEffectEventAction(StatusEffectWithDelay effect, List<StatusEffectEventFunction> durationModifiers, List<StatusEffectEventFunction> delayModifiers) implements StatusEffectEventAction {

    public static final MapCodec<AddStatusEffectStatusEffectEventAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            StatusEffectWithDelay.CODEC.fieldOf("effect").forGetter(AddStatusEffectStatusEffectEventAction::effect),
            StatusEffectEventFunctionType.CODEC.listOf().optionalFieldOf("duration_modifiers", Collections.emptyList()).forGetter(AddStatusEffectStatusEffectEventAction::durationModifiers),
            StatusEffectEventFunctionType.CODEC.listOf().optionalFieldOf("delay_modifiers", Collections.emptyList()).forGetter(AddStatusEffectStatusEffectEventAction::delayModifiers)
    ).apply(instance, AddStatusEffectStatusEffectEventAction::new));

    @Override
    public boolean apply(StatusEffectEventContext ctx) {
        DamageContext context = ctx.getParameter(StatusEffectEventParams.DAMAGE_CONTEXT);
        StatusEffect statusEffect = this.effect.createInstance();
        int duration = Mth.floor(StatusEffectEventFunctionType.applyFunctions(statusEffect.getDuration(), ctx, this.durationModifiers));
        statusEffect.setDuration(duration);
        if (context != null)
            StatusEffectHelper.setCausingEntityFromSource(statusEffect, context.getSource());
        boolean isGlobalEffect = statusEffect.getType().isGlobalEffect();
        Limb targetLimb = isGlobalEffect ? null : ctx.getLimb();
        if (!isGlobalEffect && !targetLimb.canApplyStatusEffect(statusEffect.getType()))
            return true;
        // at least 1 tick delay is required to prevent CMEs while ticking
        int delay = Math.max(1, Mth.floor(StatusEffectEventFunctionType.applyFunctions(this.effect.getDelay(), ctx, this.delayModifiers)));
        StatusEffectMap effects = isGlobalEffect ? ctx.getHealthContainer().getGlobalStatusEffects() : targetLimb.getStatusEffects();
        StatusEffectHelper.addEffect(effects, ctx.getEntity(), targetLimb, delay, statusEffect);
        return true;
    }

    @Override
    public StatusEffectEventActionType<?> getType() {
        return MedSystemStatusEffectEventActions.ADD_STATUS_EFFECT.value();
    }
}
