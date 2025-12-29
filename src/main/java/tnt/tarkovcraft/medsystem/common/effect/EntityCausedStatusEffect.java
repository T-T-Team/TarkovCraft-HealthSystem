package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.datafixers.Products;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public abstract class EntityCausedStatusEffect extends StatusEffect {

    private UUID owner;

    public EntityCausedStatusEffect(int duration, Optional<UUID> owner) {
        super(duration);
        this.owner = owner.orElse(null);
    }

    public EntityCausedStatusEffect(int duration) {
        super(duration);
    }

    @Override
    public void setCausingEntity(@Nullable UUID owner) {
        this.owner = owner;
    }

    @Override
    public @Nullable UUID getCausingEntity() {
        return this.owner;
    }

    public static <T extends StatusEffect> Products.P2<RecordCodecBuilder.Mu<T>, Integer, Optional<UUID>> commonEntity(RecordCodecBuilder.Instance<T> instance) {
        return common(instance).and(UUIDUtil.CODEC.optionalFieldOf("cause").forGetter(t -> Optional.ofNullable(t.getCausingEntity())));
    }
}
