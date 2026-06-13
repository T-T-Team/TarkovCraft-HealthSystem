package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import java.util.function.Consumer;

public class OverweightStatusEffect extends SimpleStatusEffect {

    public static final MapCodec<OverweightStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("limit_exceeded", false).forGetter(t -> t.limitExceeded)
    ).apply(instance, OverweightStatusEffect::new));
    private static final Component HINT = Component.translatable("status_effect.medsystem.overweight.info").withStyle(ChatFormatting.DARK_GRAY);
    private static final Component HINT_MAX = Component.translatable("status_effect.medsystem.max_overweight.info").withStyle(ChatFormatting.DARK_GRAY);
    private static final Identifier MAX_OVERWEIGHT_ICON = MedicalSystem.createIdentifier("textures/icons/status_effect/max_overweight.png");

    private final boolean limitExceeded;

    public OverweightStatusEffect(boolean limitExceeded) {
        super(-1);
        this.limitExceeded = limitExceeded;
    }

    public static OverweightStatusEffect createTemplate(boolean limitExceeded) {
        return new OverweightStatusEffect(limitExceeded);
    }

    @Override
    public StatusEffect copy() {
        return new OverweightStatusEffect(this.limitExceeded);
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.OVERWEIGHT.value();
    }

    @Override
    public void addAdditionalInfo(Consumer<Component> tooltip) {
        tooltip.accept(this.limitExceeded ? HINT_MAX : HINT);
    }

    @Override
    public @Nullable Identifier getCustomIcon() {
        return this.limitExceeded ? MAX_OVERWEIGHT_ICON : null;
    }
}
