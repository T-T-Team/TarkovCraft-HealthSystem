package tnt.tarkovcraft.medsystem.common.effect;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;

import java.util.function.UnaryOperator;

public enum EffectType implements UnaryOperator<Style> {

    POSITIVE,
    NEUTRAL,
    NEGATIVE;

    @Override
    public Style apply(Style style) {
        return style.applyFormat(this == POSITIVE ? ChatFormatting.GREEN : this == NEGATIVE ? ChatFormatting.RED : ChatFormatting.DARK_GRAY);
    }

    public <T> T byValue(T positive, T negative, T neutral) {
        return switch (this) {
            case POSITIVE -> positive;
            case NEGATIVE -> negative;
            case NEUTRAL -> neutral;
        };
    }
}
