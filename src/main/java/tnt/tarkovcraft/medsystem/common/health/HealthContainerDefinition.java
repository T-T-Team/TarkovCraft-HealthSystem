package tnt.tarkovcraft.medsystem.common.health;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class HealthContainerDefinition {

    public static final Codec<HealthContainerDefinition> CODEC = RecordCodecBuilder.<HealthContainerDefinition>create(instance -> instance.group(
            Codecs.list(BuiltInRegistries.ENTITY_TYPE.byNameCodec()).optionalFieldOf("targets", Collections.emptyList()).forGetter(t -> t.targets),
            Codec.unboundedMap(Codec.STRING, LimbDefinition.CODEC).optionalFieldOf("health", Collections.emptyMap()).forGetter(t -> t.limbs),
            BodyPartHitbox.CODEC.listOf().optionalFieldOf("hitboxes", Collections.emptyList()).forGetter(t -> t.hitboxes),
            BodyPartDisplay.CODEC.listOf().optionalFieldOf("hud", Collections.emptyList()).forGetter(t -> t.display)
    ).apply(instance, HealthContainerDefinition::new)).validate(HealthContainerHelper::validate);

    private final List<EntityType<?>> targets;
    private final Map<String, LimbDefinition> limbs;
    private final List<BodyPartHitbox> hitboxes;
    private final List<BodyPartDisplay> display;

    HealthContainerDefinition(List<EntityType<?>> targets, Map<String, LimbDefinition> limbs, List<BodyPartHitbox> hitboxes, List<BodyPartDisplay> display) {
        this.targets = targets;
        this.limbs = limbs;
        this.hitboxes = hitboxes;
        this.display = display;
    }

    public LimbDefinition getLimbConfiguration(String code) {
        return limbs.get(code);
    }

    public Map<String, LimbDefinition> getLimbDefinitionMap() {
        return limbs;
    }

    public List<BodyPartHitbox> getHitboxes() {
        return hitboxes;
    }

    // FIXME make health setting more compatible with mods reducing health by default
    public void bind(LivingEntity entity) {
        // bind new container only to entities without existing health container or with invalid health data
        HealthContainer data = HealthSystem.hasCustomHealth(entity) ? HealthSystem.getHealthData(entity) : null;
        if (data != null && !data.isInvalid()) {
            return;
        } else if (data != null) {
            data.clearBoundData(entity);
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
        for (LimbDefinition definition : this.limbs.values()) {
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
}
