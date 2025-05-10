package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class LightBleedStatusEffect extends BleedStatusEffect {

    public static final MapCodec<LightBleedStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> commonEntity(instance).apply(instance, LightBleedStatusEffect::new));

    public LightBleedStatusEffect(int duration, int delay, Optional<UUID> owner) {
        super(duration, delay);
    }

    public LightBleedStatusEffect(int duration, int delay) {
        super(duration, delay);
    }

    @Override
    public long getDamageInterval() {
        return 60L;
    }

    @Override
    public float getDamageAmount() {
        return 0.5F;
    }

    @Override
    public StatusEffect copy() {
        return new LightBleedStatusEffect(this.getDuration(), this.getDelay());
    }

    @Override
    public void addAdditionalInfo(Consumer<Component> tooltip) {
        tooltip.accept(Component.translatable("status_effect.medsystem.light_bleed.heal_hint").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.LIGHT_BLEED.value();
    }
}
