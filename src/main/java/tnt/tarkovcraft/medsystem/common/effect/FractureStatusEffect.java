package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class FractureStatusEffect extends EntityCausedStatusEffect {

    public static final MapCodec<FractureStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> commonEntity(instance).apply(instance, FractureStatusEffect::new));

    public FractureStatusEffect(int duration, int delay, Optional<UUID> owner) {
        super(duration, delay, owner);
    }

    public FractureStatusEffect(int duration, int delay) {
        super(duration, delay);
    }

    @Override
    public StatusEffect copy() {
        return new FractureStatusEffect(this.getDuration(), this.getDelay());
    }

    @Override
    public void apply(Context context) {
    }

    @Override
    public StatusEffect onRemoved(Context context) {
        return null;
    }

    @Override
    public void addAdditionalInfo(Consumer<Component> tooltip) {
        tooltip.accept(Component.translatable("status_effect.medsystem.fracture.heal_hint").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.FRACTURE.value();
    }
}
