package tnt.tarkovcraft.medsystem.client.model.tint;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodConfiguration;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodContainer;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodTypeOptions;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystemDefinition;
import tnt.tarkovcraft.medsystem.common.init.MedSystemItemComponents;

public final class BloodTintSource implements ItemTintSource {

    public static final Identifier IDENTIFIER = MedicalSystem.createIdentifier("blood_tint");
    public static final BloodTintSource INSTANCE = new BloodTintSource();
    public static final MapCodec<BloodTintSource> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        BloodContainer container = stack.get(MedSystemItemComponents.BLOOD_CONTAINER);
        if (container == null)
            return EntityBloodSystemDefinition.BLOOD_COLOR;
        BloodConfiguration configuration = MedicalSystem.BLOOD_SYSTEM.getConfig();
        return container.bloodType()
                .flatMap(configuration::getOptions)
                .map(BloodTypeOptions::color)
                .orElse(EntityBloodSystemDefinition.BLOOD_COLOR);
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return CODEC;
    }
}
