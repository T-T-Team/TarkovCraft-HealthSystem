package tnt.tarkovcraft.medsystem.client.shader;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.core.api.shader.PostEffectShaderProgram;
import tnt.tarkovcraft.core.api.shader.ShaderType;
import tnt.tarkovcraft.core.client.shader.ShaderHelper;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystemDefinition;

public class BloodLossEffectShaderProgram implements PostEffectShaderProgram {

    private static final Identifier IDENTIFIER = MedicalSystem.createIdentifier("bloodloss");
    public static final Identifier PIPELINE = ShaderHelper.getPostChainPipeline(IDENTIFIER, 0);
    private float strength;
    private float lastStrength;
    private float interpolatedStrength;

    @Override
    public void tickProgram(Minecraft minecraft, LivingEntity livingEntity) {
        this.lastStrength = this.strength;
        if (!BloodSystemManager.isEnabled(livingEntity))
            return;
        EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(livingEntity);
        EntityBloodSystemDefinition definition = bloodSystem.getDefinition();
        float percentage = bloodSystem.getBloodVolume() / definition.getMaxBloodVolume();
        if (definition.shouldApplyGrayscaleShader(percentage)) {
            float grayscale = definition.getGrayscaleAmount(percentage);
            this.adjustTowards(grayscale);
        } else {
            this.adjustTowards(0.0F);
        }
    }

    private void adjustTowards(float target) {
        float change = 0.025F;
        if (target < this.strength) {
            this.strength = Math.max(target, this.strength - change);
        } else if (target > this.strength) {
            this.strength = Math.min(target, this.strength + change);
        }
    }

    @Override
    public void onRender(DeltaTracker deltaTracker) {
        this.interpolatedStrength = Mth.lerp(deltaTracker.getGameTimeDeltaTicks(), this.lastStrength, this.strength);
    }

    @Override
    public boolean active() {
        return this.strength > 0.0F;
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
    public @Nullable GpuBufferSlice getDynamicUniformBuffer() {
        return ShaderHelper.scaleTransform(this.interpolatedStrength);
    }
}
