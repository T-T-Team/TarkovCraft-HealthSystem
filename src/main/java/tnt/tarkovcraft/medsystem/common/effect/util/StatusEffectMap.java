package tnt.tarkovcraft.medsystem.common.effect.util;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.medsystem.api.event.StatusEffectEvent;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public final class StatusEffectMap implements Iterable<StatusEffect> {

    public static final Codec<StatusEffectMap> CODEC = Codec.unboundedMap(
            MedSystemRegistries.STATUS_EFFECT.byNameCodec(),
            StatusEffectType.CODEC
    ).xmap(StatusEffectMap::new, t -> t.effects);

    private final Map<StatusEffectType<?>, StatusEffect> effects;

    public StatusEffectMap() {
        this.effects = new LinkedHashMap<>();
    }

    private StatusEffectMap(Map<StatusEffectType<?>, StatusEffect> effects) {
        this.effects = new LinkedHashMap<>(effects);
    }

    public void tick(HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
        if (this.effects.isEmpty())
            return;
        Iterator<Map.Entry<StatusEffectType<?>, StatusEffect>> it = effects.entrySet().iterator();
        ListStatusEffectSubmitter submitter = StatusEffectSubmitter.list();
        while (it.hasNext()) {
            StatusEffect effect = it.next().getValue();
            effect.apply(container, entity, limb);
            if (!entity.isAlive())
                break;
            if (!effect.isInfinite()) {
                int newDuration = effect.getDuration() - 1;
                effect.setDuration(newDuration);
                if (newDuration <= 0) {
                    it.remove();
                    effect.onRemoved(submitter, container, entity, limb);
                }
            }
        }
        if (entity.isAlive()) {
            StatusEffectHelper.handleSubmittedEffects(this, entity, limb, submitter);
        }
    }

    public <T extends StatusEffect> void addEffect(T effect) {
        StatusEffectType<T> type = (StatusEffectType<T>) effect.getType();
        if (!type.isDisabled()) {
            this.effects.merge(type, effect, (a, b) -> type.merge((T) a, (T) b));
        }
    }

    @Nullable
    public <T extends StatusEffect> T replace(T effect) {
        StatusEffectType<T> type = (StatusEffectType<T>) effect.getType();
        return type.isDisabled() ? null : (T) this.effects.put(type, effect);
    }

    public <T extends StatusEffect> boolean hasEffect(StatusEffectType<T> type) {
        return this.effects.containsKey(type);
    }

    public <T extends StatusEffect> boolean hasEffect(Supplier<StatusEffectType<T>> type) {
        return this.hasEffect(type.get());
    }

    public boolean hasEffect(Holder<StatusEffectType<?>> holder) {
        return this.hasEffect(holder.value());
    }

    public boolean hasEffect(TagKey<StatusEffectType<?>> tag) {
        for (StatusEffectType<?> type : this.effects.keySet()) {
            if (type.is(tag))
                return true;
        }
        return false;
    }

    public <T extends StatusEffect> Optional<T> getEffect(StatusEffectType<T> type) {
        return Optional.ofNullable((T) this.effects.get(type));
    }

    public <T extends StatusEffect> Optional<T> getEffect(Supplier<StatusEffectType<T>> type) {
        return this.getEffect(type.get());
    }

    public <T extends StatusEffect> Optional<T> getEffect(Holder<StatusEffectType<?>> holder) {
        return this.getEffect((StatusEffectType<T>) holder.value());
    }

    public void removeAll(StatusEffectSubmitter submitter, HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
        Collection<StatusEffectType<?>> keys = new ArrayList<>(this.effects.keySet());
        for (StatusEffectType<?> key : keys) {
            this.remove(submitter, key, container, entity, limb);
        }
    }

    public void remove(StatusEffectSubmitter submitter, StatusEffectType<?> type, HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
        StatusEffect effect = this.effects.remove(type);
        if (effect != null) {
            effect.onRemoved(submitter, container, entity, limb);
        }
    }

    public void remove(StatusEffectSubmitter submitter, Holder<StatusEffectType<?>> holder, HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
        this.remove(submitter, holder.value(), container, entity, limb);
    }

    public boolean removeMatching(StatusEffectSubmitter submitter, TagKey<StatusEffectType<?>> tag, HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
        return this.effects.entrySet().removeIf(entry -> {
            if (entry.getKey().is(tag)) {
                entry.getValue().onRemoved(submitter, container, entity, limb);
                NeoForge.EVENT_BUS.post(new StatusEffectEvent.Remove(entity, entry.getValue(), limb));
                return true;
            }
            return false;
        });
    }

    public Collection<StatusEffect> listEffects() {
        return this.effects.values();
    }

    public Stream<StatusEffect> getEffectsStream() {
        return this.listEffects().stream();
    }

    public boolean isEmpty() {
        return this.effects.isEmpty();
    }

    @Override
    public @NonNull Iterator<StatusEffect> iterator() {
        return this.listEffects().iterator();
    }
}
