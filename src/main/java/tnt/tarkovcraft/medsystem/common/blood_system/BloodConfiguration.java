package tnt.tarkovcraft.medsystem.common.blood_system;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import tnt.tarkovcraft.core.util.Codecs;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record BloodConfiguration(
        Map<Identifier, BloodTypeOptions> bloodTypes,
        Map<Identifier, Set<Identifier>> compatibilityMap
) {

    public static final Codec<BloodConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Identifier.CODEC, BloodTypeOptions.CODEC).fieldOf("blood_types").forGetter(BloodConfiguration::bloodTypes),
            Codec.unboundedMap(Identifier.CODEC, Codecs.hashSet(Identifier.CODEC)).optionalFieldOf("receiver_compatibility_map", Collections.emptyMap()).forGetter(BloodConfiguration::compatibilityMap)
    ).apply(instance, BloodConfiguration::new));
    private static final BloodConfiguration EMPTY = new BloodConfiguration(Collections.emptyMap(), Collections.emptyMap());

    public static BloodConfiguration missingConfiguration() {
        return EMPTY;
    }

    public Optional<BloodTypeOptions> getOptions(Identifier identifier) {
        return Optional.ofNullable(this.bloodTypes.get(identifier));
    }

    public Optional<Component> getStylizedBloodLabel(Identifier identifier) {
        return this.getOptions(identifier)
                .map(BloodTypeOptions::getStylizedLabel);
    }

    public boolean isCompatibleBloodTypeForTransfusion(Identifier myBloodType, Identifier transfusionType) {
        // simplification, the same blood types will always be compatible
        if (transfusionType.equals(myBloodType))
            return true;
        Set<Identifier> compatibleForTransfusion = this.compatibilityMap.getOrDefault(myBloodType, Collections.emptySet());
        return compatibleForTransfusion.contains(transfusionType);
    }
}
