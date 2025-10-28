package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectSubmitter;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;
import tnt.tarkovcraft.medsystem.common.status.BloodData;
import tnt.tarkovcraft.medsystem.common.status.BloodStatus;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;

import javax.annotation.Nullable;

public class MildBloodLossStatusEffect extends IntervalAppliedStatusEffect {

    public static final MapCodec<MildBloodLossStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> common(instance).apply(instance, MildBloodLossStatusEffect::new));

    public MildBloodLossStatusEffect() {
        super(-1);
    }

    private MildBloodLossStatusEffect(int duration) {
        super(duration);
    }

    @Override
    public int getUpdateInterval() {
        return 20;
    }

    @Override
    public void applyEffect(HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
        if (!BloodSystem.hasBloodDataIntegration(entity)) {
            this.markForRemoval();
            return;
        }
        BloodData data = BloodSystem.getBloodData(entity);
        float percentage = data.getBloodVolumePercentage();
        if (BloodStatus.MODERATE_BLOOD_LOSS.isInRange(percentage) || !BloodStatus.MILD_BLOOD_LOSS.isInRange(percentage)) {
            this.markForRemoval();
        }
    }

    @Override
    public void onRemoved(StatusEffectSubmitter submitter, HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
    }

    @Override
    public StatusEffect copy() {
        return new MildBloodLossStatusEffect(this.getDuration());
    }

    @Override
    public boolean hasVisibleDuration() {
        return false;
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.MILD_BLOODLOSS.value();
    }
}
