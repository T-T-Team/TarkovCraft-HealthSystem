package tnt.tarkovcraft.medsystem.common.effect.group;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import tnt.tarkovcraft.core.common.data.duration.TickValue;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectContext;

import java.util.List;
import java.util.function.Consumer;

public final class EffectGroupHolder {

    public static final Codec<EffectGroupHolder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EffectGroupItemType.CODEC.fieldOf("data").forGetter(t -> t.item),
            Codec.INT.fieldOf("duration").forGetter(t -> t.duration),
            Codec.INT.optionalFieldOf("delay", 0).forGetter(t -> t.delay),
            Codec.BOOL.optionalFieldOf("active", false).forGetter(t -> t.active)
    ).apply(instance, EffectGroupHolder::new));

    private final EffectGroupItem item;
    private int duration;
    private int delay;
    private boolean active;

    public EffectGroupHolder(EffectGroupItem item, int duration, int delay) {
        this(item, duration, delay, false);
    }

    private EffectGroupHolder(EffectGroupItem item, int duration, int delay, boolean active) {
        this.item = item;
        this.duration = duration;
        this.delay = delay;
        this.active = active;
    }

    public static Factory getFactory(List<EffectGroupHolder> items) {
        return new Factory(items);
    }

    public void tick(StatusEffectContext context) {
        if (this.delay > 0 && --this.delay > 0) {
            return;
        }
        if (!this.active) {
            this.active = true;
            this.item.init(this, context);
        }
        this.item.apply(this, context);
        if (--this.duration <= 0) {
            this.cleanUp(context);
        }
    }

    public void cleanUp(StatusEffectContext context) {
        this.item.cleanup(this, context);
    }

    public void addInformation(Consumer<Component> tooltip, boolean isItemTooltip) {
        this.item.addInformation(this, tooltip, isItemTooltip);
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean isActiveAndVisible() {
        return this.isActive() && this.item.visible();
    }

    public boolean isExpired() {
        return this.duration <= 0;
    }

    public EffectGroupItem getItem() {
        return item;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDelay() {
        return delay;
    }

    public int getRequiredGroupLifetime() {
        return this.delay + this.duration;
    }

    public EffectGroupHolder tryMerge(EffectGroupHolder other) {
        return this.item.tryToMergeWith(this, other);
    }

    public EffectGroupHolder copy() {
        return new EffectGroupHolder(this.item.copy(), this.duration, this.delay, this.active);
    }

    public static final class Factory {

        private final List<EffectGroupHolder> output;

        public Factory(List<EffectGroupHolder> output) {
            this.output = output;
        }

        public void create(int duration, int delay, EffectGroupItem item) {
            this.output.add(new EffectGroupHolder(item, duration, delay, false));
        }

        public void create(TickValue duration, int delay, EffectGroupItem item) {
            this.create(duration.tickValue(), delay, item);
        }

        public void create(int duration, TickValue delay, EffectGroupItem item) {
            this.create(duration, delay.tickValue(), item);
        }

        public void create(TickValue duration, TickValue delay, EffectGroupItem item) {
            this.create(duration.tickValue(), delay.tickValue(), item);
        }
    }
}
