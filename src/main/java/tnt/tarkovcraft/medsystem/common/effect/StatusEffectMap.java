package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import org.jetbrains.annotations.NotNull;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import javax.annotation.Nullable;
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
        this.effects = new HashMap<>();
    }

    private StatusEffectMap(Map<StatusEffectType<?>, StatusEffect> effects) {
        this.effects = new HashMap<>(effects);
    }

    public void tick(Context context) {
        if (this.effects.isEmpty())
            return;
        Iterator<Map.Entry<StatusEffectType<?>, StatusEffect>> it = effects.entrySet().iterator();
        while (it.hasNext()) {
            StatusEffect effect = it.next().getValue();
            effect.apply(context);
            if (!effect.isInfinite()) {
                int newDuration = effect.getDuration() - 1;
                effect.setDuration(newDuration);
                if (newDuration <= 0) {
                    it.remove();
                    effect.onRemoved(context);
                }
            }
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

    public <T extends StatusEffect> boolean hasEffect(Holder<StatusEffectType<?>> holder) {
        return this.hasEffect(holder.value());
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

    public void removeAll(Context context) {
        Collection<StatusEffectType<?>> keys = new ArrayList<>(this.effects.keySet());
        for (StatusEffectType<?> key : keys) {
            this.remove(key, context);
        }
    }

    public void remove(StatusEffectType<?> type, Context context) {
        StatusEffect effect = this.effects.remove(type);
        if (effect != null) {
            effect.onRemoved(context);
        }
    }

    public Collection<StatusEffect> listEffects() {
        return this.effects.values();
    }

    public Stream<StatusEffect> getEffectsStream() {
        return this.listEffects().stream();
    }

    @Override
    public @NotNull Iterator<StatusEffect> iterator() {
        return this.listEffects().iterator();
    }
}
