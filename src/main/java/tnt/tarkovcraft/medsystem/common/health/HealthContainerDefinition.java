package tnt.tarkovcraft.medsystem.common.health;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;

import java.util.*;

public final class HealthContainerDefinition {

    public static final Codec<HealthContainerDefinition> CODEC = RecordCodecBuilder.<HealthContainerDefinition>create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("replace", false).forGetter(t -> t.replace),
            Codecs.list(BuiltInRegistries.ENTITY_TYPE.byNameCodec()).optionalFieldOf("targets", Collections.emptyList()).forGetter(t -> t.targets),
            Codec.unboundedMap(Codec.STRING, BodyPartDefinition.CODEC).optionalFieldOf("health", Collections.emptyMap()).forGetter(t -> t.bodyParts),
            BodyPartHitbox.CODEC.listOf().optionalFieldOf("hitboxes", Collections.emptyList()).forGetter(t -> t.hitboxes),
            BodyPartDisplay.CODEC.listOf().optionalFieldOf("hud", Collections.emptyList()).forGetter(t -> t.display)
    ).apply(instance, HealthContainerDefinition::new)).validate(HealthContainerHelper::validate);

    private final boolean replace;
    private final List<EntityType<?>> targets;
    private final Map<String, BodyPartDefinition> bodyParts;
    private final List<BodyPartHitbox> hitboxes;
    private final List<BodyPartDisplay> display;

    HealthContainerDefinition(boolean replace, List<EntityType<?>> targets, Map<String, BodyPartDefinition> bodyParts, List<BodyPartHitbox> hitboxes, List<BodyPartDisplay> display) {
        this.replace = replace;
        this.targets = targets;
        this.bodyParts = bodyParts;
        this.hitboxes = hitboxes;
        this.display = display;
    }

    public BodyPartDefinition getHealthTpl(String id) {
        return bodyParts.get(id);
    }

    public Map<String, BodyPartDefinition> getBodyParts() {
        return bodyParts;
    }

    public List<BodyPartHitbox> getHitboxes() {
        return hitboxes;
    }

    public void bind(LivingEntity entity) {
        // bind new container only to entities without existing health container or with invalid health data
        if (HealthSystem.hasCustomHealth(entity) && !entity.getData(MedSystemDataAttachments.HEALTH_CONTAINER).isInvalid()) {
            return;
        }

        float maxHealth = this.getMaxHealth();
        AttributeInstance instance = entity.getAttribute(Attributes.MAX_HEALTH);
        if (instance != null) {
            instance.setBaseValue(maxHealth);
        }
        HealthContainer container = new HealthContainer(entity);
        container.updateHealth(entity);
        entity.setData(MedSystemDataAttachments.HEALTH_CONTAINER, container);
    }

    public float getMaxHealth() {
        float value = 0.0F;
        for (BodyPartDefinition definition : this.bodyParts.values()) {
            value += definition.getMaxHealth();
        }
        return value;
    }

    public List<BodyPartDisplay> getDisplayConfiguration() {
        return display;
    }

    List<EntityType<?>> getTargets() {
        return targets;
    }

    public boolean canReplace() {
        return this.replace;
    }
}
