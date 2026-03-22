package tnt.tarkovcraft.medsystem.client.shader;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.client.shader.ShaderHelper;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

public class PainReliefEffectShaderProgram extends SimpleEffectShaderProgram {

    private static final Identifier IDENTIFIER = MedicalSystem.createIdentifier("pain_relief");
    public static final Identifier PIPELINE = ShaderHelper.getPostChainPipeline(IDENTIFIER, 0);

    @Override
    public Holder<StatusEffectType<?>> getEffect() {
        return MedSystemStatusEffects.PAIN_RELIEF;
    }

    @Override
    public float getStrengthGain() {
        return 0.005F;
    }

    @Override
    public float getStrengthDecay() {
        return 0.005F;
    }

    @Override
    public Identifier postChainId() {
        return IDENTIFIER;
    }

    @Override
    protected boolean canApply(LivingEntity entity, HealthContainer container, StatusEffectMap map) {
        return !map.hasEffect(MedSystemStatusEffects.BLOODLOSS);
    }
}
