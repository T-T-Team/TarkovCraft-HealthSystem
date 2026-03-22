package tnt.tarkovcraft.medsystem.client.shader;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

public class PainReliefEffectShaderProgram extends StatusEffectShaderProgram {

    public static final PainReliefEffectShaderProgram INSTANCE = new PainReliefEffectShaderProgram();
    private static final ResourceLocation IDENTIFIER = MedicalSystem.resource("pain_relief");

    private PainReliefEffectShaderProgram() {}

    @Override
    public Holder<StatusEffectType<?>> getStatusEffect() {
        return MedSystemStatusEffects.PAIN_RELIEF;
    }

    @Override
    public float getGain() {
        return 0.005F;
    }

    @Override
    public float getDecay() {
        return 0.005F;
    }

    @Override
    public ResourceLocation postChainId() {
        return IDENTIFIER;
    }

    @Override
    protected boolean canApply(LivingEntity entity, HealthContainer container, StatusEffectMap map) {
        return !map.hasEffect(MedSystemStatusEffects.BLOODLOSS);
    }
}
