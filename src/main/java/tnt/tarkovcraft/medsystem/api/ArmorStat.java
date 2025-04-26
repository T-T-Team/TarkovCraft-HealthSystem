package tnt.tarkovcraft.medsystem.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.common.health.BodyPartGroup;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;

public record ArmorStat(Set<BodyPartGroup> protectedArea) {

    public static final Codec<ArmorStat> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.enumCodec(BodyPartGroup.class).listOf().xmap(EnumSet::copyOf, ArrayList::new).fieldOf("protectedArea").forGetter(t -> EnumSet.copyOf(t.protectedArea))
    ).apply(instance, ArmorStat::new));
}
