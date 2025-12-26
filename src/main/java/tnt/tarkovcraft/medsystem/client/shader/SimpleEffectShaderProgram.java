package tnt.tarkovcraft.medsystem.client.shader;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.core.api.shader.PostEffectShaderProgram;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;

public abstract class SimpleEffectShaderProgram implements PostEffectShaderProgram {

    private float strength;

    public abstract Holder<StatusEffectType<?>> getEffect();

    public abstract float getStrengthGain();

    public abstract float getStrengthDecay();

    @Override
    public final boolean active() {
        return this.strength > 0.0F;
    }

    @Override
    public final void tickProgram(Minecraft minecraft, LivingEntity livingEntity) {
        if (!HealthSystem.hasCustomHealth(livingEntity))
            return;
        HealthContainer container = HealthSystem.getHealthData(livingEntity);
        StatusEffectMap map = container.getGlobalStatusEffects();
        if (this.canApply(livingEntity, container, map) && map.hasEffect(this.getEffect())) {
            this.strength = Math.min(1.0F, this.strength + this.getStrengthGain());
        } else if (this.strength > 0.0F) {
            this.strength = Math.max(0.0F, this.strength - this.getStrengthDecay());
        }
    }

    @Override
    public final @Nullable GpuBufferSlice getDynamicUniformBuffer() {
        return RenderSystem.getDynamicUniforms().writeTransform(
                RenderSystem.getModelViewMatrix(), new Vector4f(0.0F, 0.0F, 0.0F, this.strength), new Vector3f(), new Matrix4f()
        );
    }

    protected boolean canApply(LivingEntity entity, HealthContainer container, StatusEffectMap map) {
        return true;
    }
}
