package tnt.tarkovcraft.medsystem.common.config;

import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.blood_system.UnconsciousOptions;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;

public enum UnconsciousEntityTargeting {

    ALWAYS(TargetingPredicate.ALWAYS),
    IGNORE_RESCUE(TargetingPredicate.IGNORE_RESCUE),
    NEVER(TargetingPredicate.NEVER);

    private final TargetingPredicate predicate;

    UnconsciousEntityTargeting(TargetingPredicate predicate) {
        this.predicate = predicate;
    }

    public boolean canTargetEntity(LivingEntity target, EntityBloodSystem bloodSystem) {
        return this.predicate.canAttack(target, bloodSystem);
    }

    private static boolean skipRescueModeTargeting(LivingEntity entity, EntityBloodSystem bloodSystem) {
        UnconsciousOptions options = bloodSystem.getUnconsciousState().getUnconsciousOptions();
        return !options.allowRescue();
    }

    @FunctionalInterface
    public interface TargetingPredicate {

        TargetingPredicate ALWAYS = (entity, bloodSystem) -> true;
        TargetingPredicate IGNORE_RESCUE = UnconsciousEntityTargeting::skipRescueModeTargeting;
        TargetingPredicate NEVER = (entity, bloodSystem) -> false;

        boolean canAttack(LivingEntity target, EntityBloodSystem bloodSystem);
    }
}
