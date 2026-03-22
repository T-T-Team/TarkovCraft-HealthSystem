package tnt.tarkovcraft.medsystem.client.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystemDefinition;

public final class BloodLossEffectShaderProgram extends SimpleScalingShaderProgram {

    public static final BloodLossEffectShaderProgram INSTANCE = new BloodLossEffectShaderProgram();
    private static final ResourceLocation IDENTIFIER = MedicalSystem.resource("bloodloss");

    private BloodLossEffectShaderProgram() {}

    @Override
    public void tickProgram(Minecraft client, LivingEntity entity) {
        super.tickProgram(client, entity);
        if (!BloodSystemManager.isEnabled(entity))
            return;
        EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(entity);
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
    public ResourceLocation postChainId() {
        return IDENTIFIER;
    }
}
