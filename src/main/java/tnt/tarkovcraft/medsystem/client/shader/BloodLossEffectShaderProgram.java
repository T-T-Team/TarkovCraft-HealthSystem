package tnt.tarkovcraft.medsystem.client.shader;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.core.api.shader.PostEffectShaderProgram;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.status.BloodData;
import tnt.tarkovcraft.medsystem.common.status.BloodStatus;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;

public class BloodLossEffectShaderProgram implements PostEffectShaderProgram {

    public static final Identifier PIPELINE = MedicalSystem.createIdentifier("bloodloss/0");
    private static final Identifier IDENTIFIER = MedicalSystem.createIdentifier("bloodloss");
    private float strength;
    private float lastStrength;
    private float interpolatedStrength;

    @Override
    public void tickProgram(Minecraft minecraft, LivingEntity livingEntity) {
        this.lastStrength = this.strength;
        if (!BloodSystem.hasBloodDataIntegration(livingEntity))
            return;
        BloodData data = BloodSystem.getBloodData(livingEntity);
        float percentage = data.getBloodVolumePercentage();
        BloodStatus status = BloodStatus.fromBloodLevelPercentage(percentage);
        if (status == BloodStatus.HEALTHY) {
            this.adjustTowards(0.0F);
        } else {
            float maxBloodPercentage = BloodStatus.MILD_BLOOD_LOSS.getAmount();
            float minBloodPercentage = BloodStatus.RANDOM_BLACKOUT.getAmount();
            float clampedValue = Mth.clamp(percentage, minBloodPercentage, maxBloodPercentage);
            float adjustedPercentage = 1.0F - ((clampedValue - minBloodPercentage) / (maxBloodPercentage - minBloodPercentage));
            this.adjustTowards(adjustedPercentage);
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
    public @Nullable GpuBufferSlice getDynamicUniformBuffer() {
        return RenderSystem.getDynamicUniforms().writeTransform(
                RenderSystem.getModelViewMatrix(), new Vector4f(0.0F, 0.0F, 0.0F, this.interpolatedStrength), new Vector3f(), new Matrix4f()
        );
    }
}
