package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import tnt.tarkovcraft.core.common.data.duration.Duration;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class HeavyBleedStatusEffect extends BleedStatusEffect {

    public static final MapCodec<HeavyBleedStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> commonEntity(instance).apply(instance, HeavyBleedStatusEffect::new));

    public HeavyBleedStatusEffect(int duration, int delay, Optional<UUID> owner) {
        super(duration, delay, owner);
    }

    public HeavyBleedStatusEffect(int duration, int delay) {
        super(duration, delay);
    }

    @Override
    public long getDamageInterval() {
        return 30L;
    }

    @Override
    public float getDamageAmount() {
        return 0.75F;
    }

    @Override
    public StatusEffect copy() {
        return new HeavyBleedStatusEffect(this.getDuration(), this.getDelay());
    }

    @Override
    public StatusEffect onRemoved(Context context) {
        return new FreshWoundStatusEffect(
                Duration.minutes(5).tickValue(),
                Duration.seconds(5).tickValue()
        );
    }

    @Override
    public void addAdditionalInfo(Consumer<Component> tooltip) {
        tooltip.accept(Component.translatable("status_effect.medsystem.heavy_bleed.heal_hint").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.HEAVY_BLEED.value();
    }
}
