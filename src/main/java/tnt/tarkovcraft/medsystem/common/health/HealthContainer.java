package tnt.tarkovcraft.medsystem.common.health;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.network.Synchronizable;

import java.util.Map;

public final class HealthContainer implements Synchronizable<HealthContainer> {

    public static final Codec<HealthContainer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            HealthContainerDefinition.CODEC.fieldOf("def").forGetter(t -> t.definition),
            Codec.unboundedMap(Codec.STRING, BodyPartHealth.CODEC).fieldOf("bodyParts").forGetter(t -> t.bodyParts)
    ).apply(instance, HealthContainer::new));

    private final HealthContainerDefinition definition;
    private final Map<String, BodyPartHealth> bodyParts;

    public HealthContainer() {
        throw new UnsupportedOperationException("Cannot instantiate default health container");
    }

    public HealthContainer(HealthContainerDefinition definition, Map<String, BodyPartHealth> bodyParts) {
        this.definition = definition;
        this.bodyParts = bodyParts;
    }

    public float getHealth() {
        float health = 0.0F;
        for (BodyPartHealth bodyPartHealth : bodyParts.values()) {
            if (bodyPartHealth.shouldOwnerDie()) {
                return 0.0F;
            }
            health += bodyPartHealth.getHealth();
        }
        return health;
    }

    public void updateHealth(LivingEntity entity) {
        float health = this.getHealth();
        entity.setHealth(health);
    }

    @Override
    public Codec<HealthContainer> networkCodec() {
        return CODEC;
    }
}
