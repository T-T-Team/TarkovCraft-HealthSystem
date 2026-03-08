package tnt.tarkovcraft.medsystem.common.blood_system;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record UnconsciousOptions(boolean allowSkip, boolean allowRescue, Component label) {

    public static final UnconsciousOptions EMPTY = new UnconsciousOptions(true, false, CommonComponents.EMPTY);
    public static final UnconsciousOptions PAIN = new UnconsciousOptions(false, false, Component.translatable("label.medsystem.unconscious.info.pain"));
    public static final UnconsciousOptions RESCUE_DELAY = new UnconsciousOptions(false, false, Component.translatable("label.medsystem.unconscious.info.rescue_delay"));
    public static final UnconsciousOptions DOWNED = new UnconsciousOptions(true, true, Component.translatable("label.medsystem.unconscious.info.downed"));

    public static final Codec<UnconsciousOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("allow_skip").forGetter(UnconsciousOptions::allowSkip),
            Codec.BOOL.optionalFieldOf("allow_rescue", false).forGetter(UnconsciousOptions::allowRescue),
            ComponentSerialization.CODEC.fieldOf("label").forGetter(UnconsciousOptions::label)
    ).apply(instance, UnconsciousOptions::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, UnconsciousOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, UnconsciousOptions::allowSkip,
            ByteBufCodecs.BOOL, UnconsciousOptions::allowRescue,
            ComponentSerialization.STREAM_CODEC, UnconsciousOptions::label,
            UnconsciousOptions::new
    );
}
