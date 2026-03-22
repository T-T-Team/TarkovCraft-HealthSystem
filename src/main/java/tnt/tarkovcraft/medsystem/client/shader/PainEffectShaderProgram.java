package tnt.tarkovcraft.medsystem.client.shader;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import tnt.tarkovcraft.core.client.shader.ShaderHelper;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

public class PainEffectShaderProgram extends SimpleEffectShaderProgram {

    private static final Identifier IDENTIFIER = MedicalSystem.createIdentifier("pain");
    public static final Identifier PIPELINE = ShaderHelper.getPostChainPipeline(IDENTIFIER, 0);
    private static final float DECAY_RATE = 0.015F;

    @Override
    public Holder<StatusEffectType<?>> getEffect() {
        return MedSystemStatusEffects.PAIN;
    }

    @Override
    public float getStrengthGain() {
        return 3 * DECAY_RATE;
    }

    @Override
    public float getStrengthDecay() {
        return DECAY_RATE;
    }

    @Override
    public Identifier postChainId() {
        return IDENTIFIER;
    }
}
