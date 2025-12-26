package tnt.tarkovcraft.medsystem.client.shader;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.pipeline.PipelineModifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.core.api.shader.PostEffectShaderProgram;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import java.util.Optional;

public class PainEffectShaderProgram implements PostEffectShaderProgram {

    private static final Identifier IDENTIFIER = MedicalSystem.createIdentifier("pain");
    private static final float MAX_STRENGTH = 0.035F;
    private static final float DECAY_RATE = 0.0005F;
    private float strength = 0.0F;

    @Override
    public void tickProgram(Minecraft client, LivingEntity entity) {
        HealthContainer container = HealthSystem.getHealthData(entity);
        StatusEffectMap effects = container.getGlobalStatusEffects();
        Optional<StatusEffect> holder = effects.getEffect(MedSystemStatusEffects.PAIN);
        if (holder.isPresent()) {
            this.strength = Math.min(MAX_STRENGTH, this.strength + DECAY_RATE * 3);
        } else if (this.strength > 0.0F) {
            this.strength = Math.max(0.0F, this.strength - DECAY_RATE);
        }
    }

    @Override
    public boolean active() {
        return this.strength > 0.0F;
    }

    @Override
    public @Nullable ResourceKey<PipelineModifier> modifier() {
        return Modifier.MODIFIER_KEY;
    }

    @Override
    public @Nullable GpuBufferSlice getDynamicUniformBuffer() {
        return RenderSystem.getDynamicUniforms().writeTransform(
                RenderSystem.getModelViewMatrix(), new Vector4f(this.strength, 0.0F, 0.0F, 0.0F), new Vector3f(), new Matrix4f()
        );
    }

    @Override
    public Identifier postChainId() {
        return IDENTIFIER;
    }

    public static final class Modifier implements PipelineModifier {

        private static final Identifier PIPELINE_ID = MedicalSystem.createIdentifier("pain/0");
        public static final ResourceKey<PipelineModifier> MODIFIER_KEY = ResourceKey.create(PipelineModifier.MODIFIERS_KEY, PIPELINE_ID);

        @Override
        public RenderPipeline apply(RenderPipeline pipeline, Identifier name) {
            if (PIPELINE_ID.equals(pipeline.getLocation())) {
                return pipeline.toBuilder()
                        .withLocation(name)
                        .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                        .build();
            }
            return pipeline;
        }
    }
}
