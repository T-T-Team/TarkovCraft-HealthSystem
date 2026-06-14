package tnt.tarkovcraft.medsystem.common.health_event.action;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.medsystem.common.config.BleedConfiguration;
import tnt.tarkovcraft.medsystem.common.effect.BleedStatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectHelper;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectSubmitter;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventParams;
import tnt.tarkovcraft.medsystem.util.WeightedList;

import java.util.Arrays;
import java.util.List;

public final class AddBleedEventAction implements HealthEventAction {

    public static final AddBleedEventAction INSTANCE = new AddBleedEventAction();
    public static final MapCodec<AddBleedEventAction> CODEC = MapCodec.unit(INSTANCE);

    private AddBleedEventAction() {
    }

    @Override
    public boolean apply(HealthEventContext ctx) {
        DamageContext context = ctx.getParameter(HealthEventParams.DAMAGE_CONTEXT);
        if (context == null)
            return false;
        float damageAmount = ctx.getParameterOrDefault(HealthEventParams.DAMAGE_AMOUNT, 0.0F);
        List<BleedStatusEffect.BleedType> bleedTypes = Arrays.stream(BleedStatusEffect.BleedType.values())
                .filter(type -> canUseBleedStage(type, damageAmount))
                .toList();
        if (bleedTypes.isEmpty()) {
            return true;
        }
        WeightedList.Builder<BleedStatusEffect.BleedType> builder = WeightedList.builder();
        bleedTypes.forEach(type -> builder.add(type, type.getConfig().weight));
        WeightedList<BleedStatusEffect.BleedType> bleedTypeWeights = builder.build();
        Limb limb = ctx.getLimb();
        BleedStatusEffect.BleedType bleedType = bleedTypeWeights.getRandomOrThrow(ctx.getEntity().getRandom());
        BleedConfiguration.BleedStageConfig stageConfig = bleedType.getConfig();
        StatusEffectSubmitter submitter = limb.getStatusEffects().getEffectSubmitter();

        BleedStatusEffect bleedStatusEffect = BleedStatusEffect.createTemplate(stageConfig.bleedDuration, bleedType);
        StatusEffectHelper.setCausingEntityFromSource(bleedStatusEffect, context.getSource());
        submitter.submitImmediate(bleedStatusEffect);
        return true;
    }

    @Override
    public MapCodec<? extends HealthEventAction> codec() {
        return CODEC;
    }

    private boolean canUseBleedStage(BleedStatusEffect.BleedType type, float damage) {
        BleedConfiguration.BleedStageConfig config = type.getConfig();
        return config.weight > 0 && damage >= config.minDamageThreshold;
    }
}
