package tnt.tarkovcraft.medsystem.client.shader;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.EasingType;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

public class ConcussionEffectShaderProgram extends SimpleEffectShaderProgram {

    public static final Identifier PIPELINE = MedicalSystem.createIdentifier("concussion/0");
    private static final Identifier IDENTIFIER = MedicalSystem.createIdentifier("concussion");

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
    protected float applySmoothing(float start, float end, float delta) {
        return EasingType.IN_OUT_SINE.apply(super.applySmoothing(start, end, delta));
    }
}
