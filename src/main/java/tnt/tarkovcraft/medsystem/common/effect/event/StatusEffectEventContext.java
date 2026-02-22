package tnt.tarkovcraft.medsystem.common.effect.event;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public final class StatusEffectEventContext {

    private final LivingEntity entity;
    private final HealthContainer container;
    private final Limb limb;
    private final Map<LootContextParam<?>, Object> parameters;

    private StatusEffectEventContext(LivingEntity entity, HealthContainer container, Limb limb, Map<LootContextParam<?>, Object> parameters) {
        this.entity = Objects.requireNonNull(entity);
        this.container = Objects.requireNonNull(container);
        this.limb = Objects.requireNonNull(limb);
        this.parameters = ImmutableMap.copyOf(parameters);
    }

    public static StatusEffectEventContext simple(LivingEntity entity, HealthContainer container, Limb limb) {
        return new StatusEffectEventContext(entity, container, limb, Map.of());
    }

    public static StatusEffectEventContext withParams(LivingEntity entity, HealthContainer container, Limb limb, Consumer<ParameterBuilder> builder) {
        Map<LootContextParam<?>, Object> params = new HashMap<>();
        builder.accept(params::put);
        return new StatusEffectEventContext(entity, container, limb, params);
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
        LootContextParam<Float> key = localDamage ? StatusEffectEventParams.DAMAGE_AMOUNT_LIMB : StatusEffectEventParams.DAMAGE_AMOUNT;
        return this.getParameterOrDefault(key, fallback);
    }

    public boolean hasParameter(LootContextParam<?> key) {
        return this.parameters.containsKey(key);
    }

    @Nullable
    public <T> T getParameter(LootContextParam<T> key) {
        return (T) this.parameters.get(key);
    }

    public <T> T getParameterOrDefault(LootContextParam<T> key, T fallback) {
        T val = this.getParameter(key);
        return val != null ? val : fallback;
    }

    public interface ParameterBuilder {
        <T> void add(LootContextParam<T> key, T value);
    }
}
