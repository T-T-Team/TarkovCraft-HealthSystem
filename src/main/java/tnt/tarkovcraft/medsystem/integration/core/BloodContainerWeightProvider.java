package tnt.tarkovcraft.medsystem.integration.core;

import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemInstance;
import tnt.tarkovcraft.core.common.weight.WeightContext;
import tnt.tarkovcraft.core.common.weight.WeightProvider;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodContainer;
import tnt.tarkovcraft.medsystem.common.init.MedSystemItemComponents;

public class BloodContainerWeightProvider implements WeightProvider {

    public static final Identifier PROVIDER_ID = MedicalSystem.createIdentifier("weight/item/blood_container");

    @Override
    public WeightSource getSource() {
        return WeightSource.ITEM;
    }

    @Override
    public int getWeight(WeightContext weightContext) {
        ItemInstance instance = weightContext.item();
        BloodContainer container = instance.get(MedSystemItemComponents.BLOOD_CONTAINER);
        if (container == null)
            return 0;
        float volume = container.value();
        return Mth.ceil(1000 * volume);
    }
}
