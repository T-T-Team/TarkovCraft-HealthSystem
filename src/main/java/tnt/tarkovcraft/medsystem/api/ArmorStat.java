package tnt.tarkovcraft.medsystem.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.common.health.LimbType;

import java.util.Set;

@Deprecated
public record ArmorStat(Set<LimbType> protectedArea) {

    public static final Codec<ArmorStat> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.enumSet(LimbType.class).fieldOf("protectedArea").forGetter(t -> t.protectedArea)
    ).apply(instance, ArmorStat::new));
}
