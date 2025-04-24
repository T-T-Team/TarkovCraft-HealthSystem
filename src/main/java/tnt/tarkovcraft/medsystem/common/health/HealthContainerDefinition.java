package tnt.tarkovcraft.medsystem.common.health;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;

import java.util.*;
import java.util.stream.Collectors;

public final class HealthContainerDefinition {

    public static final Codec<HealthContainerDefinition> CODEC = RecordCodecBuilder.<HealthContainerDefinition>create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("replace", false).forGetter(t -> t.replace),
            Codecs.list(BuiltInRegistries.ENTITY_TYPE.byNameCodec()).optionalFieldOf("targets",Collections.emptyList()).forGetter(t -> t.targets),
            Codec.unboundedMap(Codec.STRING, BodyPartHealthDefinition.CODEC).optionalFieldOf("health", Collections.emptyMap()).forGetter(t -> t.bodyParts),
            BodyPartHitbox.CODEC.listOf().optionalFieldOf("hitboxes", Collections.emptyList()).forGetter(t -> t.hitboxes)
    ).apply(instance, HealthContainerDefinition::new)).validate(HealthContainerDefinition::validate);

    private final boolean replace;
    private final List<EntityType<?>> targets;
    private final Map<String, BodyPartHealthDefinition> bodyParts;
    private final List<BodyPartHitbox> hitboxes;

    public HealthContainerDefinition(boolean replace, List<EntityType<?>> targets, Map<String, BodyPartHealthDefinition> bodyParts, List<BodyPartHitbox> hitboxes) {
        this.replace = replace;
        this.targets = targets;
        this.bodyParts = bodyParts;
        this.hitboxes = hitboxes;
    }

    private static DataResult<HealthContainerDefinition> validate(HealthContainerDefinition container) {
        Set<String> hitboxOwners = container.hitboxes.stream().map(BodyPartHitbox::getOwner).collect(Collectors.toSet());
        if (hitboxOwners.size() != container.bodyParts.size()) {
            return DataResult.error(() -> "Mismatched hitbox count. Got " + hitboxOwners.size() + ", expected " + container.bodyParts.size());
        }
        for (String owner : container.bodyParts.keySet()) {
            if (!hitboxOwners.contains(owner)) {
                return DataResult.error(() -> "Missing hitbox definition for body part " + owner);
            }
        }
        return DataResult.success(container);
    }

    public BodyPartHealthDefinition getHealthTpl(String id) {
        return bodyParts.get(id);
    }

    public Map<String, BodyPartHealthDefinition> getBodyParts() {
        return bodyParts;
    }

    public List<BodyPartHitbox> getHitboxes() {
        return hitboxes;
    }

    public void bind(LivingEntity entity) {
        Map<String, BodyPartHealth> bodyParts = new HashMap<>();
        for (Map.Entry<String, BodyPartHealthDefinition> entry : this.bodyParts.entrySet()) {
            String partName = entry.getKey();
            BodyPartHealthDefinition definition = entry.getValue();
            bodyParts.put(partName, definition.createContainer());
        }
        HealthContainer container = new HealthContainer(this, bodyParts);
        entity.setData(MedSystemDataAttachments.HEALTH_CONTAINER, container);
        HealthSystem.synchronizeEntity(entity);
    }

    public List<EntityType<?>> getTargets() {
        return targets;
    }

    public HealthContainerDefinition merge(HealthContainerDefinition other) {
        if (other.replace) {
            return other;
        }
        List<EntityType<?>> targets = new ArrayList<>(this.targets);
        targets.addAll(other.targets);
        Map<String, BodyPartHealthDefinition> newBodyParts = new HashMap<>(this.bodyParts);
        for (Map.Entry<String, BodyPartHealthDefinition> entry : other.bodyParts.entrySet()) {
            BodyPartGroup group = entry.getValue().getBodyPartGroup();
            if (group.isInactive()) {
                newBodyParts.remove(entry.getKey());
            } else {
                newBodyParts.put(entry.getKey(), entry.getValue());
            }
        }
        List<BodyPartHitbox> hitboxes = new ArrayList<>(this.hitboxes);
        hitboxes.addAll(other.hitboxes);
        return new HealthContainerDefinition(
                this.replace,
                targets,
                newBodyParts,
                hitboxes
        );
    }
}
