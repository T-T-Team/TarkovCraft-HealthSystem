package tnt.tarkovcraft.medsystem.client.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.BiConsumer;

public abstract class SimpleScalingShaderProgram implements ShaderProgram {

    protected float strength;
    private float lastStrength;

    @Override
    public void update(Minecraft client, LivingEntity entity) {
        this.lastStrength = this.strength;
    }

    @Override
    public boolean shouldRender() {
        return this.strength > 0.0F;
    }

    @Override
    public void renderTick(float delta, BiConsumer<String, Float> uniformSetter) {
        float interpolatedStrength = this.applySmoothing(delta, this.lastStrength, this.strength);
        uniformSetter.accept("Scale", interpolatedStrength);
    }

    protected float applySmoothing(float delta, float start, float end) {
        return Mth.lerp(delta, start, end);
    }
}
