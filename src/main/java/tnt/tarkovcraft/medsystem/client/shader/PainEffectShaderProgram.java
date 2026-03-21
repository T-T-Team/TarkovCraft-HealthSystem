package tnt.tarkovcraft.medsystem.client.shader;

import net.minecraft.core.Holder;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

public final class PainEffectShaderProgram extends StatusEffectShaderProgram {

    public static final PainEffectShaderProgram INSTANCE = new PainEffectShaderProgram();

    private PainEffectShaderProgram() {}

    @Override
    protected Holder<StatusEffectType<?>> getStatusEffect() {
        return MedSystemStatusEffects.PAIN;
    }

    @Override
    protected float getGain() {
        return 0.045F;
    }

    @Override
    protected float getDecay() {
        return 0.015F;
    }
}
