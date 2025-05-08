package tnt.tarkovcraft.medsystem.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.common.health.BodyPartGroup;

import java.util.Set;

public record ArmorStat(Set<BodyPartGroup> protectedArea) {

    public static final Codec<ArmorStat> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.enumSet(BodyPartGroup.class).fieldOf("protectedArea").forGetter(t -> t.protectedArea)
    ).apply(instance, ArmorStat::new));
}
