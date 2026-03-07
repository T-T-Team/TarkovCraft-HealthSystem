package tnt.tarkovcraft.medsystem.common.blood_system;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.ExtraCodecs;

public record BloodTypeOptions(int color, Component label) {

    public static final Codec<BloodTypeOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.STRING_RGB_COLOR.optionalFieldOf("color", 0xB2000).forGetter(BloodTypeOptions::color),
            ComponentSerialization.CODEC.fieldOf("label").forGetter(BloodTypeOptions::label)
    ).apply(instance, BloodTypeOptions::new));
}
