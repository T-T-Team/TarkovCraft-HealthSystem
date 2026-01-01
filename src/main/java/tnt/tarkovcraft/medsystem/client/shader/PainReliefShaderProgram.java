package tnt.tarkovcraft.medsystem.client.shader;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

public final class PainReliefShaderProgram extends StatusEffectShaderProgram {

    public static final PainReliefShaderProgram INSTANCE = new PainReliefShaderProgram();

    private PainReliefShaderProgram() {}

    @Override
    protected Holder<StatusEffectType<?>> getStatusEffect() {
        return MedSystemStatusEffects.PAIN_RELIEF;
    }

    @Override
    protected float getGain() {
        return 0.005F;
    }

    @Override
    protected float getDecay() {
        return 0.005F;
    }

    @Override
    protected boolean canApply(LivingEntity entity, HealthContainer container, StatusEffectMap map) {
        return !BloodlossShaderProgram.INSTANCE.shouldRender();
    }
}
