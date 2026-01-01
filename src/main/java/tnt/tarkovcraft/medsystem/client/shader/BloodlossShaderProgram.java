package tnt.tarkovcraft.medsystem.client.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.status.BloodData;
import tnt.tarkovcraft.medsystem.common.status.BloodStatus;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;

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
        if (!BloodSystem.hasBloodDataIntegration(entity))
            return;
        BloodData data = BloodSystem.getBloodData(entity);
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
}
