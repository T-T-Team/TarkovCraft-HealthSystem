package tnt.tarkovcraft.medsystem.common.health;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public record LimbConfiguration(String rootLimb, Map<String, LimbDefinition> limbs) {

    public static final Codec<LimbConfiguration> CODEC = RecordCodecBuilder.<LimbConfiguration>create(instance -> instance.group(
            Codec.STRING.fieldOf("root_limb").forGetter(LimbConfiguration::rootLimb),
            Codec.unboundedMap(Codec.STRING, LimbDefinition.CODEC).fieldOf("limbs").forGetter(LimbConfiguration::limbs)
    ).apply(instance, LimbConfiguration::new)).validate(config -> {
        if (!config.limbs.containsKey(config.rootLimb)) {
            return DataResult.error(() -> "No limb configuration found for root limb code " + config.rootLimb);
        }
        return DataResult.success(config);
    });

    public Map<String, Limb> buildLimbInstances() {
        ImmutableMap.Builder<String, Limb> builder = ImmutableMap.builder();
        this.limbs.forEach((code, definition) -> builder.put(code, definition.createLimbInstance(code)));
        return builder.build();
    }

    public LimbDefinition getLimbDefinition(String code) {
        return this.limbs.get(code);
    }

    public Set<String> getLimbCodes() {
        return this.limbs.keySet();
    }

    public float getMaxHealth() {
        float health = 0.0F;
        for (LimbDefinition definition : this.limbs.values()) {
            health += definition.baseHealth();
        }
        return health;
    }

    public int getLimbCount() {
        return this.limbs.size();
    }
}
