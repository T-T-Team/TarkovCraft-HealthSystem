package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public record QueuedStatusEffect(long target, String limb, StatusEffect data) implements Comparable<QueuedStatusEffect> {

    public static final Codec<QueuedStatusEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("target").forGetter(QueuedStatusEffect::target),
            Codec.STRING.optionalFieldOf("limb", "").forGetter(QueuedStatusEffect::limb),
            StatusEffectType.CODEC.fieldOf("data").forGetter(QueuedStatusEffect::data)
    ).apply(instance, QueuedStatusEffect::new));

    public boolean isReady(Level level) {
        return this.isReady(level.getGameTime());
    }

    public boolean isReady(long time) {
        return this.target <= time;
    }

    @Override
    public int compareTo(@NotNull QueuedStatusEffect o) {
        return Long.compare(this.target(), o.target());
    }
}