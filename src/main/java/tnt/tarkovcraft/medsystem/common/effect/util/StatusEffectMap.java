package tnt.tarkovcraft.medsystem.common.effect.util;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import tnt.tarkovcraft.medsystem.api.event.StatusEffectEvent;
import tnt.tarkovcraft.medsystem.common.effect.PainStatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectContext;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

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
    private final StatusEffectSubmitter submitter = StatusEffectSubmitter.list();

    public StatusEffectMap() {
        this.effects = new LinkedHashMap<>();
    }

    private StatusEffectMap(Map<StatusEffectType<?>, StatusEffect> effects) {
        this.effects = new LinkedHashMap<>(effects);
    }

    public void tick(StatusEffectContext.MutableContext ctx) {
        if (this.effects.isEmpty())
            return;
        Iterator<Map.Entry<StatusEffectType<?>, StatusEffect>> it = effects.entrySet().iterator();
        ctx.withEffectSubmitter(this.submitter);
        LivingEntity entity = ctx.entity();
        while (it.hasNext()) {
            StatusEffect effect = it.next().getValue();
            effect.apply(ctx);
            if (!entity.isAlive())
                break;
            if (!effect.isInfinite()) {
                int newDuration = effect.getDuration() - 1;
                effect.setDuration(newDuration);
                if (newDuration <= 0) {
                    it.remove();
                    effect.onRemoved(ctx);
                }
            }
        }
        if (entity.isAlive()) {
            this.submitPendingEffects(this.submitter, entity, ctx.limb());
        }
    }

    public void painEffectTick(LivingEntity entity, int delay, boolean tickNow) {
        Level level = entity.level();
        long time = level.getGameTime();
        boolean isValidTick = tickNow || time % 20L == 0L;
        if (isValidTick && !this.hasEffect(MedSystemStatusEffects.PAIN) && HealthSystem.isInPain(entity)) {
            StatusEffectHelper.addGlobalEffect(this, entity, delay, PainStatusEffect.infinite());
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

    public void removeAll(StatusEffectContext ctx) {
        Collection<StatusEffectType<?>> keys = new ArrayList<>(this.effects.keySet());
        for (StatusEffectType<?> key : keys) {
            this.remove(key, ctx);
        }
    }

    public void remove(StatusEffectType<?> type, StatusEffectContext context) {
        StatusEffect effect = this.effects.remove(type);
        if (effect != null) {
            effect.onRemoved(context);
        }
    }

    public void remove(Holder<StatusEffectType<?>> holder, StatusEffectContext ctx) {
        this.remove(holder.value(), ctx);
    }

    public boolean removeMatching(TagKey<StatusEffectType<?>> tag, StatusEffectContext ctx) {
        return this.effects.entrySet().removeIf(entry -> {
            if (entry.getKey().is(tag)) {
                entry.getValue().onRemoved(ctx);
                NeoForge.EVENT_BUS.post(new StatusEffectEvent.Remove(ctx.entity(), entry.getValue(), ctx.limb()));
                return true;
            }
            return false;
        });
    }

    public Map<StatusEffectType<?>, StatusEffect> getEffects() {
        return Collections.unmodifiableMap(this.effects);
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
    public @NotNull Iterator<StatusEffect> iterator() {
        return this.listEffects().iterator();
    }

    public StatusEffectSubmitter getEffectSubmitter() {
        return this.submitter;
    }

    private void submitPendingEffects(StatusEffectSubmitter submitter, LivingEntity entity, @Nullable Limb limb) {
        submitter.accept(delayedEffect -> {
            if (limb != null) {
                StatusEffectHelper.addEffect(this, entity, limb, delayedEffect.getDelay(), delayedEffect.createInstance());
            } else {
                StatusEffectHelper.addGlobalEffect(this, entity, delayedEffect.getDelay(), delayedEffect.createInstance());
            }
        });
        submitter.clear();
    }
}
