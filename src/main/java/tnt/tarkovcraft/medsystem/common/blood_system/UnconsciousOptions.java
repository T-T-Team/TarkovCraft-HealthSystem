package tnt.tarkovcraft.medsystem.common.blood_system;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.MedicalSystem;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public record UnconsciousOptions(boolean allowSkip, boolean allowRescue, boolean downedStateAllowed, Component label, Set<ResourceLocation> tags) {

    public static final ResourceLocation TAG_DOWNED = MedicalSystem.createIdentifier("downed");

    public static final UnconsciousOptions EMPTY = new UnconsciousOptions(true, false, true, CommonComponents.EMPTY);
    public static final UnconsciousOptions PAIN_SHOCK = new UnconsciousOptions(false, false, true, Component.translatable("label.medsystem.unconscious.info.pain"));
    public static final UnconsciousOptions RESCUE_DELAY = new UnconsciousOptions(false, false, false, Component.translatable("label.medsystem.unconscious.info.rescue_delay"));
    public static final UnconsciousOptions IMMUNE_REACTION = new UnconsciousOptions(true, false, false, Component.translatable("label.medsystem.unconscious.info.immune_reaction"));
    public static final UnconsciousOptions DOWNED = new UnconsciousOptions(true, true, false, Component.translatable("label.medsystem.unconscious.info.downed"), Set.of(TAG_DOWNED));
    public static final UnconsciousOptions DOWNED_NO_RESCUE = new UnconsciousOptions(false, false, false, Component.translatable("label.medsystem.unconscious.info.downed"), Set.of(TAG_DOWNED));

    public static final Codec<UnconsciousOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("allow_skip").forGetter(UnconsciousOptions::allowSkip),
            Codec.BOOL.optionalFieldOf("allow_rescue", false).forGetter(UnconsciousOptions::allowRescue),
            Codec.BOOL.optionalFieldOf("allow_downed", true).forGetter(UnconsciousOptions::downedStateAllowed),
            ComponentSerialization.CODEC.fieldOf("label").forGetter(UnconsciousOptions::label),
            Codecs.hashSet(ResourceLocation.CODEC).optionalFieldOf("tags", Collections.emptySet()).forGetter(UnconsciousOptions::tags)
    ).apply(instance, UnconsciousOptions::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, UnconsciousOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, UnconsciousOptions::allowSkip,
            ByteBufCodecs.BOOL, UnconsciousOptions::allowRescue,
            ByteBufCodecs.BOOL, UnconsciousOptions::downedStateAllowed,
            ComponentSerialization.STREAM_CODEC, UnconsciousOptions::label,
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.collection(HashSet::new)), UnconsciousOptions::tags,
            UnconsciousOptions::new
    );

    public UnconsciousOptions(boolean allowSkip, boolean allowRescue, boolean downedStateAllowed, Component label) {
        this(allowSkip, allowRescue, downedStateAllowed, label, Collections.emptySet());
    }

    public boolean is(ResourceLocation tag) {
        return this.tags.contains(tag);
    }
}
