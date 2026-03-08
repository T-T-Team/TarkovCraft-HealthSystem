package tnt.tarkovcraft.medsystem.client.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystemDefinition;

public final class BloodlossShaderProgram extends SimpleScalingShaderProgram {

    public static final BloodlossShaderProgram INSTANCE = new BloodlossShaderProgram();
    private static final ResourceLocation IDENTIFIER = MedicalSystem.resource("bloodloss");

    private BloodlossShaderProgram() {}

    @Override
    public ResourceLocation postChainId() {
        return IDENTIFIER;
    }

    @Override
    public void update(Minecraft client, LivingEntity entity) {
        super.update(client, entity);
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
}
