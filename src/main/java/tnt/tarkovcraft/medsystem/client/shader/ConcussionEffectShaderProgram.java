package tnt.tarkovcraft.medsystem.client.shader;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.EasingType;
import tnt.tarkovcraft.core.api.shader.ShaderType;
import tnt.tarkovcraft.core.client.shader.ShaderHelper;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import java.util.function.Consumer;

public class ConcussionEffectShaderProgram extends SimpleEffectShaderProgram {

    private static final Identifier IDENTIFIER = MedicalSystem.createIdentifier("concussion");
    public static final Identifier PIPELINE = ShaderHelper.getPostChainPipeline(IDENTIFIER, 0);

    @Override
    public Holder<StatusEffectType<?>> getEffect() {
        return MedSystemStatusEffects.CONCUSSION;
    }

    @Override
    public float getStrengthGain() {
        return 1.0F;
    }

    @Override
    public float getStrengthDecay() {
        return 0.01F;
    }

    @Override
    public Identifier postChainId() {
        return IDENTIFIER;
    }

    @Override
    public ShaderType getShaderType() {
        return ShaderType.COSMETIC;
    }

    @Override
    protected float applySmoothing(float start, float end, float delta) {
        return EasingType.IN_OUT_SINE.apply(super.applySmoothing(start, end, delta));
    }

    @Override
    public void applyDynamicUniforms(Consumer<Identifier> passIdentifierConsumer) {
        passIdentifierConsumer.accept(PIPELINE);
    }
}
