package tnt.tarkovcraft.medsystem.client.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.api.shader.ShaderType;
import tnt.tarkovcraft.core.util.Easing;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

public final class ConcussionEffectShaderProgram extends StatusEffectShaderProgram {

    public static final ConcussionEffectShaderProgram INSTANCE = new ConcussionEffectShaderProgram();

    private long gameTime;

    private ConcussionEffectShaderProgram() {}

    @Override
    protected Holder<StatusEffectType<?>> getStatusEffect() {
        return MedSystemStatusEffects.CONCUSSION;
    }

    @Override
    public void tickProgram(Minecraft client, LivingEntity entity) {
        super.tickProgram(client, entity);
        this.gameTime = client.level.getGameTime() % 24000L;
    }

    @Override
    protected float getGain() {
        return 1.0F;
    }

    @Override
    protected float getDecay() {
        return 0.01F;
    }

    @Override
    public void onRender(float delta, UniformSetter uniformSetter) {
        super.onRender(delta, uniformSetter);
        uniformSetter.setUniform("GameTime", this.gameTime / 24000.0F);
    }

    @Override
    public ShaderType getShaderType() {
        return ShaderType.COSMETIC;
    }

    @Override
    protected float applySmoothing(float delta, float start, float end) {
        return Easing.EASE_OUT_SINE.apply(super.applySmoothing(delta, start, end));
    }
}
