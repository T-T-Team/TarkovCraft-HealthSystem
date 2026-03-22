package tnt.tarkovcraft.medsystem.client.shader;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

public final class PainEffectShaderProgram extends StatusEffectShaderProgram {

    public static final PainEffectShaderProgram INSTANCE = new PainEffectShaderProgram();
    private static final ResourceLocation IDENTIFIER = MedicalSystem.resource("pain");
    private static final float DECAY_RATE = 0.015F;

    private PainEffectShaderProgram() {}

    @Override
    protected Holder<StatusEffectType<?>> getStatusEffect() {
        return MedSystemStatusEffects.PAIN;
    }

    @Override
    public float getGain() {
        return 3 * DECAY_RATE;
    }

    @Override
    public float getDecay() {
        return DECAY_RATE;
    }

    @Override
    public ResourceLocation postChainId() {
        return IDENTIFIER;
    }
}
