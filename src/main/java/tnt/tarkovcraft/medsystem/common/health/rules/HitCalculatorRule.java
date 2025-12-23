package tnt.tarkovcraft.medsystem.common.health.rules;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.math.HitCalculator;

import java.util.function.Function;
import java.util.function.Predicate;

public record HitCalculatorRule(int priority, Predicate<Context> filter, Function<Context, HitCalculator> factory) {

    public static int SPECIFIC_PART = -1_000;
    public static int ENVIRONMENT = -750;
    public static int EFFECTS = -500;
    public static int GENERIC = 100;
    public static int MELEE = 200;
    public static int PROJECTILE = 300;

    public boolean validate(Context ctx) {
        return filter.test(ctx);
    }

    public HitCalculator createCalculator(Context ctx) {
        return factory.apply(ctx);
    }

    public record Context(DamageSource source, LivingEntity target, HealthContainer container) {

        public Entity getSourceEntity() {
            return this.source.getEntity() != null ? this.source.getEntity() : this.source.getDirectEntity();
        }
    }
}
