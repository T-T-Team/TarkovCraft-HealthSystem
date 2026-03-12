package tnt.tarkovcraft.medsystem.client.model.tint;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodConfiguration;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodContainer;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodTypeOptions;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystemDefinition;
import tnt.tarkovcraft.medsystem.common.init.MedSystemItemComponents;

public final class BloodTintSource implements ItemColor {

    public static final BloodTintSource INSTANCE = new BloodTintSource();

    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        BloodContainer container = stack.get(MedSystemItemComponents.BLOOD_CONTAINER);
        if (container == null)
            return EntityBloodSystemDefinition.BLOOD_COLOR;
        BloodConfiguration configuration = MedicalSystem.BLOOD_SYSTEM.getConfig();
        return container.bloodType()
                .flatMap(configuration::getOptions)
                .map(BloodTypeOptions::color)
                .orElse(EntityBloodSystemDefinition.BLOOD_COLOR);
    }
}
