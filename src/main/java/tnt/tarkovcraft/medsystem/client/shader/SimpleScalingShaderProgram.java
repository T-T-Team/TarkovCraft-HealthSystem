package tnt.tarkovcraft.medsystem.client.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.api.shader.PostEffectShaderProgram;

public abstract class SimpleScalingShaderProgram implements PostEffectShaderProgram {

    protected float strength;
    private float lastStrength;

    @Override
    public void tickProgram(Minecraft client, LivingEntity entity) {
        this.lastStrength = this.strength;
    }

    @Override
    public boolean active() {
        return this.strength > 0.0F;
    }

    @Override
    public void onRender(float delta, UniformSetter uniformSetter) {
        float interpolatedStrength = this.applySmoothing(delta, this.lastStrength, this.strength);
        uniformSetter.setUniform("Scale", interpolatedStrength);
    }

    protected float applySmoothing(float delta, float start, float end) {
        return Mth.lerp(delta, start, end);
    }
}
