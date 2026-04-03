package tnt.tarkovcraft.medsystem.common.health_event.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.common.data.number.NumberProvider;
import tnt.tarkovcraft.core.common.data.number.NumberProviderType;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystemDefinition;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.health_event.function.HealthEventFunction;
import tnt.tarkovcraft.medsystem.common.health_event.function.HealthEventFunctionType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthEventActions;

import java.util.Collections;
import java.util.List;

public record AddShockEventAction(List<HealthEventFunction> scaleFunctions, NumberProvider amount) implements HealthEventAction {

    public static final MapCodec<AddShockEventAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            HealthEventFunctionType.CODEC.listOf().optionalFieldOf("scale_functions", Collections.emptyList()).forGetter(AddShockEventAction::scaleFunctions),
            NumberProviderType.VALUE_CODEC.fieldOf("amount").forGetter(AddShockEventAction::amount)
    ).apply(instance, AddShockEventAction::new));

    @Override
    public boolean apply(HealthEventContext ctx) {
        LivingEntity entity = ctx.getEntity();
        if (BloodSystemManager.isEnabled(entity)) {
            float shockAmount = HealthEventFunctionType.applyFunctions(this.amount.floatValue(), ctx, this.scaleFunctions);
            EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(entity);
            EntityBloodSystemDefinition definition = bloodSystem.getDefinition();
            float amount = definition.getReceivedShockValue(shockAmount, entity);
            bloodSystem.addShock(amount);
        }
        return true;
    }

    @Override
    public HealthEventActionType<?> getType() {
        return MedSystemHealthEventActions.ADD_SHOCK.value();
    }
}
