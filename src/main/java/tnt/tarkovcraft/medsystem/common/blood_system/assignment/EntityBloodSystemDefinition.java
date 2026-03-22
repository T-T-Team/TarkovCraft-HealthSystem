package tnt.tarkovcraft.medsystem.common.blood_system.assignment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector2fc;
import tnt.tarkovcraft.core.common.attribute.AttributeSystem;
import tnt.tarkovcraft.core.common.data.number.NumberProvider;
import tnt.tarkovcraft.core.common.data.number.NumberProviderType;
import tnt.tarkovcraft.core.common.util.AttributeNumber;
import tnt.tarkovcraft.core.util.Cached;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.blood_system.effect.BloodLevelEffectHolder;
import tnt.tarkovcraft.medsystem.common.effect.BloodLossStatusEffect;
import tnt.tarkovcraft.medsystem.common.health.HealthBloodSystemIntegration;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemAttributes;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;
import tnt.tarkovcraft.medsystem.util.WeightedList;

import java.util.*;

public final class EntityBloodSystemDefinition {

    public static final Codec<EntityBloodSystemDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.hashSet(BuiltInRegistries.ENTITY_TYPE.byNameCodec()).fieldOf("entity_types").forGetter(t -> t.entityTypes),
            Codec.unboundedMap(ResourceLocation.CODEC, ExtraCodecs.NON_NEGATIVE_INT).fieldOf("blood_types").forGetter(t -> t.bloodTypes),
            NumberProviderType.valueCodec(ExtraCodecs.POSITIVE_FLOAT).fieldOf("blood_volume").forGetter(t -> t.bloodVolume),
            AttributeNumber.CODEC.fieldOf("blood_recovery").forGetter(t -> t.bloodRecovery),
            UnconsciousModeSettings.CODEC.optionalFieldOf("unconscious_mode", UnconsciousModeSettings.DEFAULT).forGetter(t -> t.unconsciousMode),
            EntityShockData.CODEC.optionalFieldOf("shock_attributes", EntityShockData.DEFAULT).forGetter(t -> t.shockData),
            BloodEffectAttributes.CODEC.optionalFieldOf("blood_effect_attributes", BloodEffectAttributes.DEFAULT).forGetter(t -> t.effectAttributes),
            Codecs.list(BloodLevelEffectHolder.CODEC).optionalFieldOf("effect_list", Collections.emptyList()).forGetter(t -> t.effectList)
    ).apply(instance, EntityBloodSystemDefinition::new));

    public static final int BLOOD_COLOR = 0xB20000;

    private final Set<EntityType<?>> entityTypes;
    private final Map<ResourceLocation, Integer> bloodTypes;
    private final NumberProvider bloodVolume;
    private final AttributeNumber bloodRecovery;
    private final UnconsciousModeSettings unconsciousMode;
    private final EntityShockData shockData;
    private final BloodEffectAttributes effectAttributes;
    private final List<BloodLevelEffectHolder> effectList;

    private final WeightedList<ResourceLocation> weightedBloodTypes;
    private final Cached<EntityDimensions> unconsciousModeDimensions;

    private EntityBloodSystemDefinition(Set<EntityType<?>> entityTypes, Map<ResourceLocation, Integer> bloodTypes, NumberProvider bloodVolume, AttributeNumber bloodRecovery, UnconsciousModeSettings unconsciousMode, EntityShockData shockData, BloodEffectAttributes effectAttributes, List<BloodLevelEffectHolder> effectList) {
        this.entityTypes = entityTypes;
        this.bloodTypes = bloodTypes;
        this.bloodVolume = bloodVolume;
        this.bloodRecovery = bloodRecovery;
        this.unconsciousMode = unconsciousMode;
        this.shockData = shockData;
        this.effectAttributes = effectAttributes;
        this.effectList = effectList;

        this.weightedBloodTypes = this.computeWeightedList();
        this.unconsciousModeDimensions = Cached.create(() -> {
            Vector2fc dims = this.unconsciousMode.dimensions();
            return EntityDimensions.scalable(dims.x(), dims.y());
        });
    }

    public static EntityBloodSystemDefinition forEntity(LivingEntity entity) {
        EntityType<?> type = entity.getType();
        return forEntityType(type);
    }

    public static EntityBloodSystemDefinition forEntityType(EntityType<?> type) {
        BloodSystemManager manager = MedicalSystem.BLOOD_SYSTEM;
        return manager.getAssignment(type);
    }

    public void bind(LivingEntity entity) {
        EntityType<?> type = entity.getType();
        ResourceLocation bloodType = this.weightedBloodTypes.getRandomOrThrow(entity.getRandom());
        EntityBloodSystem system = new EntityBloodSystem(type, bloodType, this.getMaxBloodVolume());
        entity.setData(MedSystemDataAttachments.BLOOD_SYSTEM, system);

        this.bindListeners(system, entity);
    }

    public void bindListeners(EntityBloodSystem bloodSystem, LivingEntity entity) {
        if (HealthSystem.hasCustomHealth(entity)) {
            HealthContainer container = HealthSystem.getHealthData(entity);
            bloodSystem.eventHandler.subscribe(new HealthBloodSystemIntegration(container));
        }
    }

    public void applyEffects(LivingEntity entity, ServerLevel level, EntityBloodSystem bloodSystem) {
        float bloodVolume = bloodSystem.getBloodVolume();
        for (BloodLevelEffectHolder effect : this.effectList) {
            if (effect.canApply(bloodVolume))
                effect.apply(entity, level, bloodSystem);
        }
    }

    public float getMaxBloodVolume() {
        return this.bloodVolume.floatValue();
    }

    public boolean canUseBloodType(ResourceLocation bloodType) {
        return this.bloodTypes.containsKey(bloodType);
    }

    public boolean isUnconsciousModeAllowed() {
        return this.unconsciousMode.enabled();
    }

    public boolean hasSpecialUnconsciousPoseRenderer() {
        return this.unconsciousMode.hasCustomPose();
    }

    public boolean isDownedStateEnabled() {
        return this.isUnconsciousModeAllowed() && this.unconsciousMode.downedOnDeath();
    }

    public EntityDimensions getDimensionsForUnconsciousMode() {
        return this.unconsciousModeDimensions.get();
    }

    public boolean isInPain(float bloodPct) {
        return this.effectAttributes.isInPain(bloodPct);
    }

    public float getGrayscaleAmount(float bloodPct) {
        return this.effectAttributes.getGrayscale(bloodPct);
    }

    public boolean shouldApplyGrayscaleShader(float bloodPct) {
        return this.effectAttributes.shouldApplyGrayscale(bloodPct);
    }

    public float getBloodRegenerationAmount(LivingEntity entity) {
        return (float) this.bloodRecovery.getValue(entity);
    }

    public BloodLossStatusEffect.Stage getBloodLossStage(float percentage) {
        return this.effectAttributes.getBloodLossStage(percentage);
    }

    public List<ResourceLocation> getAvailableBloodTypes() {
        return this.bloodTypes.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .toList();
    }

    public Collection<EntityType<?>> getEntityTypes() {
        return this.entityTypes;
    }

    public float getShockRecoveryRate(boolean inShock) {
        float multiplier = inShock ? this.shockData.inShockRecoveryMultiplier() : 1.0F;
        return this.shockData.recoveryRate() * multiplier;
    }

    public float getReceivedShockValue(float incoming, LivingEntity entity) {
        return AttributeSystem.getFloatValue(entity, MedSystemAttributes.SHOCK_SCALE, 1.0F) * (incoming * this.shockData.receptionMultiplier());
    }

    public boolean isInShock(float value) {
        return this.shockData.isUnconscious(value);
    }

    private WeightedList<ResourceLocation> computeWeightedList() {
        WeightedList.Builder<ResourceLocation> builder = WeightedList.builder();
        this.bloodTypes.forEach(builder::add);
        return builder.build();
    }
}
