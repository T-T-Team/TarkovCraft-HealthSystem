package tnt.tarkovcraft.medsystem.common.health_event;

import com.google.common.collect.ImmutableMap;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public final class HealthEventContext {

    private final LivingEntity entity;
    private final HealthContainer container;
    private final Limb limb;
    private final Map<ContextKey<?>, Object> parameters;

    private HealthEventContext(LivingEntity entity, HealthContainer container, Limb limb, Map<ContextKey<?>, Object> parameters) {
        this.entity = Objects.requireNonNull(entity);
        this.container = Objects.requireNonNull(container);
        this.limb = Objects.requireNonNull(limb);
        this.parameters = ImmutableMap.copyOf(parameters);
    }

    public static HealthEventContext simple(LivingEntity entity, HealthContainer container, Limb limb) {
        return new HealthEventContext(entity, container, limb, Map.of());
    }

    public static HealthEventContext withParams(LivingEntity entity, HealthContainer container, Limb limb, Consumer<ParameterBuilder> builder) {
        Map<ContextKey<?>, Object> params = new HashMap<>();
        builder.accept(params::put);
        return new HealthEventContext(entity, container, limb, params);
    }

    public LivingEntity getEntity() {
        return this.entity;
    }

    public HealthContainer getHealthContainer() {
        return this.container;
    }

    public Limb getLimb() {
        return this.limb;
    }

    public float getDamage(boolean localDamage, float fallback) {
        ContextKey<Float> key = localDamage ? HealthEventParams.DAMAGE_AMOUNT_LIMB : HealthEventParams.DAMAGE_AMOUNT;
        return this.getParameterOrDefault(key, fallback);
    }

    public boolean hasParameter(ContextKey<?> key) {
        return this.parameters.containsKey(key);
    }

    @Nullable
    public <T> T getParameter(ContextKey<T> key) {
        return (T) this.parameters.get(key);
    }

    public <T> T getParameterOrDefault(ContextKey<T> key, T fallback) {
        T val = this.getParameter(key);
        return val != null ? val : fallback;
    }

    public interface ParameterBuilder {
        <T> void add(ContextKey<T> key, T value);
    }
}
