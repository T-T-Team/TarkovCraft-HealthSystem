package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.datafixers.Products;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.Optional;
import java.util.UUID;

public abstract class EntityCausedStatusEffect extends StatusEffect {

    private UUID owner;

    public EntityCausedStatusEffect(int duration, int delay, Optional<UUID> owner) {
        super(duration, delay);
        this.owner = owner.orElse(null);
    }

    public EntityCausedStatusEffect(int duration, int delay) {
        super(duration, delay);
    }

    @Override
    public void setCausingEntity(UUID owner) {
        this.owner = owner;
    }

    @Override
    public UUID getCausingEntity() {
        return this.owner;
    }

    public static <T extends StatusEffect> Products.P3<RecordCodecBuilder.Mu<T>, Integer, Integer, Optional<UUID>> commonEntity(RecordCodecBuilder.Instance<T> instance) {
        return common(instance).and(UUIDUtil.CODEC.optionalFieldOf("cause").forGetter(t -> Optional.ofNullable(t.getCausingEntity())));
    }
}
