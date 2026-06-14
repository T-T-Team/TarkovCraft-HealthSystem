package tnt.tarkovcraft.medsystem.common.health_event.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.common.data.number.NumberProvider;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystemDefinition;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.health_event.function.HealthEventFunction;

import java.util.Collections;
import java.util.List;

public record AddShockEventAction(List<HealthEventFunction> scaleFunctions, float amount) implements HealthEventAction {

    public static final MapCodec<AddShockEventAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            HealthEventFunction.CODEC.listOf().optionalFieldOf("scale_functions", Collections.emptyList()).forGetter(AddShockEventAction::scaleFunctions),
            NumberProvider.NON_NEGATIVE_FLOAT.fieldOf("amount").forGetter(AddShockEventAction::amount)
    ).apply(instance, AddShockEventAction::new));

    @Override
    public boolean apply(HealthEventContext ctx) {
        LivingEntity entity = ctx.getEntity();
        if (BloodSystemManager.isEnabled(entity)) {
            float shockAmount = HealthEventFunction.applyFunctions(this.amount, ctx, this.scaleFunctions);
            EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(entity);
            EntityBloodSystemDefinition definition = bloodSystem.getDefinition();
            float amount = definition.getReceivedShockValue(shockAmount, entity);
            bloodSystem.addShock(amount);
        }
        return true;
    }

    @Override
    public MapCodec<? extends HealthEventAction> codec() {
        return CODEC;
    }
}
