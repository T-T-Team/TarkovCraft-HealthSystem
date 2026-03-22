package tnt.tarkovcraft.medsystem.common.health;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.common.data.duration.Duration;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.BloodSystemListener;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystemDefinition;
import tnt.tarkovcraft.medsystem.common.effect.BloodImmuneReactionStatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.BloodLossStatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectHelper;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

public final class HealthBloodSystemIntegration implements BloodSystemListener {

    private final HealthContainer container;

    public HealthBloodSystemIntegration(HealthContainer container) {
        this.container = container;
    }

    @Override
    public void onBloodTick(float bloodVolume, LivingEntity entity, EntityBloodSystemDefinition definition) {
        BloodLossStatusEffect.Stage stage = definition.getBloodLossStage(bloodVolume / definition.getMaxBloodVolume());
        StatusEffectMap statusEffects = this.container.getGlobalStatusEffects();
        if (stage == null)
            return;
        BloodLossStatusEffect statusEffect = (BloodLossStatusEffect) statusEffects.getEffect(MedSystemStatusEffects.BLOODLOSS)
                .orElse(null);
        if (statusEffect == null || statusEffect.getStage() != stage) {
            BloodLossStatusEffect bloodLoss = BloodLossStatusEffect.createTemplate(stage);
            StatusEffectHelper.addGlobalEffect(statusEffects, entity, 1, bloodLoss);
        }
    }

    @Override
    public void onIncompatibleBloodTransfusion(LivingEntity entity, Identifier bloodType, Identifier receivedBloodType, float receivedVolume) {
        StatusEffectMap effects = this.container.getGlobalStatusEffects();
        int delay = Duration.minutes(5).tickValue();
        StatusEffectHelper.addGlobalEffect(effects, entity, delay, BloodImmuneReactionStatusEffect.createDefault());
    }
}
