package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.medsystem.common.effect.group.EffectGroupHolder;
import tnt.tarkovcraft.medsystem.common.effect.util.EffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health.LimbContainer;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import java.util.List;

public class NegativeEffectProtectionStatusEffect extends IntervalAppliedStatusEffect {

    public static final MapCodec<NegativeEffectProtectionStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> common(instance)
            .apply(instance, NegativeEffectProtectionStatusEffect::new)
    );

    public NegativeEffectProtectionStatusEffect(int duration) {
        super(duration);
    }

    @Override
    public int getUpdateInterval() {
        return 20;
    }

    @Override
    public void applyEffect(StatusEffectContext context) {
        HealthContainer container = context.container();
        LimbContainer limbContainer = container.getLimbContainer();
        for (Limb limb : limbContainer) {
            StatusEffectMap effects = limb.getStatusEffects();
            for (StatusEffect effect : effects.listEffects()) {
                this.checkEffect(effect);
            }
        }
    }

    @Override
    public void onRemoved(StatusEffectContext context) {
        this.applyEffect(context);
    }

    @Override
    public StatusEffect copy() {
        return new NegativeEffectProtectionStatusEffect(this.getDuration());
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.NEGATIVE_EFFECT_PROTECTION.value();
    }

    private void checkEffect(StatusEffect effect) {
        StatusEffectType<?> type = effect.getType();
        if (type.getEffectType() != EffectType.NEGATIVE)
            return;
        if (effect instanceof GroupStatusEffect groupStatusEffect) {
            this.checkGroupEffect(groupStatusEffect);
        } else {
            this.checkNormalEffect(effect);
        }
    }

    private void checkNormalEffect(StatusEffect effect) {
        if (effect.isInfinite() || effect.getDuration() > 100) {
            effect.setDuration(100);
        }
    }

    private void checkGroupEffect(GroupStatusEffect effect) {
        List<EffectGroupHolder> items = effect.getItems();
        for (EffectGroupHolder holder : items) {
            if (holder.getDelay() > 0)
                continue;
            if (holder.getDuration() > 100) {
                holder.setDuration(100);
                effect.updateDuration();
            }
        }
    }
}
