package tnt.tarkovcraft.medsystem.common.blood_system;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import tnt.tarkovcraft.core.util.Codecs;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public record BloodConfiguration(
        Map<ResourceLocation, BloodTypeOptions> bloodTypes,
        Map<ResourceLocation, Set<ResourceLocation>> compatibilityMap
) {

    public static final Codec<BloodConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(ResourceLocation.CODEC, BloodTypeOptions.CODEC).fieldOf("blood_types").forGetter(BloodConfiguration::bloodTypes),
            Codec.unboundedMap(ResourceLocation.CODEC, Codecs.hashSet(ResourceLocation.CODEC)).optionalFieldOf("compatibility_map", Collections.emptyMap()).forGetter(BloodConfiguration::compatibilityMap)
    ).apply(instance, BloodConfiguration::new));
    private static final BloodConfiguration EMPTY = new BloodConfiguration(Collections.emptyMap(), Collections.emptyMap());

    public static BloodConfiguration missingConfiguration() {
        return EMPTY;
    }

    public BloodTypeOptions getOptions(ResourceLocation identifier) {
        return this.bloodTypes.get(identifier);
    }

    public boolean isCompatible(ResourceLocation currentBloodType, ResourceLocation compatibleWith) {
        if (currentBloodType.equals(compatibleWith))
            return true;
        Set<ResourceLocation> compatibleTypes = this.compatibilityMap.getOrDefault(currentBloodType, Collections.emptySet());
        return compatibleTypes.contains(compatibleWith);
    }
}
