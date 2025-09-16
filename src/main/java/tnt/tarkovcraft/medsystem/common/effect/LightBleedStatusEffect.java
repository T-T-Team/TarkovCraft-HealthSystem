package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class LightBleedStatusEffect extends BleedStatusEffect {

    public static final MapCodec<LightBleedStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> commonEntity(instance).apply(instance, LightBleedStatusEffect::new));
    private static final Component HINT = Component.translatable("status_effect.medsystem.light_bleed.heal_hint").withStyle(ChatFormatting.DARK_GRAY);

    public LightBleedStatusEffect(int duration, Optional<UUID> owner) {
        super(duration);
    }

    public LightBleedStatusEffect(int duration) {
        super(duration);
    }

    public static LightBleedStatusEffect createTemplate() {
        return new LightBleedStatusEffect(-1);
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
    public float getBloodLossAmount(LivingEntity entity) {
        return 0.0025F; // 0.1L/min
    }

    @Override
    public StatusEffect copy() {
        return new LightBleedStatusEffect(this.getDuration());
    }

    @Override
    public void addAdditionalInfo(Consumer<Component> tooltip) {
        tooltip.accept(HINT);
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.LIGHT_BLEED.value();
    }
}
