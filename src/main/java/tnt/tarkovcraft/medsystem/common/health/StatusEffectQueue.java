package tnt.tarkovcraft.medsystem.common.health;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectHelper;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;

import java.util.ArrayList;
import java.util.PriorityQueue;

public final class StatusEffectQueue {

    public static final Codec<StatusEffectQueue> CODEC = Codecs.collection(QueuedEffect.CODEC, PriorityQueue::new, ArrayList::new)
            .xmap(StatusEffectQueue::new, t -> t.queue);

    private final PriorityQueue<QueuedEffect> queue;

    private StatusEffectQueue() {
        this.queue = new PriorityQueue<>();
    }

    private StatusEffectQueue(PriorityQueue<QueuedEffect> queue) {
        this.queue = queue;
    }

    public static StatusEffectQueue createEmpty() {
        return new StatusEffectQueue();
    }

    public void update(HealthContainer container, LivingEntity entity) {
        QueuedEffect queuedEffect;
        long gameTime = entity.level().getGameTime();
        while ((queuedEffect = this.queue.peek()) != null && queuedEffect.ready(gameTime)) {
            this.queue.poll();
            String limbCode = queuedEffect.limbCode();
            Limb limb = container.getLimbByCode(limbCode);
            StatusEffectMap map = limb.getStatusEffects();
            StatusEffectHelper.addImmediateEffect(map, entity, limb, queuedEffect.createInstance());
            container.setChanged();
        }
    }

    public void clear() {
        this.queue.clear();
    }

    public void submit(Level level, long delay, @Nullable Limb limb, StatusEffect template) {
        long currentTime = level.getGameTime();
        long expireAt = currentTime + delay;
        String limbCode = limb != null ? limb.getLimbCode() : "";
        this.queue.add(new QueuedEffect(expireAt, limbCode, template));
    }

    public record QueuedEffect(long expireAt, String limbCode, StatusEffect effect) implements Comparable<QueuedEffect> {

        public static final Codec<QueuedEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.LONG.fieldOf("target").forGetter(QueuedEffect::expireAt),
                Codec.STRING.optionalFieldOf("limb", "").forGetter(QueuedEffect::limbCode),
                StatusEffectType.CODEC.fieldOf("data").forGetter(QueuedEffect::effect)
        ).apply(instance, QueuedEffect::new));

        public boolean ready(long gameTime) {
            return this.expireAt <= gameTime;
        }

        public StatusEffect createInstance() {
            return this.effect.copy();
        }

        @Override
        public int compareTo(StatusEffectQueue.QueuedEffect o) {
            return Long.compare(this.expireAt, o.expireAt);
        }
    }
}
