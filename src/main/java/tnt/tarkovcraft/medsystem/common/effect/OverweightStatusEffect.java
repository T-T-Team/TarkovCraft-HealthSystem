package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import java.util.function.Consumer;

public class OverweightStatusEffect extends StatusEffect {

    public static final MapCodec<OverweightStatusEffect> CODEC = MapCodec.unit(OverweightStatusEffect::new);
    private static final Component HINT = Component.translatable("status_effect.medsystem.overweight.info").withStyle(ChatFormatting.DARK_GRAY);

    public OverweightStatusEffect() {
        super(-1, 0);
    }

    public static OverweightStatusEffect createTemplate() {
        return new OverweightStatusEffect();
    }

    @Override
    public void apply(Context context) {
    }

    @Override
    public StatusEffect onRemoved(Context context) {
        return null;
    }

    @Override
    public StatusEffect copy() {
        return new OverweightStatusEffect();
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.OVERWEIGHT.value();
    }

    @Override
    public void addAdditionalInfo(Consumer<Component> tooltip) {
        tooltip.accept(HINT);
    }
}
