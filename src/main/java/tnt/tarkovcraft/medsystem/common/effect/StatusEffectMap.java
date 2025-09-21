package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import tnt.tarkovcraft.medsystem.api.event.StatusEffectEvent;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
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

    public void tick(HealthContainer container, LivingEntity entity, @Nullable BodyPart limb) {
        if (this.effects.isEmpty())
            return;
        Iterator<Map.Entry<StatusEffectType<?>, StatusEffect>> it = effects.entrySet().iterator();
        List<PostEffect> newEffects = new ArrayList<>();
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
                    Collection<PostEffect> postEffects = effect.onRemoved(container, entity, limb);
                    if (postEffects != null && !postEffects.isEmpty()) {
                        newEffects.addAll(postEffects);
                    }
                }
            }
        }
        if (entity.isAlive()) {
            newEffects.forEach(effect -> StatusEffectHelper.addPostEffect(this, entity, limb, effect));
        }
    }

    public <T extends StatusEffect> void addEffect(T effect) {
        StatusEffectType<T> type = (StatusEffectType<T>) Objects.requireNonNull(effect.getType());
        this.effects.merge(type, effect, (a, b) -> type.merge((T) a, (T) b));
    }

    @Nullable
    public <T extends StatusEffect> T replace(T effect) {
        return (T) this.effects.put(effect.getType(), effect);
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

    public void removeAll(HealthContainer container, LivingEntity entity, @Nullable BodyPart limb) {
        Collection<StatusEffectType<?>> keys = new ArrayList<>(this.effects.keySet());
        for (StatusEffectType<?> key : keys) {
            this.remove(key, container, entity, limb);
        }
    }

    public Collection<PostEffect> remove(StatusEffectType<?> type, HealthContainer container, LivingEntity entity, @Nullable BodyPart limb) {
        StatusEffect effect = this.effects.remove(type);
        if (effect != null) {
            return effect.onRemoved(container, entity, limb);
        }
        return null;
    }

    public Collection<PostEffect> remove(Holder<StatusEffectType<?>> holder, HealthContainer container, LivingEntity entity, @Nullable BodyPart limb) {
        return this.remove(holder.value(), container, entity, limb);
    }

    public boolean removeMatching(TagKey<StatusEffectType<?>> tag, HealthContainer container, LivingEntity entity, @Nullable BodyPart limb) {
        return this.effects.entrySet().removeIf(entry -> {
            if (entry.getKey().is(tag)) {
                entry.getValue().onRemoved(container, entity, limb);
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

    public StatusEffectMap copy() {
        Map<StatusEffectType<?>, StatusEffect> instances = this.getEffectsStream()
                .map(StatusEffect::copy).collect(Collectors.toMap(
                        StatusEffect::getType,
                        Function.identity(),
                        (e1, e2) -> ((StatusEffectType<StatusEffect>) e1.getType()).merge(e1, e2)
                ));
        return new StatusEffectMap(instances);
    }

    public void importAndMerge(StatusEffectMap other) {
        Map<StatusEffectType<?>, StatusEffect> merged = Stream.concat(this.getEffectsStream(), other.getEffectsStream())
                .collect(Collectors.toMap(
                        StatusEffect::getType,
                        Function.identity(),
                        (e1, e2) -> ((StatusEffectType<StatusEffect>) e1.getType()).merge(e1, e2),
                        LinkedHashMap::new
                ));
        this.effects.clear();
        this.effects.putAll(merged);
    }

    @Override
    public @NotNull Iterator<StatusEffect> iterator() {
        return this.listEffects().iterator();
    }
}
