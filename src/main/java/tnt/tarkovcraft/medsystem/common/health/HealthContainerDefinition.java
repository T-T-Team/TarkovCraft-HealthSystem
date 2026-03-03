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
import tnt.tarkovcraft.medsystem.common.health.state.EntityStateMatcher;
import tnt.tarkovcraft.medsystem.common.health.state.EntityStateMatcherType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public record HealthContainerDefinition(List<EntityType<?>> targets, LimbConfiguration limbConfiguration, Map<String, EntityStateMatcher> customStateDefinitions, EntityHitboxContainer hitboxContainer, HealthContainerDisplay display, BloodDecalSettings decalSettings) {

    public static final Codec<HealthContainerDefinition> CODEC = RecordCodecBuilder.<HealthContainerDefinition>create(instance -> instance.group(
            Codecs.list(BuiltInRegistries.ENTITY_TYPE.byNameCodec()).fieldOf("targets").forGetter(HealthContainerDefinition::targets),
            LimbConfiguration.CODEC.fieldOf("limb_configuration").forGetter(HealthContainerDefinition::limbConfiguration),
            Codec.unboundedMap(Codec.string(1, 64), EntityStateMatcherType.CODEC).optionalFieldOf("custom_state_definitions", Collections.emptyMap()).forGetter(HealthContainerDefinition::customStateDefinitions),
            EntityHitboxContainer.CODEC.fieldOf("hitbox_container").forGetter(HealthContainerDefinition::hitboxContainer),
            HealthContainerDisplay.CODEC.fieldOf("display_configuration").forGetter(HealthContainerDefinition::display),
            BloodDecalSettings.CODEC.optionalFieldOf("blood_decals", BloodDecalSettings.DEFAULT).forGetter(HealthContainerDefinition::decalSettings)
    ).apply(instance, HealthContainerDefinition::new)).validate(HealthContainerHelper::validate);

    public String getRootLimbCode() {
        return this.limbConfiguration.rootLimb();
    }

    public LimbDefinition getLimbConfiguration(String code) {
        return this.limbConfiguration.getLimbDefinition(code);
    }

    public String getCurrentEntityState(LivingEntity entity) {
        for (Map.Entry<String, EntityStateMatcher> entry : this.customStateDefinitions.entrySet()) {
            EntityStateMatcher matcher = entry.getValue();
            if (matcher.matches(entity)) {
                return entry.getKey();
            }
        }
        return MedSystemConstants.DEFAULT_ENTITY_STATE;
    }

    public void bind(LivingEntity entity) {
        // bind new container only to entities without existing health container or with invalid health data
        HealthContainer data = HealthSystem.hasCustomHealth(entity) ? HealthSystem.getHealthData(entity) : null;
        if (data != null && !data.isInvalid()) {
            return;
        } else if (data != null) {
            data.clearBoundData(entity);
        }

        HealthContainer container = new HealthContainer(entity);
        float containerMaxHealth = this.limbConfiguration.getMaxHealth();
        float entityMaxHealth = entity.getMaxHealth();
        float diff = containerMaxHealth - entityMaxHealth;
        AttributeModifier modifier = new AttributeModifier(HealthSystem.IDENTIFIER, diff, AttributeModifier.Operation.ADD_VALUE);
        AttributeInstance instance = entity.getAttribute(Attributes.MAX_HEALTH);
        instance.addOrReplacePermanentModifier(modifier);
        container.updateHealth(entity);
        entity.setData(MedSystemDataAttachments.HEALTH_CONTAINER, container);
    }
}
