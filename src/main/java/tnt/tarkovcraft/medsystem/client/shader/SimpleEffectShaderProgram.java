package tnt.tarkovcraft.medsystem.client.shader;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.core.api.shader.PostEffectShaderProgram;
import tnt.tarkovcraft.core.client.shader.ShaderHelper;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;

public abstract class SimpleEffectShaderProgram implements PostEffectShaderProgram {

    private float strength;
    private float lastStrength;
    private float smoothStrength;

    public abstract Holder<StatusEffectType<?>> getEffect();

    public abstract float getStrengthGain();

    public abstract float getStrengthDecay();

    @Override
    public final boolean active() {
        return this.strength > 0.0F;
    }

    @Override
    public final void onRender(DeltaTracker deltaTracker) {
        this.smoothStrength = this.applySmoothing(this.lastStrength, this.strength, deltaTracker.getGameTimeDeltaTicks());
    }

    @Override
    public final void tickProgram(Minecraft minecraft, LivingEntity livingEntity) {
        this.lastStrength = this.strength;
        if (!HealthSystem.hasCustomHealth(livingEntity))
            return;
        HealthContainer container = HealthContainer.getAttached(livingEntity);
        StatusEffectMap map = container.getGlobalStatusEffects();
        if (this.canApply(livingEntity, container, map) && map.hasEffect(this.getEffect())) {
            this.strength = Math.min(1.0F, this.strength + this.getStrengthGain());
        } else if (this.strength > 0.0F) {
            this.strength = Math.max(0.0F, this.strength - this.getStrengthDecay());
        }
    }

    @Override
    public final @Nullable GpuBufferSlice getDynamicUniformBuffer() {
        return ShaderHelper.scaleTransform(this.smoothStrength);
    }

    @Override
    public void resetShader() {
        this.strength = 0.0F;
    }

    protected boolean canApply(LivingEntity entity, HealthContainer container, StatusEffectMap map) {
        return true;
    }

    protected float applySmoothing(float start, float end, float delta) {
        return Mth.lerp(delta, start, end);
    }
}
