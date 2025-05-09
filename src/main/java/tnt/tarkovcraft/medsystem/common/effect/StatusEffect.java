package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import tnt.tarkovcraft.core.common.data.duration.TickValue;
import tnt.tarkovcraft.core.util.context.Context;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class StatusEffect {

    private int duration;
    private int delay;

    public StatusEffect(int duration, int delay) {
        this.duration = duration;
        this.delay = delay;
    }

    public abstract StatusEffectType<?> getType();

    public abstract void apply(Context context);

    public abstract StatusEffect onRemoved(Context context);

    public abstract StatusEffect copy();

    public void setCausingEntity(UUID owner) {}

    public UUID getCausingEntity() {
        return null;
    }

    public void addAdditionalInfo(Consumer<Component> tooltip) {}

    public final Optional<Entity> getCausingEntity(ServerLevel level) {
        UUID owner = this.getCausingEntity();
        if (owner != null) {
            return Optional.of(level.getEntity(owner));
        }
        return Optional.empty();
    }

    public final void markForRemoval() {
        this.setDuration(1);
    }

    public final int getDuration() {
        return this.duration;
    }

    public final void setDuration(int duration) {
        this.duration = duration;
    }

    public final int getDelay() {
        return this.delay;
    }

    public final void setDelay(int delay) {
        this.delay = delay;
    }

    public final void addDuration(int duration) {
        this.setDuration(this.getDuration() + duration);
    }

    public final void addDuration(TickValue duration) {
        this.setDuration(this.getDuration() + duration.tickValue());
    }

    public final boolean isActive() {
        return this.getDelay() <= 0;
    }

    public final boolean isInfinite() {
        return this.getDuration() < 0;
    }

    public static <T extends StatusEffect> Products.P2<RecordCodecBuilder.Mu<T>, Integer, Integer> common(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(
                Codec.INT.optionalFieldOf("duration", 600).forGetter(StatusEffect::getDuration),
                Codec.INT.optionalFieldOf("delay", 0).forGetter(StatusEffect::getDelay)
        );
    }

    public static <S extends StatusEffect> S merge(S a, S b) {
        if (a.getDelay() > b.getDelay()) {
            return a;
        }
        if (b.getDelay() > a.getDelay()) {
            return b;
        }
        if (a.isInfinite() || b.isInfinite()) {
            a.setDuration(-1);
        } else {
            int duration = a.getDuration();
            a.setDuration(duration + b.getDuration());
        }
        return a;
    }
}
