package tnt.tarkovcraft.medsystem.common.effect.util;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffectCategory;

import java.util.function.UnaryOperator;

public enum EffectType implements UnaryOperator<Style>, StringRepresentable {

    POSITIVE("positive"),
    NEUTRAL("neutral"),
    NEGATIVE("negative");

    public static final Codec<EffectType> CODEC = StringRepresentable.fromEnum(EffectType::values);
    private final String serializedName;

    EffectType(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public Style apply(Style style) {
        return style.applyFormat(this == POSITIVE ? ChatFormatting.GREEN : this == NEGATIVE ? ChatFormatting.RED : ChatFormatting.YELLOW);
    }

    public <T> T byValue(T positive, T negative, T neutral) {
        return switch (this) {
            case POSITIVE -> positive;
            case NEGATIVE -> negative;
            case NEUTRAL -> neutral;
        };
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public static EffectType byMobEffectCategory(MobEffectCategory category) {
        return switch (category) {
            case HARMFUL -> NEGATIVE;
            case NEUTRAL -> NEUTRAL;
            case BENEFICIAL ->  POSITIVE;
        };
    }
}
