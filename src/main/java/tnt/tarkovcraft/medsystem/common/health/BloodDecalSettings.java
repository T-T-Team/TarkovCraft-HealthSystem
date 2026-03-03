package tnt.tarkovcraft.medsystem.common.health;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.util.Codecs;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public record BloodDecalSettings(List<Integer> defaultColors, Map<EntityType<?>, List<Integer>> overrides) {

    public static final Codec<BloodDecalSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.list(ExtraCodecs.STRING_RGB_COLOR).fieldOf("colors").forGetter(BloodDecalSettings::defaultColors),
            Codec.unboundedMap(
                    BuiltInRegistries.ENTITY_TYPE.byNameCodec(),
                    Codecs.list(ExtraCodecs.STRING_RGB_COLOR)
            ).optionalFieldOf("entity_overrides", Collections.emptyMap()).forGetter(BloodDecalSettings::overrides)
    ).apply(instance, BloodDecalSettings::new));

    public static final BloodDecalSettings DEFAULT = new BloodDecalSettings(Collections.singletonList(0xB20000), Collections.emptyMap());

    public Integer getColor(LivingEntity entity) {
        RandomSource random = entity.getRandom();
        List<Integer> availableColors = this.overrides.getOrDefault(entity.getType(), this.defaultColors);
        if (availableColors.isEmpty())
            return null;
        return availableColors.get(random.nextInt(availableColors.size()));
    }
}
